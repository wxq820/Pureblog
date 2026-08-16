package com.pureblog.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pureblog.common.context.LoginUser;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.UserRole;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.utils.WebUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BLACKLIST_PREFIX = "pureblog:token:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.equals("/api/article/list")
                || path.equals("/api/article/hot")
                || path.equals("/api/article/featured")
                || path.startsWith("/api/article/author/")
                || path.startsWith("/api/article/public/")
                || path.startsWith("/api/search/")
                || path.startsWith("/api/category/")
                || path.startsWith("/api/tag/")
                || path.startsWith("/api/tree/")
                || path.startsWith("/api/user/public/")
                || path.startsWith("/api/stats/")
                || path.startsWith("/actuator/")
                || path.equals("/error")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = WebUtils.getToken();

        if (token == null || token.isBlank()) {
            writeUnauthorized(response, "未登录");
            return;
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, "令牌已过期");
            return;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            writeUnauthorized(response, "令牌无效");
            return;
        }

        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
            writeUnauthorized(response, "令牌已失效");
            return;
        }

        LoginUser.LoginUserBuilder builder = LoginUser.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .username(claims.get("username", String.class))
                .nickname(claims.get("nickname", String.class))
                .avatarUrl(claims.get("avatarUrl", String.class));

        String roleStr = claims.get("role", String.class);
        if (roleStr != null) {
            builder.role(UserRole.of(Integer.parseInt(roleStr)));
        }
        LoginUserHolder.set(builder.build());

        try {
            chain.doFilter(request, response);
        } finally {
            LoginUserHolder.remove();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(401, message)));
    }
}