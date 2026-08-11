package com.pureblog.user.service;

import com.pureblog.user.dto.UserProfileDTO;
import com.pureblog.user.vo.UserProfileVO;

import java.util.List;

public interface UserService {
    
    UserProfileVO getCurrentUserProfile();
    
    UserProfileVO getUserProfile(Long userId);
    
    void updateProfile(UserProfileDTO dto);
    
    void follow(Long userId);
    
    void unfollow(Long userId);
    
    boolean isFollowing(Long userId);
    
    List<UserProfileVO> getFollowers(Long userId, int page, int size);
    
    List<UserProfileVO> getFollowing(Long userId, int page, int size);
}
