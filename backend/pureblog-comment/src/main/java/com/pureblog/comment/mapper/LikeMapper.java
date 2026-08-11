package com.pureblog.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.comment.entity.LikeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper extends BaseMapper<LikeDO> {
}
