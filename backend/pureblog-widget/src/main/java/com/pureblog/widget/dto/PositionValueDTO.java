package com.pureblog.widget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PositionValueDTO {

    @NotNull(message = "仓位ID不能为空")
    private Long positionId;

    @NotNull(message = "市值不能为空")
    @DecimalMin(value = "0.00", message = "市值不能为负")
    private BigDecimal marketValue;

    private LocalDateTime recordedAt;
    private String remark;
}
