package com.pureblog.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public class IdUtils {

    private static final Snowflake SNOWFLAKE = new Snowflake(1, 1);

    private IdUtils() {}

    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    public static String nextIdStr() {
        return String.valueOf(SNOWFLAKE.nextId());
    }

    public static String simpleUUID() {
        return IdUtil.simpleUUID();
    }
}
