package com.pureblog.widget.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LedgerRecordCreateDTO {

    @NotNull(message = "日期不能为空")
    private LocalDate recordDate;

    @NotNull(message = "平台不能为空")
    private Long platformDictId;

    /** 二选一: 固定项传 itemDictId, 自定义传 itemText */
    private Long itemDictId;

    @Size(max = 128)
    private String itemText;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "设备ID不能为空")
    @Size(max = 64)
    private String deviceId;
}
