package com.pureblog.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebUtils {

    private WebUtils() {}

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getResponse() : null;
    }

    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader(name) : null;
    }

    public static String getToken() {
        String auth = getHeader("Authorization");
        if (StringUtils.isNotBlank(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return auth;
    }

    public static void setResponseNoCache(HttpServletResponse response) {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Expires", "0");
    }
}
