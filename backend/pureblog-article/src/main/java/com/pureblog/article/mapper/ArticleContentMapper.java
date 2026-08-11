package com.pureblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.article.entity.ArticleContentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleContentMapper extends BaseMapper<ArticleContentDO> {
}
