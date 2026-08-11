package com.pureblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.article.entity.CategoryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {
}
