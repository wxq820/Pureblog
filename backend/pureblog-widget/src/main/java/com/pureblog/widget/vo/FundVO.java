package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FundVO {

    private Long id;
    private String name;
    private String code;
    private String type;
    private String remark;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 最新净值 (pb_fund_nav 最新一条) */
    private BigDecimal latestNav;
    /** 最新一条的 recordedAt */
    private LocalDateTime latestNavAt;
    /** 累计份额 */
    private BigDecimal totalShares;
    /** 累计成本 */
    private BigDecimal totalCost;
    /** 当前市值 = totalShares * latestNav */
    private BigDecimal marketValue;
    /** 盈亏 = marketValue - totalCost */
    private BigDecimal pnl;
    /** 盈亏率 = pnl / totalCost * 100, cost=0 时 null */
    private BigDecimal pnlRate;
    /** 买入行明细 */
    private List<FundBuyVO> buys;
}
