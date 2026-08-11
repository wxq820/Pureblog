package com.pureblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.article.entity.TagDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<TagDO> {
}
