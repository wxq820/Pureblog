package com.pureblog.admin.vo;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String status;
    private Integer followerCount;
    private Integer articleCount;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
