package com.pureblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.user.entity.FollowDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowMapper extends BaseMapper<FollowDO> {
}
