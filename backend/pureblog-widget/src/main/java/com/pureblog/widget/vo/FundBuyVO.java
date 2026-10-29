package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FundBuyVO {

    private Long id;
    private Long fundId;
    private LocalDate buyDate;
    private BigDecimal price;
    private BigDecimal shares;
    private BigDecimal amount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
