package com.pureblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pureblog.admin.dto.ArticleAdminQueryDTO;
import com.pureblog.admin.dto.UserAdminDTO;
import com.pureblog.admin.service.AdminService;
import com.pureblog.admin.vo.AdminCommentVO;
import com.pureblog.admin.vo.AdminUserVO;
import com.pureblog.article.entity.ArticleDO;
import com.pureblog.article.mapper.ArticleMapper;
import com.pureblog.article.vo.ArticleListVO;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.comment.entity.CommentDO;
import com.pureblog.comment.mapper.CommentMapper;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ArticleStatus;
import com.pureblog.common.enums.CommentStatus;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.enums.UserRole;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.PageResult;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.stats.service.StatsService;
import com.pureblog.stats.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final StatsService statsService;

    @Override
    public DashboardVO getDashboard() {
        return statsService.getDashboardStats();
    }

    @Override
    public PageResult<AdminUserVO> getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(UserDO::getUsername, keyword).or().like(UserDO::getNickname, keyword));
        }
        wrapper.orderByDesc(UserDO::getCreatedAt);

        Page<UserDO> pageResult = new Page<>((long) page, (long) size);
        Page<UserDO> result = userMapper.selectPage(pageResult, wrapper);

        List<AdminUserVO> voList = result.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserAdminDTO dto) {
        checkAdmin();
        UserDO user = userMapper.selectById(dto.getUserId());
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        userMapper.updateById(user);
        log.info("Admin updated user: userId={}", dto.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        checkAdmin();
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .set(UserDO::getStatus, 2));
        log.info("Admin disabled user: userId={}", userId);
    }

    @Override
    public PageResult<ArticleListVO> getArticleList(ArticleAdminQueryDTO query) {
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        if (query.getAuthorId() != null) wrapper.eq(ArticleDO::getAuthorId, query.getAuthorId());
        if (query.getStatus() != null) wrapper.eq(ArticleDO::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(ArticleDO::getTitle, query.getKeyword());
        }
        wrapper.orderByDesc(ArticleDO::getCreatedAt);

        Page<ArticleDO> pageResult = new Page<>((long) query.getPage(), (long) query.getSize());
        Page<ArticleDO> result = articleMapper.selectPage(pageResult, wrapper);

        List<ArticleListVO> voList = result.getRecords().stream().map(this::toArticleVO).collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineArticle(Long articleId) {
        checkAdmin();
        articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                .eq(ArticleDO::getId, articleId)
                .set(ArticleDO::getStatus, ArticleStatus.OFFLINE.getCode()));
        log.info("Admin offline article: articleId={}", articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId) {
        checkAdmin();
        articleMapper.deleteById(articleId);
        log.info("Admin deleted article: articleId={}", articleId);
    }

    @Override
    public PageResult<AdminCommentVO> getPendingComments(int page, int size) {
        LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getStatus, CommentStatus.PENDING.getCode())
                .orderByAsc(CommentDO::getCreatedAt);

        Page<CommentDO> pageResult = new Page<>((long) page, (long) size);
        Page<CommentDO> result = commentMapper.selectPage(pageResult, wrapper);

        List<AdminCommentVO> voList = new ArrayList<>();
        for (CommentDO comment : result.getRecords()) {
            ArticleDO article = articleMapper.selectByIdSimple(comment.getArticleId());
            UserDO user = userMapper.selectById(comment.getUserId());
            voList.add(AdminCommentVO.builder()
                    .id(comment.getId())
                    .articleId(comment.getArticleId())
                    .articleTitle(article != null ? article.getTitle() : null)
                    .userId(comment.getUserId())
                    .username(user != null ? user.getUsername() : null)
                    .nickname(user != null ? user.getNickname() : null)
                    .content(comment.getContent())
                    .status(CommentStatus.of(comment.getStatus()).getDesc())
                    .createdAt(comment.getCreatedAt())
                    .build());
        }
        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long commentId, boolean approve) {
        checkAdmin();
        int status = approve ? CommentStatus.APPROVED.getCode() : CommentStatus.REJECTED.getCode();
        commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                .eq(CommentDO::getId, commentId)
                .set(CommentDO::getStatus, status));
        log.info("Admin audited comment: commentId={}, approved={}", commentId, approve);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        checkAdmin();
        commentMapper.deleteById(commentId);
        log.info("Admin deleted comment: commentId={}", commentId);
    }

    @Override
    public List<AdminCommentVO> getPendingCommentCount() {
        long count = commentMapper.selectCount(new LambdaQueryWrapper<CommentDO>()
                .eq(CommentDO::getStatus, CommentStatus.PENDING.getCode()));
        return List.of(AdminCommentVO.builder()
                .id(count)
                .build());
    }

    private void checkAdmin() {
        if (LoginUserHolder.get() == null || !LoginUserHolder.get().isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private AdminUserVO toUserVO(UserDO user) {
        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(UserRole.of(user.getRole()).getDesc())
                .status(user.getStatus() == 1 ? "正常" : "禁用")
                .followerCount(user.getFollowerCount())
                .articleCount(user.getArticleCount())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private ArticleListVO toArticleVO(ArticleDO article) {
        UserDO author = userMapper.selectById(article.getAuthorId());
        return ArticleListVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .coverUrl(article.getCoverUrl())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .isFeatured(article.getIsFeatured() == 1)
                .isTop(article.getIsTop() == 1)
                .publishedAt(article.getPublishedAt())
                .authorName(author != null ? author.getNickname() : null)
                .authorId(article.getAuthorId())
                .build();
    }
}
