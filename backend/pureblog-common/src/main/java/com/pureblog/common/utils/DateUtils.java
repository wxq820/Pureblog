package com.pureblog.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DateUtils() {}

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(TIME_FORMATTER);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();
        if (seconds < 60) return "刚刚";
        if (seconds < 3600) return (seconds / 60) + "分钟前";
        if (seconds < 86400) return (seconds / 3600) + "小时前";
        if (seconds < 2592000) return (seconds / 86400) + "天前";
        if (seconds < 31536000L) return (seconds / 2592000) + "个月前";
        return (seconds / 31536000L) + "年前";
    }
}
