package com.pureblog.widget.controller;

import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.WidgetCreateDTO;
import com.pureblog.widget.dto.WidgetUpdateDTO;
import com.pureblog.widget.entity.WidgetDO;
import com.pureblog.widget.service.WidgetService;
import com.pureblog.widget.vo.WidgetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/widget")
@RequiredArgsConstructor
public class WidgetAdminController {

    private final WidgetService widgetService;

    @GetMapping("/list")
    public ApiResponse<List<WidgetVO>> listAll() {
        requireAdmin();
        return ApiResponse.success(widgetService.listAll());
    }

    @PostMapping("/create")
    public ApiResponse<WidgetDO> create(@Valid @RequestBody WidgetCreateDTO dto) {
        requireAdmin();
        return ApiResponse.success(widgetService.create(dto));
    }

    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody WidgetUpdateDTO dto) {
        requireAdmin();
        widgetService.update(dto);
        return ApiResponse.success();
    }

    @PostMapping("/status/{id}")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        requireAdmin();
        widgetService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        requireAdmin();
        widgetService.delete(id);
        return ApiResponse.success();
    }

    private void requireAdmin() {
        var user = LoginUserHolder.get();
        if (user == null || !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
