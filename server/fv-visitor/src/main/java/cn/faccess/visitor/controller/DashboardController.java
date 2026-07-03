package cn.faccess.visitor.controller;

import cn.faccess.common.web.R;
import cn.faccess.visitor.entity.VisitRecord;
import cn.faccess.visitor.mapper.VisitRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "tenant-dashboard", description = "数据看板")
@RestController
@RequestMapping("/api/tenant/dashboard")
public class DashboardController {

    private final VisitRecordMapper recordMapper;

    public DashboardController(VisitRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Operation(summary = "概览指标（今日到访/在场/待审）")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        Map<String, Object> m = new HashMap<>();
        m.put("todayVisits", recordMapper.selectCount(Wrappers.<VisitRecord>lambdaQuery()
                .ge(VisitRecord::getCreateTime, dayStart)));
        m.put("onsite", recordMapper.selectCount(Wrappers.<VisitRecord>lambdaQuery()
                .in(VisitRecord::getStatus, "ONSITE", "OVERSTAY")));
        m.put("pending", recordMapper.selectCount(Wrappers.<VisitRecord>lambdaQuery()
                .eq(VisitRecord::getStatus, "PENDING")));
        return R.ok(m);
    }

    @Operation(summary = "近7日到访趋势")
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            Long c = recordMapper.selectCount(Wrappers.<VisitRecord>lambdaQuery()
                    .ge(VisitRecord::getCreateTime, day.atStartOfDay())
                    .lt(VisitRecord::getCreateTime, day.plusDays(1).atStartOfDay()));
            Map<String, Object> m = new HashMap<>();
            m.put("date", day.toString());
            m.put("count", c);
            list.add(m);
        }
        return R.ok(list);
    }
}
