package com.pureblog.widget.service;

import com.pureblog.widget.dto.WidgetCreateDTO;
import com.pureblog.widget.dto.WidgetUpdateDTO;
import com.pureblog.widget.entity.WidgetDO;
import com.pureblog.widget.vo.WidgetVO;

import java.util.List;

public interface WidgetService {

    /** 公开 API: 仅返回 status=1 的启用项, 按 click_count 降序. */
    List<WidgetVO> listActive();

    /** admin: 返回所有 (含禁用). */
    List<WidgetVO> listAll();

    WidgetDO getById(Long id);

    WidgetDO create(WidgetCreateDTO dto);

    void update(WidgetUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
