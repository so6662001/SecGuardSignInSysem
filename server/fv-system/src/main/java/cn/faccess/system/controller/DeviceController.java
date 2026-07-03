package cn.faccess.system.controller;

import cn.faccess.common.web.R;
import cn.faccess.system.entity.SysDevice;
import cn.faccess.system.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "tenant-device", description = "设备管理")
@RestController
@RequestMapping("/api/tenant/devices")
@PreAuthorize("hasAuthority('gate:manage') or hasRole('TENANT_ADMIN')")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "设备列表")
    @GetMapping
    public R<List<SysDevice>> list(@RequestParam(required = false) String type) {
        return R.ok(deviceService.list(type));
    }

    @Operation(summary = "注册设备（生成编号与密钥）")
    @PostMapping
    public R<SysDevice> create(@RequestBody SysDevice d) {
        return R.ok(deviceService.create(d));
    }

    @Operation(summary = "编辑设备/绑定门岗")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysDevice d) {
        deviceService.update(id, d);
        return R.ok();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return R.ok();
    }
}
