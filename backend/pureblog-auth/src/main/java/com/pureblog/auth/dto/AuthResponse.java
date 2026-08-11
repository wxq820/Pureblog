package com.pureblog.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserVO user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserVO {
        private Long userId;
        private String username;
        private String nickname;
        private String avatarUrl;
        private String role;
        private Integer roleCode;
    }
}
