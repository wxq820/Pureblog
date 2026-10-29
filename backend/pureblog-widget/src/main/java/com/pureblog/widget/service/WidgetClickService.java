package com.pureblog.widget.service;

public interface WidgetClickService {

    /**
     * 记录一次点击: INSERT IGNORE pb_widget_click 流水,
     * 若 affectedRows=1 (新命中) 则 pb_widget.click_count++.
     * 同 deviceId 同一天对同一 widget 只计 1 次.
     *
     * @return 是否新增了一次 (true=次数+1, false=已存在不计)
     */
    boolean click(Long widgetId, String deviceId);
}
