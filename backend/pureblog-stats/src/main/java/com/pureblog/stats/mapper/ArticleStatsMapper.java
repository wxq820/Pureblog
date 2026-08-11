package com.pureblog.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.stats.entity.ArticleStatsDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleStatsMapper extends BaseMapper<ArticleStatsDO> {
}
