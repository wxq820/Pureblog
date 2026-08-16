package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "操作成功"),

    // 认证相关 1000-1999
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问该资源"),
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已被使用"),
    INVALID_CREDENTIALS(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "账号已被禁用"),
    TOKEN_EXPIRED(1005, "令牌已过期"),
    TOKEN_INVALID(1006, "令牌无效"),
    REFRESH_TOKEN_EXPIRED(1007, "刷新令牌已过期"),

    // 用户相关 2000-2999
    USER_NOT_FOUND(2001, "用户不存在"),
    CANNOT_FOLLOW_SELF(2002, "不能关注自己"),
    ALREADY_FOLLOWED(2003, "已关注过该用户"),
    NOT_FOLLOWED(2004, "未关注该用户"),

    // 文章相关 3000-3999
    ARTICLE_NOT_FOUND(3001, "文章不存在"),
    ARTICLE_NOT_PUBLISHED(3002, "文章未发布或已下架"),
    NOT_AUTHOR(3003, "不是该文章的作者"),
    CATEGORY_NOT_FOUND(3004, "分类不存在"),
    TAG_NOT_FOUND(3005, "标签不存在"),
    TAG_EXISTS(3006, "标签已存在"),
    CATEGORY_EXISTS(3007, "分类已存在"),

    // 目录树相关 6000-6999
    TREE_NOT_FOUND(6001, "目录树不存在"),
    TREE_CODE_EXISTS(6002, "目录树编码已存在"),
    TREE_NODE_NOT_FOUND(6003, "目录树节点不存在"),
    TREE_NODE_NOT_LEAF(6004, "只能在叶子节点下挂文章"),
    TREE_NODE_HAS_CHILDREN(6005, "该节点下存在子节点,无法删除"),
    TREE_NODE_HAS_ARTICLES(6006, "该节点下存在文章,无法删除"),

    // 评论相关 4000-4999
    COMMENT_NOT_FOUND(4001, "评论不存在"),
    PARENT_COMMENT_NOT_FOUND(4002, "父评论不存在"),
    ARTICLE_COMMENT_DISABLED(4003, "该文章已关闭评论"),

    // 文件相关 5000-5999
    FILE_UPLOAD_FAILED(5001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(5002, "不支持的文件类型"),
    FILE_TOO_LARGE(5003, "文件大小超过限制"),

    // 系统相关 9000-9999
    INTERNAL_SERVER_ERROR(9001, "服务器内部错误"),
    SERVICE_UNAVAILABLE(9002, "服务暂不可用"),
    PARAM_INVALID(9003, "参数不合法"),
    OPERATION_TOO_FREQUENT(9004, "操作过于频繁，请稍后再试");

    private final int code;
    private final String message;
}
