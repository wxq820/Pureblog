package com.pureblog.common.context;

import com.pureblog.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private UserRole role;
    
    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }
    
    public boolean isAuthor() {
        return role == UserRole.AUTHOR || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }
    
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }
}
