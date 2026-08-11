package com.pureblog.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.comment.entity.CommentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {
}
