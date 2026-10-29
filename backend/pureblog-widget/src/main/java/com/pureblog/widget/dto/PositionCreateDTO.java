package com.pureblog.widget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionCreateDTO {

    private Long id;

    @NotBlank(message = "仓位名不能为空")
    @Size(max = 64)
    private String name;

    @NotNull(message = "成本不能为空")
    @DecimalMin(value = "0.00", message = "成本不能为负")
    private BigDecimal cost;

    @Size(max = 255)
    private String remark;

    private Integer sortOrder;
    private Integer status;
}
