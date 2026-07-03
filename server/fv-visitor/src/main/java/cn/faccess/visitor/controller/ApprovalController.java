package cn.faccess.visitor.controller;

import cn.faccess.common.tenant.TenantContext;
import cn.faccess.common.web.R;
import cn.faccess.visitor.entity.VisitApproval;
import cn.faccess.visitor.mapper.VisitApprovalMapper;
import cn.faccess.visitor.service.VisitService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "tenant-approval", description = "审批")
@RestController
@RequestMapping("/api/tenant/approvals")
public class ApprovalController {

    private final VisitService visitService;
    private final VisitApprovalMapper approvalMapper;

    public ApprovalController(VisitService visitService, VisitApprovalMapper approvalMapper) {
        this.visitService = visitService;
        this.approvalMapper = approvalMapper;
    }

    @Operation(summary = "待处理审批列表")
    @GetMapping("/pending")
    public R<List<VisitApproval>> pending() {
        return R.ok(approvalMapper.selectList(Wrappers.<VisitApproval>lambdaQuery()
                .in(VisitApproval::getStatus, "WAITING", "ESCALATED")
                .orderByDesc(VisitApproval::getId)));
    }

    @Operation(summary = "审批决策（同意/拒绝）")
    @PostMapping("/{id}/decision")
    public R<Void> decision(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        String validScope = (String) body.getOrDefault("validScope", "ONCE");
        visitService.decide(id, approve, validScope, "APP", TenantContext.getUserId());
        return R.ok();
    }
}
