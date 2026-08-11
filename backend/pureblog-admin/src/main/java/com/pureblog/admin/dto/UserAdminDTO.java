package com.pureblog.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAdminDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Integer role;
    private Integer status;
}
