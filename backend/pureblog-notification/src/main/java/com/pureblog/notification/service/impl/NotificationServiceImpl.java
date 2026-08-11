package com.pureblog.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.NotificationType;
import com.pureblog.common.result.PageResult;
import com.pureblog.notification.entity.NotificationDO;
import com.pureblog.notification.event.*;
import com.pureblog.notification.mapper.NotificationMapper;
import com.pureblog.notification.service.NotificationService;
import com.pureblog.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public void handleCommentCreated(CommentEvent event) {
        if (event.getArticleAuthorId() == null) return;
        
        Long targetUserId = event.getParentId() != null && event.getParentId() != 0
                ? event.getReplyToUid() != null ? event.getReplyToUid() : null
                : event.getArticleAuthorId();

        if (targetUserId == null || targetUserId.equals(event.getCommentUserId())) {
            return;
        }

        UserDO commenter = userMapper.selectById(event.getCommentUserId());
        String nickname = commenter != null ? commenter.getNickname() : "某用户";

        String title;
        String content;
        NotificationType type;

        if (event.getParentId() != null && event.getParentId() != 0) {
            title = "收到新回复";
            content = nickname + " 回复了你的评论: " + truncate(event.getContent(), 50);
            type = NotificationType.REPLY;
        } else {
            title = "收到新评论";
            content = nickname + " 评论了你的文章: " + truncate(event.getContent(), 50);
            type = NotificationType.COMMENT;
        }

        saveNotification(targetUserId, type, title, content, event.getArticleId(), 1);
        log.info("Notification sent: userId={}, type={}, articleId={}", targetUserId, type, event.getArticleId());
    }

    @Override
    public void handleArticlePublished(ArticleEvent event) {
        log.info("Article published: id={}, author={}", event.getArticleId(), event.getAuthorId());
    }

    @Override
    public void handleFollowEvent(FollowEvent event) {
        UserDO follower = userMapper.selectById(event.getFollowerId());
        String followerName = follower != null ? follower.getNickname() : "某用户";

        String content = followerName + " 关注了你";
        
        saveNotification(event.getFollowingId(), NotificationType.FOLLOW,
                "收到新关注", content, event.getFollowerId(), 3);
        
        log.info("Follow notification sent: follower={}, following={}", event.getFollowerId(), event.getFollowingId());
    }

    @Override
    public void handleStatsEvent(StatsEvent event) {
        if ("LIKE_ARTICLE".equals(event.getEventType()) && event.getArticleAuthorId() != null) {
            if (event.getUserId() != null && event.getUserId().equals(event.getArticleAuthorId())) {
                return;
            }
            UserDO liker = userMapper.selectById(event.getUserId());
            String likerName = liker != null ? liker.getNickname() : "某用户";
            
            String content = likerName + " 点赞了你的文章";
            saveNotification(event.getArticleAuthorId(), NotificationType.LIKE,
                    "收到点赞", content, event.getArticleId(), 1);
        }
    }

    @Override
    public PageResult<NotificationVO> getNotifications(int page, int size) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationDO::getUserId, userId)
                .orderByDesc(NotificationDO::getCreatedAt);

        Page<NotificationDO> pageResult = new Page<>((long) page, (long) size);
        Page<NotificationDO> result = notificationMapper.selectPage(pageResult, wrapper);

        List<NotificationVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public void markAsRead(Long notificationId) {
        LambdaUpdateWrapper<NotificationDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotificationDO::getId, notificationId)
               .set(NotificationDO::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    public void markAllAsRead() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) return;

        LambdaUpdateWrapper<NotificationDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotificationDO::getUserId, userId)
               .eq(NotificationDO::getIsRead, 0)
               .set(NotificationDO::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) return 0L;

        LambdaQueryWrapper<NotificationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationDO::getUserId, userId)
                .eq(NotificationDO::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    private void saveNotification(Long userId, NotificationType type, String title, String content,
                                  Long relatedId, Integer relatedType) {
        NotificationDO notification = new NotificationDO();
        notification.setUserId(userId);
        notification.setType(type.getCode());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private NotificationVO toVO(NotificationDO notification) {
        NotificationType[] values = NotificationType.values();
        NotificationType typeEnum = notification.getType() >= 1 && notification.getType() <= values.length
                ? values[notification.getType() - 1] : null;
        return NotificationVO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .typeDesc(typeEnum != null ? typeEnum.getDesc() : "未知")
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .isRead(notification.getIsRead() == 1)
                .createdAt(notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null)
                .relativeTime(notification.getCreatedAt() != null
                        ? getRelativeTime(notification.getCreatedAt()) : null)
                .build();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }

    private String getRelativeTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        long seconds = java.time.Duration.between(dateTime, java.time.LocalDateTime.now()).getSeconds();
        if (seconds < 60) return "刚刚";
        if (seconds < 3600) return (seconds / 60) + "分钟前";
        if (seconds < 86400) return (seconds / 3600) + "小时前";
        if (seconds < 2592000) return (seconds / 86400) + "天前";
        return (seconds / 2592000) + "个月前";
    }
}
