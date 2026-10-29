package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WidgetClickDTO {

    @NotNull(message = "widgetId 不能为空")
    private Long widgetId;

    @NotBlank(message = "deviceId 不能为空")
    @Size(max = 64)
    private String deviceId;
}
