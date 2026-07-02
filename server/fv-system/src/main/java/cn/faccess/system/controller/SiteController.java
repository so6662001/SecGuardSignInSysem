package cn.faccess.system.controller;

import cn.faccess.common.web.R;
import cn.faccess.system.entity.SysSite;
import cn.faccess.system.mapper.SysGateMapper;
import cn.faccess.system.mapper.SysSiteMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "tenant-site", description = "厂区/多厂区")
@RestController
@RequestMapping("/api/tenant/sites")
public class SiteController {

    private final SysSiteMapper siteMapper;
    private final SysGateMapper gateMapper;

    public SiteController(SysSiteMapper siteMapper, SysGateMapper gateMapper) {
        this.siteMapper = siteMapper;
        this.gateMapper = gateMapper;
    }

    @Operation(summary = "厂区列表")
    @GetMapping
    public R<List<SysSite>> list() {
        return R.ok(siteMapper.selectList(Wrappers.<SysSite>lambdaQuery().orderByAsc(SysSite::getId)));
    }

    @Operation(summary = "新增厂区")
    @PostMapping
    public R<Long> create(@RequestBody SysSite site) {
        site.setId(null);
        if (site.getStatus() == null) site.setStatus(1);
        siteMapper.insert(site);
        return R.ok(site.getId());
    }

    @Operation(summary = "集团总览汇总")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Map<String, Object> m = new HashMap<>();
        m.put("siteCount", siteMapper.selectCount(null));
        m.put("gateCount", gateMapper.selectCount(null));
        m.put("sites", siteMapper.selectList(Wrappers.<SysSite>lambdaQuery().orderByAsc(SysSite::getId)));
        return R.ok(m);
    }
}
