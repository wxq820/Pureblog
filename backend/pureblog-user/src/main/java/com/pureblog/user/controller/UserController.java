package com.pureblog.user.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.result.PageResult;
import com.pureblog.user.dto.UserProfileDTO;
import com.pureblog.user.service.UserService;
import com.pureblog.user.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUserProfile());
    }

    @GetMapping("/public/{userId}")
    public ApiResponse<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UserProfileDTO dto) {
        userService.updateProfile(dto);
        return ApiResponse.success();
    }

    @PostMapping("/follow/{userId}")
    public ApiResponse<Void> follow(@PathVariable Long userId) {
        userService.follow(userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/follow/{userId}")
    public ApiResponse<Void> unfollow(@PathVariable Long userId) {
        userService.unfollow(userId);
        return ApiResponse.success();
    }

    @GetMapping("/followers/{userId}")
    public ApiResponse<List<UserProfileVO>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userService.getFollowers(userId, page, size));
    }

    @GetMapping("/following/{userId}")
    public ApiResponse<List<UserProfileVO>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userService.getFollowing(userId, page, size));
    }
}
