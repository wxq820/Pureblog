package com.pureblog.notification.service;

import com.pureblog.common.result.PageResult;
import com.pureblog.notification.vo.NotificationVO;

import java.util.List;

public interface NotificationService {

    PageResult<NotificationVO> getNotifications(int page, int size);

    void markAsRead(Long notificationId);

    void markAllAsRead();

    Long getUnreadCount();
}
