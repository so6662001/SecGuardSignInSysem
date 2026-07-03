package cn.faccess.system.controller;

import cn.faccess.common.web.PageResult;
import cn.faccess.common.web.R;
import cn.faccess.system.dto.MemberReq;
import cn.faccess.system.entity.SysRole;
import cn.faccess.system.entity.SysUser;
import cn.faccess.system.mapper.SysRoleMapper;
import cn.faccess.system.service.MemberService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "tenant-member", description = "成员与角色")
@RestController
@RequestMapping("/api/tenant")
public class MemberController {

    private final MemberService memberService;
    private final SysRoleMapper roleMapper;

    public MemberController(MemberService memberService, SysRoleMapper roleMapper) {
        this.memberService = memberService;
        this.roleMapper = roleMapper;
    }

    @Operation(summary = "成员列表")
    @GetMapping("/members")
    public R<PageResult<SysUser>> list(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size) {
        return R.ok(memberService.page(keyword, page, size));
    }

    @Operation(summary = "新增成员")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/members")
    public R<Long> create(@Valid @RequestBody MemberReq req) {
        return R.ok(memberService.create(req));
    }

    @Operation(summary = "编辑成员")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PutMapping("/members/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MemberReq req) {
        memberService.update(id, req);
        return R.ok();
    }

    @Operation(summary = "删除成员")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/members/{id}")
    public R<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/members/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        memberService.resetPassword(id, body == null ? null : body.get("password"));
        return R.ok();
    }

    @Operation(summary = "角色列表")
    @GetMapping("/roles")
    public R<List<SysRole>> roles() {
        return R.ok(roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().orderByAsc(SysRole::getId)));
    }
}
