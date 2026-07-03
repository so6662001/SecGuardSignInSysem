package cn.faccess.visitor.service;

import cn.faccess.visitor.entity.Blacklist;
import cn.faccess.visitor.mapper.BlacklistMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 黑名单与限制。登记时命中拦截。
 */
@Service
public class BlacklistService {

    private final BlacklistMapper blacklistMapper;

    public BlacklistService(BlacklistMapper blacklistMapper) {
        this.blacklistMapper = blacklistMapper;
    }

    /** 返回命中的名单类型（BLACK/RESTRICT），未命中返回 null。 */
    public String hit(String mobile, String company) {
        if (StringUtils.hasText(mobile)) {
            Blacklist b = blacklistMapper.selectOne(Wrappers.<Blacklist>lambdaQuery()
                    .eq(Blacklist::getTargetType, "MOBILE").eq(Blacklist::getTargetValue, mobile).last("limit 1"));
            if (b != null) return b.getListType();
        }
        if (StringUtils.hasText(company)) {
            Blacklist b = blacklistMapper.selectOne(Wrappers.<Blacklist>lambdaQuery()
                    .eq(Blacklist::getTargetType, "COMPANY").eq(Blacklist::getTargetValue, company).last("limit 1"));
            if (b != null) return b.getListType();
        }
        return null;
    }

    public List<Blacklist> list() {
        return blacklistMapper.selectList(Wrappers.<Blacklist>lambdaQuery().orderByDesc(Blacklist::getId));
    }

    public Long add(Blacklist b) {
        b.setId(null);
        if (b.getListType() == null) b.setListType("BLACK");
        blacklistMapper.insert(b);
        return b.getId();
    }

    public void remove(Long id) {
        blacklistMapper.deleteById(id);
    }
}
