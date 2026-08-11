package com.pureblog.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String role;
    private Integer followerCount;
    private Integer followingCount;
    private Integer articleCount;
    private Boolean isFollowing;
    private LocalDateTime createdAt;
}
