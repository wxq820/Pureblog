package com.pureblog.notification.service;

import com.pureblog.common.result.PageResult;
import com.pureblog.notification.event.*;
import com.pureblog.notification.vo.NotificationVO;
import java.util.List;

public interface NotificationService {
    
    void handleCommentCreated(CommentEvent event);
    
    void handleArticlePublished(ArticleEvent event);
    
    void handleFollowEvent(FollowEvent event);
    
    void handleStatsEvent(StatsEvent event);
    
    PageResult<NotificationVO> getNotifications(int page, int size);
    
    void markAsRead(Long notificationId);
    
    void markAllAsRead();
    
    Long getUnreadCount();
}
