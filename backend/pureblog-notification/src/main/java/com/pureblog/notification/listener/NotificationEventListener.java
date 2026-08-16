package com.pureblog.notification.listener;

import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.common.enums.NotificationType;
import com.pureblog.common.event.CommentCreatedEvent;
import com.pureblog.common.event.FollowCreatedEvent;
import com.pureblog.common.event.StatsLikeArticleEvent;
import com.pureblog.notification.entity.NotificationDO;
import com.pureblog.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @EventListener
    @Transactional
    public void onCommentCreated(CommentCreatedEvent event) {
        if (event.articleAuthorId() == null) return;

        Long targetUserId = event.parentId() != null && event.parentId() != 0
                ? event.replyToUid() != null ? event.replyToUid() : null
                : event.articleAuthorId();

        if (targetUserId == null || targetUserId.equals(event.commentUserId())) {
            return;
        }

        UserDO commenter = userMapper.selectById(event.commentUserId());
        String nickname = commenter != null ? commenter.getNickname() : "某用户";

        String title;
        String content;
        NotificationType type;

        if (event.parentId() != null && event.parentId() != 0) {
            title = "收到新回复";
            content = nickname + " 回复了你的评论: " + truncate(event.content(), 50);
            type = NotificationType.REPLY;
        } else {
            title = "收到新评论";
            content = nickname + " 评论了你的文章: " + truncate(event.content(), 50);
            type = NotificationType.COMMENT;
        }

        save(targetUserId, type, title, content, event.articleId(), 1);
        log.info("Notification sent: userId={}, type={}, articleId={}", targetUserId, type, event.articleId());
    }

    @EventListener
    @Transactional
    public void onFollow(FollowCreatedEvent event) {
        UserDO follower = userMapper.selectById(event.followerId());
        String followerName = follower != null ? follower.getNickname() : "某用户";

        save(event.followingId(), NotificationType.FOLLOW,
                "收到新关注", followerName + " 关注了你", event.followerId(), 3);

        log.info("Follow notification sent: follower={}, following={}", event.followerId(), event.followingId());
    }

    @EventListener
    @Transactional
    public void onStatsLike(StatsLikeArticleEvent event) {
        if (event.articleAuthorId() == null) return;
        if (event.userId() != null && event.userId().equals(event.articleAuthorId())) {
            return;
        }
        UserDO liker = userMapper.selectById(event.userId());
        String likerName = liker != null ? liker.getNickname() : "某用户";

        save(event.articleAuthorId(), NotificationType.LIKE,
                "收到点赞", likerName + " 点赞了你的文章", event.articleId(), 1);
    }

    private void save(Long userId, NotificationType type, String title, String content,
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

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}