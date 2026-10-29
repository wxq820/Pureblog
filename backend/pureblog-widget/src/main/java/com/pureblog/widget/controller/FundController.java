package com.pureblog.widget.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.FundBuyDTO;
import com.pureblog.widget.dto.FundCreateDTO;
import com.pureblog.widget.dto.FundNavDTO;
import com.pureblog.widget.entity.FundDO;
import com.pureblog.widget.service.FundService;
import com.pureblog.widget.vo.FundDashboardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 投资表 - 登录态. 路径前缀 /api/widget/fund.
 */
@RestController
@RequestMapping("/api/widget/fund")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    @GetMapping("/dashboard")
    public ApiResponse<FundDashboardVO> dashboard() {
        return ApiResponse.success(fundService.dashboard());
    }

    @PostMapping("/save")
    public ApiResponse<FundDO> saveFund(@Valid @RequestBody FundCreateDTO dto) {
        return ApiResponse.success(fundService.saveFund(dto));
    }

    @PostMapping("/buy/save")
    public ApiResponse<Void> saveBuy(@Valid @RequestBody FundBuyDTO dto) {
        fundService.saveBuy(dto);
        return ApiResponse.success();
    }

    @PostMapping("/nav/save")
    public ApiResponse<Void> saveNav(@Valid @RequestBody FundNavDTO dto) {
        fundService.saveNav(dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/buy/{id}")
    public ApiResponse<Void> deleteBuy(@PathVariable("id") Long id) {
        fundService.deleteBuy(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFund(@PathVariable("id") Long id) {
        fundService.deleteFund(id);
        return ApiResponse.success();
    }
}
