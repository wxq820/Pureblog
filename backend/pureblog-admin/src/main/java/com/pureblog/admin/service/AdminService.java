package com.pureblog.admin.service;

import com.pureblog.admin.dto.ArticleAdminQueryDTO;
import com.pureblog.admin.dto.UserAdminDTO;
import com.pureblog.admin.vo.AdminCommentVO;
import com.pureblog.admin.vo.AdminUserVO;
import com.pureblog.article.vo.ArticleListVO;
import com.pureblog.common.result.PageResult;
import com.pureblog.stats.vo.DashboardVO;
import java.util.List;

public interface AdminService {

    DashboardVO getDashboard();

    PageResult<AdminUserVO> getUserList(int page, int size, String keyword);

    void updateUser(UserAdminDTO dto);

    void disableUser(Long userId);

    PageResult<ArticleListVO> getArticleList(ArticleAdminQueryDTO query);

    void offlineArticle(Long articleId);

    void deleteArticle(Long articleId);

    PageResult<AdminCommentVO> getPendingComments(int page, int size);

    void auditComment(Long commentId, boolean approve);

    void deleteComment(Long commentId);

    List<AdminCommentVO> getPendingCommentCount();

    /** 校验当前登录用户是管理员,否则抛 FORBIDDEN. */
    void requireAdmin();
}
