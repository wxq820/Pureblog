package com.pureblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.article.entity.ArticleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleMapper extends BaseMapper<ArticleDO> {
    
    @Select("SELECT * FROM pb_article WHERE id = #{id} AND deleted = 0")
    ArticleDO selectByIdSimple(Long id);
}
