package com.pureblog.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pureblog.article.entity.ArticleDO;
import com.pureblog.article.mapper.ArticleMapper;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.comment.dto.CommentCreateDTO;
import com.pureblog.comment.entity.CollectDO;
import com.pureblog.comment.entity.CommentDO;
import com.pureblog.comment.entity.LikeDO;
import com.pureblog.comment.mapper.CollectMapper;
import com.pureblog.comment.mapper.CommentMapper;
import com.pureblog.comment.mapper.LikeMapper;
import com.pureblog.comment.service.CommentService;
import com.pureblog.comment.vo.ArticleStatsVO;
import com.pureblog.comment.vo.CommentVO;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ArticleStatus;
import com.pureblog.common.enums.CommentStatus;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.enums.LikeTargetType;
import com.pureblog.common.event.CommentCreatedEvent;
import com.pureblog.common.event.StatsLikeArticleEvent;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;
    private final CollectMapper collectMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(CommentCreateDTO dto) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        ArticleDO article = articleMapper.selectByIdSimple(dto.getArticleId());
        if (article == null) throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        if (article.getStatus() != ArticleStatus.PUBLISHED.getCode()) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_PUBLISHED);
        }

        if (dto.getParentId() != null) {
            CommentDO parent = commentMapper.selectById(dto.getParentId());
            if (parent == null) throw new BusinessException(ErrorCode.PARENT_COMMENT_NOT_FOUND);
        }

        CommentDO comment = new CommentDO();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToId(dto.getReplyToId());
        comment.setReplyToUid(dto.getReplyToUid());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(CommentStatus.APPROVED.getCode());
        commentMapper.insert(comment);

        articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                .eq(ArticleDO::getId, dto.getArticleId())
                .setSql("comment_count = comment_count + 1"));

        CommentCreatedEvent event = new CommentCreatedEvent(
                comment.getId(),
                dto.getArticleId(),
                article.getAuthorId(),
                userId,
                dto.getContent(),
                dto.getParentId(),
                dto.getReplyToUid()
        );
        eventPublisher.publishEvent(event);

        log.info("Comment created: id={}, articleId={}, userId={}", comment.getId(), dto.getArticleId(), userId);
        return buildCommentVO(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        CommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        commentMapper.deleteById(commentId);
        articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                .eq(ArticleDO::getId, comment.getArticleId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));

        log.info("Comment deleted: id={}", commentId);
    }

    @Override
    public PageResult<CommentVO> getArticleComments(Long articleId, int page, int size) {
        Long currentUserId = LoginUserHolder.getUserId();

        LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getArticleId, articleId)
                .eq(CommentDO::getParentId, 0L)
                .eq(CommentDO::getStatus, CommentStatus.APPROVED.getCode())
                .orderByDesc(CommentDO::getCreatedAt);

        Page<CommentDO> pageResult = new Page<>((long) page, (long) size);
        Page<CommentDO> result = commentMapper.selectPage(pageResult, wrapper);

        List<CommentVO> voList = new ArrayList<>();
        for (CommentDO comment : result.getRecords()) {
            CommentVO vo = buildCommentVO(comment);
            vo.setReplies(getReplies(comment.getId(), currentUserId));
            voList.add(vo);
        }

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private List<CommentVO> getReplies(Long parentId, Long currentUserId) {
        LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getParentId, parentId)
                .eq(CommentDO::getStatus, CommentStatus.APPROVED.getCode())
                .orderByAsc(CommentDO::getCreatedAt);
        List<CommentDO> replies = commentMapper.selectList(wrapper);
        return replies.stream().map(this::buildCommentVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeDO::getUserId, userId)
                .eq(LikeDO::getTargetId, articleId)
                .eq(LikeDO::getTargetType, LikeTargetType.ARTICLE.getCode());
        if (likeMapper.selectCount(wrapper) > 0) return;

        LikeDO like = new LikeDO();
        like.setUserId(userId);
        like.setTargetId(articleId);
        like.setTargetType(LikeTargetType.ARTICLE.getCode());
        like.setCreatedAt(java.time.LocalDateTime.now());
        likeMapper.insert(like);

        articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                .eq(ArticleDO::getId, articleId)
                .setSql("like_count = like_count + 1"));

        ArticleDO article = articleMapper.selectByIdSimple(articleId);
        eventPublisher.publishEvent(new StatsLikeArticleEvent(
                articleId,
                article != null ? article.getAuthorId() : null,
                userId
        ));

        log.info("User {} liked article {}", userId, articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeDO::getUserId, userId)
                .eq(LikeDO::getTargetId, articleId)
                .eq(LikeDO::getTargetType, LikeTargetType.ARTICLE.getCode());
        int deleted = likeMapper.delete(wrapper);

        if (deleted > 0) {
            articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                    .eq(ArticleDO::getId, articleId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            log.info("User {} unliked article {}", userId, articleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        LambdaQueryWrapper<CollectDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectDO::getUserId, userId).eq(CollectDO::getArticleId, articleId);
        if (collectMapper.selectCount(wrapper) > 0) return;

        CollectDO collect = new CollectDO();
        collect.setUserId(userId);
        collect.setArticleId(articleId);
        collect.setCreatedAt(java.time.LocalDateTime.now());
        collectMapper.insert(collect);

        articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                .eq(ArticleDO::getId, articleId)
                .setSql("collect_count = collect_count + 1"));

        log.info("User {} collected article {}", userId, articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        LambdaQueryWrapper<CollectDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectDO::getUserId, userId).eq(CollectDO::getArticleId, articleId);
        int deleted = collectMapper.delete(wrapper);

        if (deleted > 0) {
            articleMapper.update(null, new LambdaUpdateWrapper<ArticleDO>()
                    .eq(ArticleDO::getId, articleId)
                    .setSql("collect_count = GREATEST(collect_count - 1, 0)"));
            log.info("User {} uncollected article {}", userId, articleId);
        }
    }

    @Override
    public ArticleStatsVO getArticleStats(Long articleId) {
        Long currentUserId = LoginUserHolder.getUserId();
        ArticleDO article = articleMapper.selectByIdSimple(articleId);
        if (article == null) throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);

        boolean isLiked = false;
        boolean isCollected = false;
        if (currentUserId != null) {
            isLiked = likeMapper.selectCount(new LambdaQueryWrapper<LikeDO>()
                    .eq(LikeDO::getUserId, currentUserId)
                    .eq(LikeDO::getTargetId, articleId)
                    .eq(LikeDO::getTargetType, LikeTargetType.ARTICLE.getCode())) > 0;
            isCollected = collectMapper.selectCount(new LambdaQueryWrapper<CollectDO>()
                    .eq(CollectDO::getUserId, currentUserId)
                    .eq(CollectDO::getArticleId, articleId)) > 0;
        }

        return ArticleStatsVO.builder()
                .articleId(articleId)
                .likeCount(article.getLikeCount())
                .collectCount(article.getCollectCount())
                .commentCount(article.getCommentCount())
                .isLiked(isLiked)
                .isCollected(isCollected)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long commentId, boolean approve) {
        int status = approve ? CommentStatus.APPROVED.getCode() : CommentStatus.REJECTED.getCode();
        commentMapper.update(null, new LambdaUpdateWrapper<CommentDO>()
                .eq(CommentDO::getId, commentId)
                .set(CommentDO::getStatus, status));
        log.info("Comment {} audit: {}", commentId, approve ? "approved" : "rejected");
    }

    private CommentVO buildCommentVO(CommentDO comment) {
        UserDO user = userMapper.selectById(comment.getUserId());
        UserDO replyToUser = comment.getReplyToUid() != null ? userMapper.selectById(comment.getReplyToUid()) : null;

        Long currentUserId = LoginUserHolder.getUserId();
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = likeMapper.selectCount(new LambdaQueryWrapper<LikeDO>()
                    .eq(LikeDO::getUserId, currentUserId)
                    .eq(LikeDO::getTargetId, comment.getId())
                    .eq(LikeDO::getTargetType, LikeTargetType.COMMENT.getCode())) > 0;
        }

        return CommentVO.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .username(user != null ? user.getUsername() : null)
                .nickname(user != null ? user.getNickname() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .parentId(comment.getParentId() == 0 ? null : comment.getParentId())
                .replyToId(comment.getReplyToId())
                .replyToUid(comment.getReplyToUid())
                .replyToNickname(replyToUser != null ? replyToUser.getNickname() : null)
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .status(CommentStatus.of(comment.getStatus()).getDesc())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
