package com.pureblog.widget.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.LedgerDictCreateDTO;
import com.pureblog.widget.dto.LedgerRecordCreateDTO;
import com.pureblog.widget.entity.LedgerRecordDO;
import com.pureblog.widget.service.LedgerService;
import com.pureblog.widget.vo.LedgerDictVO;
import com.pureblog.widget.vo.LedgerRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /** 需登录: 拉平台(type=1) 或 项目(type=2) 字典. */
    @GetMapping("/dict")
    public ApiResponse<List<LedgerDictVO>> listDict(@RequestParam("type") Integer type) {
        return ApiResponse.success(ledgerService.listDict(type));
    }

    /** 需登录: 即时新增一个字典项. */
    @PostMapping("/dict")
    public ApiResponse<LedgerDictVO> addDict(@Valid @RequestBody LedgerDictCreateDTO dto) {
        return ApiResponse.success(ledgerService.addDict(dto));
    }

    /** 需登录: 记一笔. */
    @PostMapping("/record")
    public ApiResponse<LedgerRecordDO> addRecord(@Valid @RequestBody LedgerRecordCreateDTO dto) {
        return ApiResponse.success(ledgerService.addRecord(dto));
    }

    /** 需登录: 按设备ID查询最近记录 (按日期倒序, 字典信息已内联). */
    @GetMapping("/record")
    public ApiResponse<List<LedgerRecordVO>> listRecord(
            @RequestParam("deviceId") String deviceId,
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit) {
        return ApiResponse.success(ledgerService.listRecentRecord(deviceId, limit));
    }
}
