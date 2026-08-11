package com.pureblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.article.entity.ArticleTagDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {
}
