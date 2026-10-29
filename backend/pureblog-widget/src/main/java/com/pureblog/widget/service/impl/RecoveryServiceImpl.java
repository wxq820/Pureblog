package com.pureblog.widget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.widget.dto.PositionCreateDTO;
import com.pureblog.widget.dto.PositionValueDTO;
import com.pureblog.widget.entity.PositionDO;
import com.pureblog.widget.entity.PositionValueDO;
import com.pureblog.widget.mapper.PositionMapper;
import com.pureblog.widget.mapper.PositionValueMapper;
import com.pureblog.widget.service.RecoveryService;
import com.pureblog.widget.vo.PositionVO;
import com.pureblog.widget.vo.RecoveryDashboardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryServiceImpl implements RecoveryService {

    private final PositionMapper positionMapper;
    private final PositionValueMapper valueMapper;

    @Override
    public RecoveryDashboardVO dashboard() {
        List<PositionDO> positions = positionMapper.selectList(new LambdaQueryWrapper<PositionDO>()
                .eq(PositionDO::getDeleted, 0)
                .orderByAsc(PositionDO::getSortOrder)
                .orderByAsc(PositionDO::getId));
        if (positions.isEmpty()) {
            RecoveryDashboardVO empty = new RecoveryDashboardVO();
            empty.setPositions(Collections.emptyList());
            empty.setTotalCost(BigDecimal.ZERO);
            empty.setTotalMarketValue(BigDecimal.ZERO);
            empty.setTotalPnl(BigDecimal.ZERO);
            empty.setTotalRecoveryProgress(BigDecimal.ZERO);
            return empty;
        }
        List<Long> posIds = positions.stream().map(PositionDO::getId).collect(Collectors.toList());
        List<PositionValueDO> values = valueMapper.selectList(new LambdaQueryWrapper<PositionValueDO>()
                .eq(PositionValueDO::getDeleted, 0)
                .in(PositionValueDO::getPositionId, posIds)
                .orderByDesc(PositionValueDO::getRecordedAt));
        // 每仓位取最新一条
        Map<Long, PositionValueDO> latestByPos = values.stream()
                .collect(Collectors.toMap(PositionValueDO::getPositionId, v -> v, (a, b) -> a));

        List<PositionVO> vos = positions.stream().map(p -> toVO(p, latestByPos.get(p.getId()))).collect(Collectors.toList());
        BigDecimal totalCost = vos.stream().map(PositionVO::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMv = vos.stream().map(PositionVO::getLatestMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPnl = totalMv.subtract(totalCost);
        BigDecimal progress = totalCost.signum() == 0
                ? BigDecimal.ZERO
                : totalMv.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).min(BigDecimal.valueOf(100));

        RecoveryDashboardVO d = new RecoveryDashboardVO();
        d.setPositions(vos);
        d.setTotalCost(totalCost);
        d.setTotalMarketValue(totalMv);
        d.setTotalPnl(totalPnl);
        d.setTotalRecoveryProgress(progress);
        return d;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionDO savePosition(PositionCreateDTO dto) {
        PositionDO p;
        if (dto.getId() != null) {
            p = positionMapper.selectById(dto.getId());
            if (p == null || p.getDeleted() != 0) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "仓位不存在");
            }
        } else {
            p = new PositionDO();
            if (dto.getSortOrder() == null) {
                p.setSortOrder(nextSort());
            } else {
                p.setSortOrder(dto.getSortOrder());
            }
            p.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        }
        p.setName(dto.getName().trim());
        p.setCost(dto.getCost());
        p.setRemark(StringUtils.isBlank(dto.getRemark()) ? null : dto.getRemark().trim());
        if (dto.getStatus() != null) p.setStatus(dto.getStatus());
        if (dto.getId() == null) {
            positionMapper.insert(p);
            log.info("Position created: id={}, name={}", p.getId(), p.getName());
        } else {
            positionMapper.updateById(p);
            log.info("Position updated: id={}", p.getId());
        }
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveValue(PositionValueDTO dto) {
        PositionDO p = positionMapper.selectById(dto.getPositionId());
        if (p == null || p.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "仓位不存在");
        }
        PositionValueDO v = new PositionValueDO();
        v.setPositionId(dto.getPositionId());
        v.setMarketValue(dto.getMarketValue());
        v.setRecordedAt(dto.getRecordedAt() == null ? LocalDateTime.now() : dto.getRecordedAt());
        v.setRemark(StringUtils.isBlank(dto.getRemark()) ? null : dto.getRemark().trim());
        valueMapper.insert(v);
        log.info("Position value saved: positionId={}, mv={}", dto.getPositionId(), dto.getMarketValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePosition(Long id) {
        PositionDO p = positionMapper.selectById(id);
        if (p == null || p.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "仓位不存在");
        }
        positionMapper.deleteById(id);
        valueMapper.delete(new LambdaQueryWrapper<PositionValueDO>()
                .eq(PositionValueDO::getPositionId, id));
        log.info("Position deleted: id={}", id);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private Integer nextSort() {
        Integer max = positionMapper.selectList(new LambdaQueryWrapper<PositionDO>()
                        .eq(PositionDO::getDeleted, 0))
                .stream().map(PositionDO::getSortOrder).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(-1);
        return max + 1;
    }

    private PositionVO toVO(PositionDO p, PositionValueDO latest) {
        PositionVO vo = new PositionVO();
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setCost(p.getCost());
        vo.setRemark(p.getRemark());
        vo.setSortOrder(p.getSortOrder());
        vo.setStatus(p.getStatus());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setUpdatedAt(p.getUpdatedAt());
        BigDecimal mv = latest == null ? BigDecimal.ZERO : (latest.getMarketValue() == null ? BigDecimal.ZERO : latest.getMarketValue());
        vo.setLatestMarketValue(mv);
        vo.setLatestRecordedAt(latest == null ? null : latest.getRecordedAt());
        BigDecimal pnl = mv.subtract(p.getCost() == null ? BigDecimal.ZERO : p.getCost());
        vo.setPnl(pnl);
        if (p.getCost() != null && p.getCost().signum() > 0) {
            vo.setPnlRate(pnl.divide(p.getCost(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
            BigDecimal progress = mv.divide(p.getCost(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            if (progress.compareTo(BigDecimal.valueOf(100)) > 0) progress = BigDecimal.valueOf(100);
            vo.setRecoveryProgress(progress);
        } else {
            vo.setPnlRate(null);
            vo.setRecoveryProgress(BigDecimal.ZERO);
        }
        BigDecimal dist = p.getCost() == null ? BigDecimal.ZERO : p.getCost().subtract(mv);
        if (dist.signum() < 0) dist = BigDecimal.ZERO;
        vo.setDistanceToBreakEven(dist);
        vo.setBreakEven(dist.signum() == 0);
        return vo;
    }
}
