package com.pureblog.widget.service.impl;

import com.pureblog.widget.entity.WidgetClickDO;
import com.pureblog.widget.mapper.WidgetClickMapper;
import com.pureblog.widget.service.WidgetClickService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetClickServiceImpl implements WidgetClickService {

    private final WidgetClickMapper widgetClickMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean click(Long widgetId, String deviceId) {
        LocalDate today = LocalDate.now();
        long existed = widgetClickMapper.countByDedupKey(widgetId, deviceId, today);
        if (existed > 0) {
            log.debug("Widget click dedup: widgetId={}, deviceId={}", widgetId, deviceId);
            return false;
        }
        WidgetClickDO click = new WidgetClickDO();
        click.setWidgetId(widgetId);
        click.setDeviceId(deviceId);
        click.setClickDate(today);
        widgetClickMapper.insert(click);
        widgetClickMapper.incrementClickCount(widgetId);
        log.debug("Widget click counted: widgetId={}, deviceId={}", widgetId, deviceId);
        return true;
    }
}
