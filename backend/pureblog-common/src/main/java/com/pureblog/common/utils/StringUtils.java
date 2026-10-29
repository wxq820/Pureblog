package com.pureblog.common.utils;

public class StringUtils {

    private StringUtils() {}

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    public static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "*" + email.substring(atIndex);
        }
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }

    public static String slugify(String str) {
        if (isBlank(str)) {
            return "";
        }
        return str.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * 把 LIKE 通配符按字面量处理, 供 {@code LIKE CONCAT('%', ?, '%') ESCAPE '!'} 使用.
     *
     * <p>转义符为 {@code !}, 与 SearchMapper.xml 中的 ESCAPE '!' 保持一致:
     * {@code !} → {@code !!}, {@code %} → {@code !%}, {@code _} → {@code !_}。
     * 不转义的话, 用户搜 "100%" 会匹配到任意内容。
     */
    public static String escapeLike(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(str.length() + 8);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '!' || c == '%' || c == '_') {
                sb.append('!');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
