package com.pureblog.widget.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pb_widget_click")
public class WidgetClickDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long widgetId;
    private String deviceId;
    private LocalDate clickDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
