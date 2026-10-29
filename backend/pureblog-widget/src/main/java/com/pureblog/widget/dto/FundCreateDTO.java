package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FundCreateDTO {

    private Long id;

    @NotBlank(message = "名称不能为空")
    @Size(max = 64)
    private String name;

    @Size(max = 32)
    private String code;

    @Size(max = 16)
    private String type;

    @Size(max = 255)
    private String remark;

    private Integer sortOrder;
    private Integer status;
}
