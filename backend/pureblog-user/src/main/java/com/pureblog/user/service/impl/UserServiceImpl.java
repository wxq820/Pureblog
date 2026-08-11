package com.pureblog.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.enums.UserRole;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.PageResult;
import com.pureblog.user.dto.UserProfileDTO;
import com.pureblog.user.entity.FollowDO;
import com.pureblog.user.mapper.FollowMapper;
import com.pureblog.user.service.UserService;
import com.pureblog.user.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_PREFIX = "pureblog:user:info:";
    private static final long USER_CACHE_TTL = 30;

    @Override
    public UserProfileVO getCurrentUserProfile() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return getUserProfile(userId);
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Long currentUserId = LoginUserHolder.getUserId();
        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowing = isFollowing(userId);
        }

        return buildProfileVO(user, isFollowing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileDTO dto) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        if (dto.getNickname() != null) {
            wrapper.set(UserDO::getNickname, dto.getNickname());
        }
        if (dto.getBio() != null) {
            wrapper.set(UserDO::getBio, dto.getBio());
        }
        if (dto.getAvatarUrl() != null) {
            wrapper.set(UserDO::getAvatarUrl, dto.getAvatarUrl());
        }
        wrapper.eq(UserDO::getId, userId);
        userMapper.update(null, wrapper);

        redisTemplate.delete(USER_CACHE_PREFIX + userId);
        log.info("User {} updated profile", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId) {
        Long currentUserId = LoginUserHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        UserDO target = userMapper.selectById(userId);
        if (target == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowDO::getFollowerId, currentUserId).eq(FollowDO::getFollowingId, userId);
        if (followMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_FOLLOWED);
        }

        FollowDO follow = new FollowDO();
        follow.setFollowerId(currentUserId);
        follow.setFollowingId(userId);
        followMapper.insert(follow);

        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, currentUserId).setSql("following_count = following_count + 1"));
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId).setSql("follower_count = follower_count + 1"));

        redisTemplate.delete(USER_CACHE_PREFIX + userId);
        log.info("User {} followed user {}", currentUserId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId) {
        Long currentUserId = LoginUserHolder.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowDO::getFollowerId, currentUserId).eq(FollowDO::getFollowingId, userId);
        int deleted = followMapper.delete(wrapper);

        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOLLOWED);
        }

        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, currentUserId).setSql("following_count = GREATEST(following_count - 1, 0)"));
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId).setSql("follower_count = GREATEST(follower_count - 1, 0)"));

        redisTemplate.delete(USER_CACHE_PREFIX + userId);
        log.info("User {} unfollowed user {}", currentUserId, userId);
    }

    @Override
    public boolean isFollowing(Long userId) {
        Long currentUserId = LoginUserHolder.getUserId();
        if (currentUserId == null) return false;

        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowDO::getFollowerId, currentUserId).eq(FollowDO::getFollowingId, userId);
        return followMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<UserProfileVO> getFollowers(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowDO::getFollowingId, userId)
                .last("LIMIT " + offset + "," + size);
        List<FollowDO> follows = followMapper.selectList(wrapper);

        List<UserProfileVO> result = new ArrayList<>();
        Long currentUserId = LoginUserHolder.getUserId();
        for (FollowDO f : follows) {
            UserDO user = userMapper.selectById(f.getFollowerId());
            if (user != null) {
                boolean isFollowing = currentUserId != null && !currentUserId.equals(user.getId()) && 
                        isFollowing(user.getId());
                result.add(buildProfileVO(user, isFollowing));
            }
        }
        return result;
    }

    @Override
    public List<UserProfileVO> getFollowing(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowDO::getFollowerId, userId)
                .last("LIMIT " + offset + "," + size);
        List<FollowDO> follows = followMapper.selectList(wrapper);

        List<UserProfileVO> result = new ArrayList<>();
        Long currentUserId = LoginUserHolder.getUserId();
        for (FollowDO f : follows) {
            UserDO user = userMapper.selectById(f.getFollowingId());
            if (user != null) {
                boolean isFollowing = currentUserId != null && !currentUserId.equals(user.getId()) && 
                        isFollowing(user.getId());
                result.add(buildProfileVO(user, isFollowing));
            }
        }
        return result;
    }

    private UserProfileVO buildProfileVO(UserDO user, boolean isFollowing) {
        return UserProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .role(UserRole.of(user.getRole()).getDesc())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .articleCount(user.getArticleCount())
                .isFollowing(isFollowing)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
