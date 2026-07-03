package cn.faccess.visitor.controller;

import cn.faccess.common.web.R;
import cn.faccess.visitor.entity.Blacklist;
import cn.faccess.visitor.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "tenant-blacklist", description = "黑名单与限制")
@RestController
@RequestMapping("/api/tenant/blacklist")
public class BlacklistController {

    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Operation(summary = "黑名单列表")
    @GetMapping
    public R<List<Blacklist>> list() {
        return R.ok(blacklistService.list());
    }

    @Operation(summary = "新增黑名单/限制")
    @PostMapping
    public R<Long> add(@RequestBody Blacklist b) {
        return R.ok(blacklistService.add(b));
    }

    @Operation(summary = "移除")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        blacklistService.remove(id);
        return R.ok();
    }
}
