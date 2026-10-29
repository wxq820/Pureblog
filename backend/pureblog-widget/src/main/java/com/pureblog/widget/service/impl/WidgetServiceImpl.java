package com.pureblog.widget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.widget.dto.WidgetCreateDTO;
import com.pureblog.widget.dto.WidgetUpdateDTO;
import com.pureblog.widget.entity.WidgetDO;
import com.pureblog.widget.mapper.WidgetMapper;
import com.pureblog.widget.service.WidgetService;
import com.pureblog.widget.vo.WidgetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {

    private final WidgetMapper widgetMapper;

    @Override
    public List<WidgetVO> listActive() {
        List<WidgetDO> list = widgetMapper.selectList(new LambdaQueryWrapper<WidgetDO>()
                .eq(WidgetDO::getDeleted, 0)
                .eq(WidgetDO::getStatus, 1)
                .orderByDesc(WidgetDO::getClickCount)
                .orderByAsc(WidgetDO::getSortOrder)
                .orderByAsc(WidgetDO::getId));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<WidgetVO> listAll() {
        List<WidgetDO> list = widgetMapper.selectList(new LambdaQueryWrapper<WidgetDO>()
                .eq(WidgetDO::getDeleted, 0)
                .orderByDesc(WidgetDO::getStatus)
                .orderByDesc(WidgetDO::getClickCount)
                .orderByAsc(WidgetDO::getSortOrder)
                .orderByAsc(WidgetDO::getId));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WidgetDO getById(Long id) {
        WidgetDO w = widgetMapper.selectById(id);
        if (w == null) throw new BusinessException(ErrorCode.WIDGET_NOT_FOUND);
        return w;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WidgetDO create(WidgetCreateDTO dto) {
        Long existed = widgetMapper.selectCount(new LambdaQueryWrapper<WidgetDO>()
                .eq(WidgetDO::getCode, dto.getCode())
                .eq(WidgetDO::getDeleted, 0));
        if (existed != null && existed > 0) {
            throw new BusinessException(ErrorCode.WIDGET_CODE_EXISTS);
        }
        WidgetDO w = new WidgetDO();
        w.setCode(dto.getCode());
        w.setName(dto.getName());
        w.setDescription(dto.getDescription());
        w.setIcon(dto.getIcon());
        w.setIconColor(StringUtils.isBlank(dto.getIconColor()) ? "#2563eb" : dto.getIconColor());
        w.setPath(dto.getPath());
        w.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        w.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        w.setClickCount(0);
        widgetMapper.insert(w);
        log.info("Widget created: id={}, code={}", w.getId(), w.getCode());
        return w;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WidgetUpdateDTO dto) {
        WidgetDO w = getById(dto.getId());
        if (StringUtils.isNotBlank(dto.getName())) w.setName(dto.getName());
        if (dto.getDescription() != null) w.setDescription(dto.getDescription());
        if (dto.getIcon() != null) w.setIcon(dto.getIcon());
        if (StringUtils.isNotBlank(dto.getIconColor())) w.setIconColor(dto.getIconColor());
        if (StringUtils.isNotBlank(dto.getPath())) w.setPath(dto.getPath());
        if (dto.getSortOrder() != null) w.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) w.setStatus(dto.getStatus());
        widgetMapper.updateById(w);
        log.info("Widget updated: id={}", dto.getId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        getById(id);
        widgetMapper.update(null, new LambdaUpdateWrapper<WidgetDO>()
                .eq(WidgetDO::getId, id)
                .set(WidgetDO::getStatus, status));
        log.info("Widget status: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        widgetMapper.deleteById(id);
        log.info("Widget deleted: id={}", id);
    }

    private WidgetVO toVO(WidgetDO w) {
        WidgetVO vo = new WidgetVO();
        vo.setId(w.getId());
        vo.setCode(w.getCode());
        vo.setName(w.getName());
        vo.setDescription(w.getDescription());
        vo.setIcon(w.getIcon());
        vo.setIconColor(w.getIconColor());
        vo.setPath(w.getPath());
        vo.setSortOrder(w.getSortOrder());
        vo.setStatus(w.getStatus());
        vo.setClickCount(w.getClickCount());
        vo.setCreatedAt(w.getCreatedAt());
        vo.setUpdatedAt(w.getUpdatedAt());
        return vo;
    }
}
