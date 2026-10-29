package com.pureblog.article.service;

import com.pureblog.article.dto.SearchRequest;
import com.pureblog.article.vo.SearchResult;

public interface SearchService {

    /**
     * 全文检索 (MySQL LIKE 实现).
     *
     * @param request 检索条件, keyword 匹配标题 / 摘要 / 正文 / 标签名
     * @return 分页结果, 字段与历史 ES 版本保持一致, 前端无需改动
     */
    SearchResult search(SearchRequest request);
}
