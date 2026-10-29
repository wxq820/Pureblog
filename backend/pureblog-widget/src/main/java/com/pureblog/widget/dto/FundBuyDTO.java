package com.pureblog.widget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FundBuyDTO {

    @NotNull(message = "基金ID不能为空")
    private Long fundId;

    @NotNull(message = "买入日期不能为空")
    private LocalDate buyDate;

    @NotNull(message = "买入单价不能为空")
    @DecimalMin(value = "0.0001", message = "单价必须大于 0")
    private BigDecimal price;

    @NotNull(message = "份额不能为空")
    @DecimalMin(value = "0.01", message = "份额必须大于 0")
    private BigDecimal shares;

    private String remark;
}
