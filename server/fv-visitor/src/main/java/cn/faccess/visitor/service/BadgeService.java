package cn.faccess.visitor.service;

import cn.faccess.visitor.entity.VisitorBadge;
import cn.faccess.visitor.mapper.VisitorBadgeMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

/**
 * 访客牌分配与回收。优先从可用牌池取号，无则新建。
 */
@Service
public class BadgeService {

    private final VisitorBadgeMapper badgeMapper;

    public BadgeService(VisitorBadgeMapper badgeMapper) {
        this.badgeMapper = badgeMapper;
    }

    public String allocate(Long recordId) {
        VisitorBadge badge = badgeMapper.selectOne(Wrappers.<VisitorBadge>lambdaQuery()
                .eq(VisitorBadge::getStatus, 0).orderByAsc(VisitorBadge::getBadgeNo).last("limit 1"));
        if (badge == null) {
            Long count = badgeMapper.selectCount(null);
            badge = new VisitorBadge();
            badge.setBadgeNo(String.format("VIP-%03d", (count == null ? 0 : count) + 1));
            badge.setStatus(1);
            badge.setRecordId(recordId);
            badgeMapper.insert(badge);
        } else {
            badge.setStatus(1);
            badge.setRecordId(recordId);
            badgeMapper.updateById(badge);
        }
        return badge.getBadgeNo();
    }

    public void release(Long recordId) {
        VisitorBadge badge = badgeMapper.selectOne(Wrappers.<VisitorBadge>lambdaQuery()
                .eq(VisitorBadge::getRecordId, recordId).last("limit 1"));
        if (badge != null) {
            badge.setStatus(0);
            badge.setRecordId(null);
            badgeMapper.updateById(badge);
        }
    }
}
