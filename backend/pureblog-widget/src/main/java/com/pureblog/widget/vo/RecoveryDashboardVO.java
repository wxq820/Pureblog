package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecoveryDashboardVO {

    private List<PositionVO> positions;
    private BigDecimal totalCost;
    private BigDecimal totalMarketValue;
    private BigDecimal totalPnl;
    /** 整体回本进度 (totalMarketValue / totalCost) */
    private BigDecimal totalRecoveryProgress;
}
