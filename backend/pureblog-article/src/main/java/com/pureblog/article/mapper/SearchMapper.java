package com.pureblog.article.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MySQL LIKE 版搜索的 Mapper。
 *
 * <p>手写 SQL 的原因: 关键词需同时匹配标题 / 摘要 / 正文 / 标签名, 涉及
 * pb_article ⋈ pb_article_content ⋈ pb_article_tag ⋈ pb_tag 四表 JOIN,
 * LambdaQueryWrapper 表达不了; 且分页用显式 LIMIT/OFFSET, 不依赖
 * MyBatis-Plus 的 PaginationInnerInterceptor (本项目未注册该拦截器)。
 */
@Mapper
public interface SearchMapper {

    /**
     * 按关键词检索已发布文章, 返回分页后的行.
     *
     * @param keyword    关键词, 已由 Service 层做过 HTML 转义与 LIKE 通配符处理
     * @param categoryId 分类过滤, null 表示不过滤
     * @param tagId      标签过滤, null 表示不过滤
     * @param sortBy     排序字段 (relevance / viewCount / likeCount / publishedAt)
     * @param sortOrder  asc / desc
     * @param offset     偏移量
     * @param limit      每页条数
     */
    List<SearchRow> search(@Param("keyword") String keyword,
                            @Param("categoryId") Long categoryId,
                            @Param("tagId") Long tagId,
                            @Param("sortBy") String sortBy,
                            @Param("sortOrder") String sortOrder,
                            @Param("offset") long offset,
                            @Param("limit") int limit);

    /**
     * 与 {@link #search} 相同条件的总数, 用于分页元信息.
     */
    long count(@Param("keyword") String keyword,
               @Param("categoryId") Long categoryId,
               @Param("tagId") Long tagId);
}
