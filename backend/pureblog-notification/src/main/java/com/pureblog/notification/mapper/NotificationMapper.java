package com.pureblog.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.notification.entity.NotificationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationDO> {
}
