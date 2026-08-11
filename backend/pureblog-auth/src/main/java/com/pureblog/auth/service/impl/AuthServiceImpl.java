package com.pureblog.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.auth.dto.*;
import com.pureblog.auth.entity.LoginLogDO;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.LoginLogMapper;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.auth.service.AuthService;
import com.pureblog.common.context.LoginUser;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.enums.UserRole;
import com.pureblog.common.enums.UserStatus;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.utils.IpUtils;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.common.utils.WebUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private static final String CAPTCHA_PREFIX = "pureblog:captcha:";
    private static final String REFRESH_TOKEN_PREFIX = "pureblog:refresh:token:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse login(LoginRequest request) {
        if (StringUtils.isNotBlank(request.getCaptchaKey()) && StringUtils.isNotBlank(request.getCaptchaCode())) {
            if (!verifyCaptcha(request.getCaptchaKey(), request.getCaptchaCode())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "验证码错误");
            }
        }

        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, request.getUsername());
        UserDO user = userMapper.selectOne(wrapper);

        if (user == null) {
            recordLoginLog(null, request.getUsername(), 2, "用户不存在");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginLog(user.getId(), request.getUsername(), 2, "密码错误");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != UserStatus.NORMAL.getCode()) {
            recordLoginLog(user.getId(), request.getUsername(), 2, "账号已禁用");
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        recordLoginLog(user.getId(), request.getUsername(), 1, "登录成功");

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("User {} logged in successfully", user.getUsername());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(RegisterRequest request) {
        LambdaQueryWrapper<UserDO> check = new LambdaQueryWrapper<>();
        check.eq(UserDO::getUsername, request.getUsername());
        if (userMapper.selectCount(check) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        check = new LambdaQueryWrapper<>();
        check.eq(UserDO::getEmail, request.getEmail());
        if (userMapper.selectCount(check) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        UserDO user = new UserDO();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setNickname(StringUtils.isNotBlank(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setRole(UserRole.USER.getCode());
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFollowerCount(0);
        user.setFollowingCount(0);
        user.setArticleCount(0);

        userMapper.insert(user);

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("User {} registered successfully", user.getUsername());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public void logout() {
        LoginUser current = LoginUserHolder.get();
        if (current != null) {
            String token = WebUtils.getToken();
            if (StringUtils.isNotBlank(token)) {
                redisTemplate.opsForValue().set("pureblog:token:blacklist:" + token, "1", jwtExpiration, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = Long.valueOf(claims.getSubject());
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        UserDO user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != UserStatus.NORMAL.getCode()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public String generateCaptcha() {
        String key = UUID.randomUUID().toString().replace("-", "");
        String code = String.format("%04d", (int) (Math.random() * 10000));
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code, 5, TimeUnit.MINUTES);
        return key;
    }

    @Override
    public boolean verifyCaptcha(String key, String code) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(code)) {
            return false;
        }
        String stored = (String) redisTemplate.opsForValue().get(CAPTCHA_PREFIX + key);
        if (stored == null) {
            return false;
        }
        boolean match = stored.equalsIgnoreCase(code);
        if (match) {
            redisTemplate.delete(CAPTCHA_PREFIX + key);
        }
        return match;
    }

    private String generateAccessToken(UserDO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("nickname", user.getNickname());
        claims.put("avatarUrl", user.getAvatarUrl());
        claims.put("role", String.valueOf(user.getRole()));
        claims.put("type", "access");

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    private String generateRefreshToken(UserDO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("username", user.getUsername());

        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();

        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId(), token, refreshExpiration, TimeUnit.MILLISECONDS);
        return token;
    }

    private AuthResponse buildAuthResponse(UserDO user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration / 1000)
                .user(AuthResponse.UserVO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .role(UserRole.of(user.getRole()).getDesc())
                        .roleCode(user.getRole())
                        .build())
                .build();
    }

    private void recordLoginLog(Long userId, String username, int status, String msg) {
        try {
            HttpServletRequest request = WebUtils.getRequest();
            LoginLogDO logDO = new LoginLogDO();
            logDO.setUserId(userId);
            logDO.setUsername(username);
            logDO.setIp(request != null ? IpUtils.getIpAddress(request) : "unknown");
            logDO.setUserAgent(request != null ? request.getHeader("User-Agent") : null);
            logDO.setStatus(status);
            logDO.setMsg(msg);
            logDO.setCreatedAt(java.time.LocalDateTime.now());
            loginLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("Failed to record login log", e);
        }
    }
}
