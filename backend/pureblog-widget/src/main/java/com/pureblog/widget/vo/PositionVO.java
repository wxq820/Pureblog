package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PositionVO {

    private Long id;
    private String name;
    private BigDecimal cost;
    private String remark;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 最新市值 (取 pb_position_value 最新一条) */
    private BigDecimal latestMarketValue;
    /** 最新一条的 recordedAt */
    private LocalDateTime latestRecordedAt;
    /** 盈亏金额 = latestMarketValue - cost */
    private BigDecimal pnl;
    /** 盈亏率 = pnl / cost * 100, 成本为 0 时为 null */
    private BigDecimal pnlRate;
    /** 距回本差额 = cost - latestMarketValue, 已回本为 0 */
    private BigDecimal distanceToBreakEven;
    /** 回本进度 = min(latestMarketValue / cost, 1) * 100, cost=0 时为 0 */
    private BigDecimal recoveryProgress;
    /** 是否已回本 */
    private Boolean breakEven;
}
