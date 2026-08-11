package com.pureblog.common.context;

public class LoginUserHolder {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static void remove() {
        USER_HOLDER.remove();
    }

    public static boolean isLogin() {
        return USER_HOLDER.get() != null;
    }
}
