package com.pureblog.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.pureblog.common.base.BaseDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * 全局 MyBatis-Plus 配置。
 *
 * <p>接管 {@link BaseDO} 上
 * {@code @TableField(fill = INSERT)} / {@code INSERT_UPDATE} 的自动填充,
 * 任何继承 BaseDO 的实体(Category / Tag / Article / Comment / Tree / TreeNode /
 * Notification / User / ...)在 insert / update 时都会自动写入 createdAt / updatedAt。
 *
 * <p>说明:类似 {@code @TableLogic} 的逻辑删除、{@code @Version} 的乐观锁在
 * BaseDO 已经声明,无需额外配置。
 */
@Slf4j
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // BaseDO / UserDO / CommentDO / TreeDO 等有 createdAt / updatedAt 字段,
        // 不存在的字段 strictInsertFill 会跳过,不会抛异常。
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
