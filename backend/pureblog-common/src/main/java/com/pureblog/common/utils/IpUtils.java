package com.pureblog.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private IpUtils() {}

    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static boolean isValidIp(String ip) {
        return StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip);
    }
}
