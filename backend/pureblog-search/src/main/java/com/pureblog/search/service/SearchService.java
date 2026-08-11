package com.pureblog.search.service;

import com.pureblog.search.dto.SearchRequest;
import com.pureblog.search.event.ArticleEvent;
import com.pureblog.search.vo.SearchResult;

public interface SearchService {
    
    SearchResult search(SearchRequest request);
    
    void indexArticle(ArticleEvent event);
    
    void deleteArticleIndex(Long articleId);
    
    void rebuildArticleIndex(ArticleEvent event);
    
    void createIndex();
    
    void rebuildAllIndex();
}
