package com.pureblog.notification.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.result.PageResult;
import com.pureblog.notification.service.NotificationService;
import com.pureblog.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public ApiResponse<PageResult<NotificationVO>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(notificationService.getNotifications(page, size));
    }

    @PostMapping("/read/{id}")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success();
    }

    @PostMapping("/read/all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.success();
    }

    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Long>> getUnreadCount() {
        return ApiResponse.success(Map.of("count", notificationService.getUnreadCount()));
    }
}
