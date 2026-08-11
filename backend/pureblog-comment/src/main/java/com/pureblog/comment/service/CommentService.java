package com.pureblog.comment.service;

import com.pureblog.comment.dto.CommentCreateDTO;
import com.pureblog.comment.vo.CommentVO;
import com.pureblog.comment.vo.ArticleStatsVO;
import com.pureblog.common.result.PageResult;

import java.util.List;

public interface CommentService {

    CommentVO createComment(CommentCreateDTO dto);

    void deleteComment(Long commentId);

    PageResult<CommentVO> getArticleComments(Long articleId, int page, int size);

    void likeArticle(Long articleId);

    void unlikeArticle(Long articleId);

    void collectArticle(Long articleId);

    void uncollectArticle(Long articleId);

    ArticleStatsVO getArticleStats(Long articleId);

    void auditComment(Long commentId, boolean approve);
}
