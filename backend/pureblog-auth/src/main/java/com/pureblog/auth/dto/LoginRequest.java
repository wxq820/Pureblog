package com.pureblog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Pattern(regexp = "^[a-zA-Z0-9]{4}$", message = "验证码格式错误")
    private String captchaCode;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaKey;
}
