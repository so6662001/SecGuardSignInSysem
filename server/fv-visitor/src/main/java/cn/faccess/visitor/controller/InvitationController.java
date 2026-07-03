package cn.faccess.visitor.controller;

import cn.faccess.common.exception.BizException;
import cn.faccess.common.web.R;
import cn.faccess.visitor.entity.Invitation;
import cn.faccess.visitor.mapper.InvitationMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Tag(name = "tenant-invitation", description = "预约邀请")
@RestController
@RequestMapping("/api/tenant/invitations")
public class InvitationController {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final InvitationMapper invitationMapper;

    public InvitationController(InvitationMapper invitationMapper) {
        this.invitationMapper = invitationMapper;
    }

    @Operation(summary = "邀请记录")
    @GetMapping
    public R<List<Invitation>> list(@RequestParam(required = false) String status) {
        return R.ok(invitationMapper.selectList(Wrappers.<Invitation>lambdaQuery()
                .eq(status != null, Invitation::getStatus, status)
                .orderByDesc(Invitation::getId)));
    }

    @Operation(summary = "发起邀请（生成邀请码/二维码）")
    @PostMapping
    public R<Invitation> create(@RequestBody Invitation inv) {
        inv.setId(null);
        String code = genCode();
        inv.setInviteCode(code);
        inv.setQrcodeUrl("/api/public/invite/" + code);
        inv.setStatus("PENDING");
        invitationMapper.insert(inv);
        return R.ok(inv);
    }

    @Operation(summary = "取消邀请")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        Invitation inv = invitationMapper.selectById(id);
        if (inv == null) throw new BizException(404, "邀请不存在");
        inv.setStatus("CANCELLED");
        invitationMapper.updateById(inv);
        return R.ok();
    }

    @Operation(summary = "重发邀请")
    @PostMapping("/{id}/resend")
    public R<Void> resend(@PathVariable Long id) {
        Invitation inv = invitationMapper.selectById(id);
        if (inv == null) throw new BizException(404, "邀请不存在");
        // 示例：真实场景重新触发短信/微信下发
        return R.ok();
    }

    private String genCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
        return sb.toString();
    }
}
