package com.pureblog.widget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.widget.dto.LedgerDictCreateDTO;
import com.pureblog.widget.dto.LedgerRecordCreateDTO;
import com.pureblog.widget.entity.LedgerDictDO;
import com.pureblog.widget.entity.LedgerRecordDO;
import com.pureblog.widget.mapper.LedgerDictMapper;
import com.pureblog.widget.mapper.LedgerRecordMapper;
import com.pureblog.widget.service.LedgerService;
import com.pureblog.widget.vo.LedgerDictVO;
import com.pureblog.widget.vo.LedgerRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerDictMapper dictMapper;
    private final LedgerRecordMapper recordMapper;

    @Override
    public List<LedgerDictVO> listDict(Integer type) {
        List<LedgerDictDO> list = dictMapper.selectList(new LambdaQueryWrapper<LedgerDictDO>()
                .eq(LedgerDictDO::getType, type)
                .eq(LedgerDictDO::getStatus, 1)
                .eq(LedgerDictDO::getDeleted, 0)
                .orderByAsc(LedgerDictDO::getSortOrder)
                .orderByAsc(LedgerDictDO::getId));
        return list.stream().map(this::toDictVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LedgerDictVO addDict(LedgerDictCreateDTO dto) {
        if (dto.getType() == null || (dto.getType() != 1 && dto.getType() != 2)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "type 必须是 1(平台) 或 2(项目)");
        }
        LedgerDictDO d = new LedgerDictDO();
        d.setType(dto.getType());
        d.setName(dto.getName().trim());
        d.setColor(StringUtils.isBlank(dto.getColor()) ? "#6b7280" : dto.getColor());
        d.setIcon(dto.getIcon());
        // 追加到末尾: 取当前最大 sort_order + 1
        Integer maxSort = dictMapper.selectList(new LambdaQueryWrapper<LedgerDictDO>()
                        .eq(LedgerDictDO::getType, dto.getType())
                        .eq(LedgerDictDO::getDeleted, 0))
                .stream()
                .map(LedgerDictDO::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(-1);
        d.setSortOrder(maxSort + 1);
        d.setStatus(1);
        dictMapper.insert(d);
        log.info("Ledger dict added: id={}, type={}, name={}", d.getId(), d.getType(), d.getName());
        return toDictVO(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LedgerRecordDO addRecord(LedgerRecordCreateDTO dto) {
        // 校验: itemDictId 与 itemText 二选一
        if (dto.getItemDictId() == null && StringUtils.isBlank(dto.getItemText())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "项目字典ID 和 自定义文本 至少填一个");
        }
        if (dto.getItemDictId() != null && StringUtils.isNotBlank(dto.getItemText())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "项目字典ID 和 自定义文本 不能同时填");
        }
        // 校验 dict 存在
        LedgerDictDO platform = dictMapper.selectById(dto.getPlatformDictId());
        if (platform == null || platform.getDeleted() != 0 || !Integer.valueOf(1).equals(platform.getType())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "平台字典项不存在");
        }
        if (dto.getItemDictId() != null) {
            LedgerDictDO item = dictMapper.selectById(dto.getItemDictId());
            if (item == null || item.getDeleted() != 0 || !Integer.valueOf(2).equals(item.getType())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "项目字典项不存在");
            }
        }
        LedgerRecordDO r = new LedgerRecordDO();
        r.setRecordDate(dto.getRecordDate());
        r.setPlatformDictId(dto.getPlatformDictId());
        r.setItemDictId(dto.getItemDictId());
        r.setItemText(StringUtils.isBlank(dto.getItemText()) ? null : dto.getItemText().trim());
        r.setAmount(dto.getAmount());
        r.setDeviceId(dto.getDeviceId());
        recordMapper.insert(r);
        log.info("Ledger record added: id={}, platformId={}, amount={}", r.getId(), r.getPlatformDictId(), r.getAmount());
        return r;
    }

    @Override
    public List<LedgerRecordVO> listRecentRecord(String deviceId, Integer limit) {
        if (StringUtils.isBlank(deviceId)) {
            return Collections.emptyList();
        }
        int n = limit == null ? 100 : Math.max(1, Math.min(limit, 200));
        List<LedgerRecordDO> records = recordMapper.selectList(new LambdaQueryWrapper<LedgerRecordDO>()
                .eq(LedgerRecordDO::getDeviceId, deviceId)
                .eq(LedgerRecordDO::getDeleted, 0)
                .orderByDesc(LedgerRecordDO::getRecordDate)
                .orderByDesc(LedgerRecordDO::getId)
                .last("LIMIT " + n));
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        // 收集所有 dictId, 一次性批量回查
        Set<Long> platformIds = records.stream().map(LedgerRecordDO::getPlatformDictId).collect(Collectors.toSet());
        Set<Long> itemIds = records.stream()
                .map(LedgerRecordDO::getItemDictId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, LedgerDictDO> dictMap = new HashMap<>();
        if (!platformIds.isEmpty() || !itemIds.isEmpty()) {
            Set<Long> all = new java.util.HashSet<>();
            all.addAll(platformIds);
            all.addAll(itemIds);
            List<LedgerDictDO> dicts = dictMapper.selectBatchIds(all);
            for (LedgerDictDO d : dicts) {
                dictMap.put(d.getId(), d);
            }
        }
        return records.stream().map(r -> toRecordVO(r, dictMap)).collect(Collectors.toList());
    }

    private LedgerRecordVO toRecordVO(LedgerRecordDO r, Map<Long, LedgerDictDO> dictMap) {
        LedgerRecordVO vo = new LedgerRecordVO();
        vo.setId(r.getId());
        vo.setRecordDate(r.getRecordDate());
        vo.setAmount(r.getAmount());
        vo.setCreatedAt(r.getCreatedAt());

        LedgerDictDO platform = dictMap.get(r.getPlatformDictId());
        if (platform != null) {
            vo.setPlatformDictId(platform.getId());
            vo.setPlatformName(platform.getName());
            vo.setPlatformColor(platform.getColor());
            vo.setPlatformIcon(platform.getIcon());
        }

        if (r.getItemDictId() != null) {
            LedgerDictDO item = dictMap.get(r.getItemDictId());
            if (item != null) {
                vo.setItemName(item.getName());
                vo.setItemColor(item.getColor());
            } else {
                vo.setItemName("(已删除项目)");
            }
        } else {
            vo.setItemName(r.getItemText());
        }
        return vo;
    }

    private LedgerDictVO toDictVO(LedgerDictDO d) {
        LedgerDictVO vo = new LedgerDictVO();
        vo.setId(d.getId());
        vo.setType(d.getType());
        vo.setName(d.getName());
        vo.setColor(d.getColor());
        vo.setIcon(d.getIcon());
        vo.setSortOrder(d.getSortOrder());
        vo.setStatus(d.getStatus());
        vo.setCreatedAt(d.getCreatedAt());
        return vo;
    }
}
