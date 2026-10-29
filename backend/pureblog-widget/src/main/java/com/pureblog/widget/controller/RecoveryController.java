package com.pureblog.widget.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.PositionCreateDTO;
import com.pureblog.widget.dto.PositionValueDTO;
import com.pureblog.widget.entity.PositionDO;
import com.pureblog.widget.service.RecoveryService;
import com.pureblog.widget.vo.RecoveryDashboardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回本之路 - 登录态. 路径前缀 /api/widget/recovery.
 */
@RestController
@RequestMapping("/api/widget/recovery")
@RequiredArgsConstructor
public class RecoveryController {

    private final RecoveryService recoveryService;

    @GetMapping("/dashboard")
    public ApiResponse<RecoveryDashboardVO> dashboard() {
        return ApiResponse.success(recoveryService.dashboard());
    }

    @PostMapping("/position/save")
    public ApiResponse<PositionDO> savePosition(@Valid @RequestBody PositionCreateDTO dto) {
        return ApiResponse.success(recoveryService.savePosition(dto));
    }

    @PostMapping("/value/save")
    public ApiResponse<Void> saveValue(@Valid @RequestBody PositionValueDTO dto) {
        recoveryService.saveValue(dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/position/{id}")
    public ApiResponse<Void> deletePosition(@PathVariable("id") Long id) {
        recoveryService.deletePosition(id);
        return ApiResponse.success();
    }
}
