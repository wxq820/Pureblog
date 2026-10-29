package com.pureblog.widget.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.NetWorthSnapshotCreateDTO;
import com.pureblog.widget.service.NetWorthService;
import com.pureblog.widget.vo.NetWorthEntryVO;
import com.pureblog.widget.vo.NetWorthSnapshotVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 身家 widget - 登录态. 路径前缀 /api/widget/networth,
 * 由 JwtAuthenticationFilter.shouldNotFilter 显式 NOT 跳过, 走登录校验.
 */
@RestController
@RequestMapping("/api/widget/networth")
@RequiredArgsConstructor
public class NetWorthController {

    private final NetWorthService netWorthService;

    @PostMapping("/snapshot/save")
    public ApiResponse<NetWorthSnapshotVO> saveSnapshot(@Valid @RequestBody NetWorthSnapshotCreateDTO dto) {
        return ApiResponse.success(netWorthService.saveSnapshot(dto));
    }

    @GetMapping("/snapshot/list")
    public ApiResponse<List<NetWorthSnapshotVO>> listSnapshots() {
        return ApiResponse.success(netWorthService.listSnapshots());
    }

    @GetMapping("/snapshot/{id}/entries")
    public ApiResponse<List<NetWorthEntryVO>> listEntries(@PathVariable("id") Long id) {
        return ApiResponse.success(netWorthService.listEntries(id));
    }

    @GetMapping("/dashboard")
    public ApiResponse<NetWorthSnapshotVO> dashboard() {
        return ApiResponse.success(netWorthService.latest());
    }

    @DeleteMapping("/snapshot/{id}")
    public ApiResponse<Void> deleteSnapshot(@PathVariable("id") Long id) {
        netWorthService.deleteSnapshot(id);
        return ApiResponse.success();
    }
}
