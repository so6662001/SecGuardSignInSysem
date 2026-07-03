package cn.faccess.system.controller;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.web.R;
import cn.faccess.system.entity.SysGate;
import cn.faccess.system.mapper.SysGateMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "tenant-gate", description = "门岗管理")
@RestController
@RequestMapping("/api/tenant/gates")
public class GateController {

    private final SysGateMapper gateMapper;

    public GateController(SysGateMapper gateMapper) {
        this.gateMapper = gateMapper;
    }

    @Operation(summary = "门岗列表")
    @GetMapping
    public R<List<SysGate>> list() {
        return R.ok(gateMapper.selectList(Wrappers.<SysGate>lambdaQuery().orderByAsc(SysGate::getId)));
    }

    @Operation(summary = "新增门岗")
    @PostMapping
    public R<Long> create(@RequestBody SysGate gate) {
        String code = "G" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        gate.setId(null);
        gate.setGateCode(code);
        gate.setQrcodeUrl("/api/public/gate/" + code + "/template");
        if (gate.getStatus() == null) gate.setStatus(1);
        gateMapper.insert(gate);
        return R.ok(gate.getId());
    }

    @Operation(summary = "编辑门岗")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysGate gate) {
        SysGate exist = gateMapper.selectById(id);
        if (exist == null) throw new BizException(404, "门岗不存在");
        gate.setId(id);
        gate.setGateCode(exist.getGateCode());
        gate.setQrcodeUrl(exist.getQrcodeUrl());
        gateMapper.updateById(gate);
        return R.ok();
    }

    @Operation(summary = "删除门岗")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        gateMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "获取门岗自助登记二维码")
    @GetMapping("/{id}/qrcode")
    public R<Map<String, String>> qrcode(@PathVariable Long id) {
        SysGate gate = gateMapper.selectById(id);
        if (gate == null) throw new BizException(404, "门岗不存在");
        return R.ok(Map.of("gateCode", gate.getGateCode(), "qrcodeUrl", gate.getQrcodeUrl()));
    }
}
