package cn.faccess.steel.service;

import cn.faccess.common.exception.BizException;
import cn.faccess.steel.dto.WeighReq;
import cn.faccess.steel.entity.VehicleVisit;
import cn.faccess.steel.entity.WeighRecord;
import cn.faccess.steel.mapper.VehicleVisitMapper;
import cn.faccess.steel.mapper.WeighRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 磅房过磅：记录皮重/毛重，计算净重与结算净重（扣杂），防作弊校验。
 */
@Service
public class WeighService {

    /** 超载阈值（吨），示意值。 */
    private static final BigDecimal OVERLOAD = new BigDecimal("49");

    private final WeighRecordMapper weighMapper;
    private final VehicleVisitMapper visitMapper;

    public WeighService(WeighRecordMapper weighMapper, VehicleVisitMapper visitMapper) {
        this.weighMapper = weighMapper;
        this.visitMapper = visitMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public WeighRecord weigh(WeighReq req) {
        VehicleVisit visit = visitMapper.selectById(req.getVehicleVisitId());
        if (visit == null) throw new BizException(404, "车辆到厂记录不存在");

        WeighRecord w = weighMapper.selectOne(Wrappers.<WeighRecord>lambdaQuery()
                .eq(WeighRecord::getVehicleVisitId, req.getVehicleVisitId()).last("limit 1"));
        if (w == null) {
            w = new WeighRecord();
            w.setVehicleVisitId(req.getVehicleVisitId());
            w.setDeviceId(req.getDeviceId());
            w.setDeductRate(visit.getDeductRate());
        }

        if ("TARE".equalsIgnoreCase(req.getWeighType())) {
            w.setTareWeight(req.getWeight());
            w.setTareTime(LocalDateTime.now());
        } else if ("GROSS".equalsIgnoreCase(req.getWeighType())) {
            w.setGrossWeight(req.getWeight());
            w.setGrossTime(LocalDateTime.now());
            if (req.getWeight() != null && req.getWeight().compareTo(OVERLOAD) > 0) {
                w.setAbnormal("OVERLOAD");
            }
        } else {
            throw new BizException(400, "称重类型无效");
        }

        // 皮毛齐全则计算净重与结算净重
        if (w.getTareWeight() != null && w.getGrossWeight() != null) {
            BigDecimal net = w.getGrossWeight().subtract(w.getTareWeight());
            w.setNetWeight(net);
            BigDecimal rate = w.getDeductRate() == null ? BigDecimal.ZERO : w.getDeductRate();
            BigDecimal settle = net.multiply(BigDecimal.ONE.subtract(rate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
                    .setScale(3, RoundingMode.HALF_UP);
            w.setSettleWeight(settle);
        }

        if (w.getId() == null) weighMapper.insert(w);
        else weighMapper.updateById(w);

        // 更新车辆到厂状态
        visit.setStatus(w.getNetWeight() != null ? "REWEIGH" : "WEIGHING");
        visitMapper.updateById(visit);
        return w;
    }

    public List<WeighRecord> records() {
        return weighMapper.selectList(Wrappers.<WeighRecord>lambdaQuery().orderByDesc(WeighRecord::getId));
    }
}
