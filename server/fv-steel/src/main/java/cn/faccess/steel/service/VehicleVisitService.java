package cn.faccess.steel.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.web.PageResult;
import cn.faccess.steel.adapter.TradePlatformAdapter;
import cn.faccess.steel.dto.VehicleVisitReq;
import cn.faccess.steel.entity.TradeOrderRef;
import cn.faccess.steel.entity.Vehicle;
import cn.faccess.steel.entity.VehicleVisit;
import cn.faccess.steel.entity.WeighRecord;
import cn.faccess.steel.mapper.TradeOrderRefMapper;
import cn.faccess.steel.mapper.VehicleMapper;
import cn.faccess.steel.mapper.VehicleVisitMapper;
import cn.faccess.steel.mapper.WeighRecordMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆到厂：登记（关联交易平台单据、排号）、叫号、放行（净重回写）。
 */
@Service
public class VehicleVisitService {

    private final VehicleVisitMapper visitMapper;
    private final VehicleMapper vehicleMapper;
    private final TradeOrderRefMapper tradeRefMapper;
    private final WeighRecordMapper weighMapper;
    private final TradePlatformAdapter tradeAdapter;

    public VehicleVisitService(VehicleVisitMapper visitMapper, VehicleMapper vehicleMapper,
                               TradeOrderRefMapper tradeRefMapper, WeighRecordMapper weighMapper,
                               TradePlatformAdapter tradeAdapter) {
        this.visitMapper = visitMapper;
        this.vehicleMapper = vehicleMapper;
        this.tradeRefMapper = tradeRefMapper;
        this.weighMapper = weighMapper;
        this.tradeAdapter = tradeAdapter;
    }

    /** 核验并带出交易平台单据。 */
    public TradePlatformAdapter.TradeOrderView verifyOrder(String orderNo, String orderType) {
        if (!StringUtils.hasText(orderNo)) throw new BizException(400, "请填写单据号");
        return tradeAdapter.verifyOrder(orderNo, orderType);
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleVisit checkin(VehicleVisitReq req) {
        // 车辆档案：不存在则登记
        Vehicle vehicle = vehicleMapper.selectOne(Wrappers.<Vehicle>lambdaQuery()
                .eq(Vehicle::getPlateNo, req.getPlateNo()).last("limit 1"));
        if (vehicle == null) {
            vehicle = new Vehicle();
            vehicle.setPlateNo(req.getPlateNo());
            vehicle.setCarrierId(req.getCarrierId());
            vehicle.setStatus("NORMAL");
            vehicle.setWhitelist(0);
            vehicleMapper.insert(vehicle);
        } else if ("BLACK".equals(vehicle.getStatus())) {
            throw new BizException("该车辆已被限制入厂");
        }

        VehicleVisit visit = new VehicleVisit();
        visit.setGateId(req.getGateId());
        visit.setPlateNo(req.getPlateNo());
        visit.setCarrierId(req.getCarrierId());
        visit.setDriverId(req.getDriverId());
        visit.setDirection(req.getDirection());
        visit.setOrderNo(req.getOrderNo());
        visit.setGoodsName(req.getGoodsName());
        visit.setGoodsSpec(req.getGoodsSpec());
        visit.setPlanWeight(req.getPlanWeight());
        visit.setWarehouse(req.getWarehouse());
        visit.setDeductRate(req.getDeductRate() == null ? BigDecimal.ZERO : req.getDeductRate());
        visit.setStatus("REGISTERED");
        visit.setQueueNo(genQueueNo(req.getDirection()));
        visit.setTimeline("[\"" + LocalDateTime.now() + " 到厂登记\"]");
        visitMapper.insert(visit);

        // 关联交易平台单据
        if (StringUtils.hasText(req.getOrderNo())) {
            String orderType = "INBOUND".equals(req.getDirection()) ? "PURCHASE" : "SALES";
            TradePlatformAdapter.TradeOrderView v = tradeAdapter.verifyOrder(req.getOrderNo(), orderType);
            TradeOrderRef ref = new TradeOrderRef();
            ref.setVehicleVisitId(visit.getId());
            ref.setExtOrderNo(v.getOrderNo());
            ref.setOrderType(v.getOrderType());
            ref.setPartnerName(v.getPartnerName());
            ref.setGoodsName(v.getGoodsName());
            ref.setPlanQty(v.getPlanQty());
            ref.setDoneQty(v.getDoneQty());
            ref.setRemainQty(v.getRemainQty());
            ref.setSettleStatus(v.getSettleStatus());
            ref.setSyncTime(LocalDateTime.now());
            tradeRefMapper.insert(ref);
            if (!StringUtils.hasText(visit.getGoodsName())) {
                visit.setGoodsName(v.getGoodsName());
                visitMapper.updateById(visit);
            }
        }
        return visit;
    }

    public PageResult<VehicleVisit> queue(String direction, long page, long size) {
        IPage<VehicleVisit> p = visitMapper.selectPage(new Page<>(page, size),
                Wrappers.<VehicleVisit>lambdaQuery()
                        .eq(StringUtils.hasText(direction), VehicleVisit::getDirection, direction)
                        .ne(VehicleVisit::getStatus, "RELEASED")
                        .orderByAsc(VehicleVisit::getQueueNo));
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public void call(Long id) {
        VehicleVisit v = require(id);
        v.setStatus("LOADING");
        v.setTimeline(appendTimeline(v.getTimeline(), "叫号 · 进入装卸作业"));
        visitMapper.updateById(v);
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleVisit release(Long id) {
        VehicleVisit v = require(id);
        WeighRecord w = weighMapper.selectOne(Wrappers.<WeighRecord>lambdaQuery()
                .eq(WeighRecord::getVehicleVisitId, id).last("limit 1"));
        if (w == null || w.getNetWeight() == null) {
            throw new BizException("尚未完成过磅（皮重/毛重），不能放行");
        }
        BigDecimal net = w.getSettleWeight() != null ? w.getSettleWeight() : w.getNetWeight();

        // 回写交易平台并更新单据映射
        if (StringUtils.hasText(v.getOrderNo())) {
            String orderType = "INBOUND".equals(v.getDirection()) ? "PURCHASE" : "SALES";
            tradeAdapter.writeBack(v.getOrderNo(), orderType, net);
            TradeOrderRef ref = tradeRefMapper.selectOne(Wrappers.<TradeOrderRef>lambdaQuery()
                    .eq(TradeOrderRef::getVehicleVisitId, id).last("limit 1"));
            if (ref != null && ref.getDoneQty() != null) {
                ref.setDoneQty(ref.getDoneQty().add(net));
                if (ref.getPlanQty() != null) ref.setRemainQty(ref.getPlanQty().subtract(ref.getDoneQty()));
                ref.setSyncTime(LocalDateTime.now());
                tradeRefMapper.updateById(ref);
            }
            w.setWrittenBack(1);
            weighMapper.updateById(w);
        }

        v.setStatus("RELEASED");
        v.setTimeline(appendTimeline(v.getTimeline(), "核验放行 · 净重 " + net + " 吨已回写单据"));
        visitMapper.updateById(v);
        return v;
    }

    private VehicleVisit require(Long id) {
        VehicleVisit v = visitMapper.selectById(id);
        if (v == null) throw new BizException(404, "车辆到厂记录不存在");
        return v;
    }

    private String genQueueNo(String direction) {
        Long count = visitMapper.selectCount(Wrappers.<VehicleVisit>lambdaQuery()
                .ge(VehicleVisit::getCreateTime, LocalDate.now().atStartOfDay()));
        String prefix = "INBOUND".equals(direction) ? "B" : "A";
        return prefix + String.format("%02d", (count == null ? 0 : count) + 1);
    }

    private String appendTimeline(String timeline, String step) {
        String entry = LocalDateTime.now() + " " + step;
        if (timeline == null || timeline.isBlank()) return "[\"" + entry + "\"]";
        return timeline.substring(0, timeline.length() - 1) + ",\"" + entry + "\"]";
    }
}
