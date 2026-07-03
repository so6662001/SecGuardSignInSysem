package cn.faccess.steel.controller;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.web.R;
import cn.faccess.steel.entity.Carrier;
import cn.faccess.steel.mapper.CarrierMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "steel-carrier", description = "承运商与车辆")
@RestController
@RequestMapping("/api/tenant/steel/carriers")
@PreAuthorize("hasAuthority('steel:carrier') or hasRole('TENANT_ADMIN')")
public class CarrierController {

    private static final Logger log = LoggerFactory.getLogger(CarrierController.class);
    private final CarrierMapper carrierMapper;

    public CarrierController(CarrierMapper carrierMapper) {
        this.carrierMapper = carrierMapper;
    }

    @Operation(summary = "承运商列表")
    @GetMapping
    public R<List<Carrier>> list() {
        return R.ok(carrierMapper.selectList(Wrappers.<Carrier>lambdaQuery().orderByDesc(Carrier::getId)));
    }

    @Operation(summary = "新增承运商")
    @PostMapping
    public R<Long> create(@RequestBody Carrier c) {
        c.setId(null);
        if (c.getStatus() == null) c.setStatus("NORMAL");
        if (c.getTradeJoined() == null) c.setTradeJoined(0);
        if (c.getWhitelist() == null) c.setWhitelist(0);
        carrierMapper.insert(c);
        return R.ok(c.getId());
    }

    @Operation(summary = "邀请入驻交易平台运力池")
    @PostMapping("/{id}/invite-trade")
    public R<Void> inviteTrade(@PathVariable Long id) {
        Carrier c = carrierMapper.selectById(id);
        if (c == null) throw new BizException(404, "承运商不存在");
        c.setTradeJoined(1);
        carrierMapper.updateById(c);
        log.info("[运力池] 已邀请承运商 {} 入驻交易平台", c.getName());
        return R.ok();
    }
}
