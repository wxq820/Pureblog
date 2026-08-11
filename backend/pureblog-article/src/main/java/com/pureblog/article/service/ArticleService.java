package com.pureblog.article.service;

import com.pureblog.article.dto.*;
import com.pureblog.article.vo.*;
import com.pureblog.common.result.PageResult;
import java.util.List;

public interface ArticleService {
    
    ArticleDetailVO createArticle(ArticleCreateDTO dto);
    
    ArticleDetailVO updateArticle(ArticleUpdateDTO dto);
    
    void publishArticle(ArticlePublishDTO dto);
    
    void offlineArticle(Long articleId);
    
    void deleteArticle(Long articleId);
    
    ArticleDetailVO getArticleDetail(Long articleId);
    
    PageResult<ArticleListVO> getArticleList(ArticleQueryDTO query);
    
    List<ArticleListVO> getHotArticles(int limit);
    
    List<ArticleListVO> getFeaturedArticles(int limit);
    
    List<ArticleListVO> getAuthorArticles(Long authorId, int page, int size);
    
    List<CategoryVO> getAllCategories();
    
    List<TagVO> getAllTags();
    
    void incrementViewCount(Long articleId);
    
    void rebuildSearchIndex(Long articleId);
}
