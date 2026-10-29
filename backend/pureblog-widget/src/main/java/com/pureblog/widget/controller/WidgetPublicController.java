package com.pureblog.widget.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.WidgetClickDTO;
import com.pureblog.widget.service.WidgetClickService;
import com.pureblog.widget.service.WidgetService;
import com.pureblog.widget.vo.WidgetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/widget")
@RequiredArgsConstructor
public class WidgetPublicController {

    private final WidgetService widgetService;
    private final WidgetClickService widgetClickService;

    /** 需登录: 首页侧栏拉所有启用 widget (已按 click_count 降序). */
    @GetMapping("/list")
    public ApiResponse<List<WidgetVO>> list() {
        return ApiResponse.success(widgetService.listActive());
    }

    /** 需登录: 进入 widget 页面时调用, 记 1 次点击 (按设备+日去重). */
    @PostMapping("/click")
    public ApiResponse<Boolean> click(@Valid @RequestBody WidgetClickDTO dto) {
        boolean added = widgetClickService.click(dto.getWidgetId(), dto.getDeviceId());
        return ApiResponse.success(added);
    }
}
