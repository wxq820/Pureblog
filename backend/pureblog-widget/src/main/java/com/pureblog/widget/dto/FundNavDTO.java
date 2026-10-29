package com.pureblog.widget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FundNavDTO {

    @NotNull(message = "基金ID不能为空")
    private Long fundId;

    @NotNull(message = "净值不能为空")
    @DecimalMin(value = "0.0001", message = "净值必须大于 0")
    private BigDecimal nav;

    private LocalDateTime recordedAt;
    private String remark;
}
