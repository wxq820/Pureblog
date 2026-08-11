package com.pureblog.common.interceptor;

import com.pureblog.common.context.LoginUser;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.common.utils.WebUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RedisTemplate<String, Object> redisTemplate;

    public AuthInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = WebUtils.getToken();
        
        if (StringUtils.isBlank(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String blackKey = "pureblog:token:blacklist:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blackKey))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"令牌已失效\"}");
                return false;
            }

            LoginUser user = LoginUser.builder()
                    .userId(Long.valueOf(claims.getSubject()))
                    .username(claims.get("username", String.class))
                    .nickname(claims.get("nickname", String.class))
                    .avatarUrl(claims.get("avatarUrl", String.class))
                    .build();

            String roleStr = claims.get("role", String.class);
            if (roleStr != null) {
                user.setRole(com.pureblog.common.enums.UserRole.of(Integer.parseInt(roleStr)));
            }

            LoginUserHolder.set(user);
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌已过期\"}");
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.remove();
    }
}
