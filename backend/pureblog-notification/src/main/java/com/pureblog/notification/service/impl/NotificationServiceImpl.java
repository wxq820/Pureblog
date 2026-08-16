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