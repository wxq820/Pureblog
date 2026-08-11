package com.pureblog.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDTO {
    
    @Size(max = 50, message = "昵称长度不超过50个字符")
    private String nickname;
    
    @Size(max = 500, message = "个人简介长度不超过500个字符")
    private String bio;
    
    private String avatarUrl;
}
