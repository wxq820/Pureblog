package com.pureblog.widget.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.widget.entity.WidgetClickDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

public interface WidgetClickMapper extends BaseMapper<WidgetClickDO> {

    /** 同设备同日同 widget 是否已存在点击流水. */
    @Select("SELECT COUNT(*) FROM pb_widget_click WHERE widget_id = #{widgetId} AND device_id = #{deviceId} AND click_date = #{clickDate}")
    long countByDedupKey(@Param("widgetId") Long widgetId,
                         @Param("deviceId") String deviceId,
                         @Param("clickDate") LocalDate clickDate);

    /** widget 点击数 +1. */
    @org.apache.ibatis.annotations.Update("UPDATE pb_widget SET click_count = click_count + 1, updated_at = NOW() WHERE id = #{widgetId} AND deleted = 0")
    int incrementClickCount(@Param("widgetId") Long widgetId);
}
