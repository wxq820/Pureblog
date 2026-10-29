package com.pureblog.widget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.widget.dto.AccountCategoryCreateDTO;
import com.pureblog.widget.dto.AccountCreateDTO;
import com.pureblog.widget.dto.NetWorthSnapshotCreateDTO;
import com.pureblog.widget.entity.AccountCategoryDO;
import com.pureblog.widget.entity.AccountDO;
import com.pureblog.widget.entity.BalanceEntryDO;
import com.pureblog.widget.entity.BalanceSnapshotDO;
import com.pureblog.widget.mapper.AccountCategoryMapper;
import com.pureblog.widget.mapper.AccountMapper;
import com.pureblog.widget.mapper.BalanceEntryMapper;
import com.pureblog.widget.mapper.BalanceSnapshotMapper;
import com.pureblog.widget.service.NetWorthService;
import com.pureblog.widget.vo.AccountCategoryVO;
import com.pureblog.widget.vo.AccountVO;
import com.pureblog.widget.vo.NetWorthEntryVO;
import com.pureblog.widget.vo.NetWorthSnapshotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetWorthServiceImpl implements NetWorthService {

    private static final Set<Long> SEED_CATEGORY_IDS = Set.of(1L, 2L, 3L);

    private final BalanceSnapshotMapper snapshotMapper;
    private final BalanceEntryMapper entryMapper;
    private final AccountCategoryMapper categoryMapper;
    private final AccountMapper accountMapper;

    // ============================================================
    // 登录态: 快照 CRUD
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NetWorthSnapshotVO saveSnapshot(NetWorthSnapshotCreateDTO dto) {
        if (dto.getEntries() == null || dto.getEntries().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "快照明细不能为空");
        }
        // 校验: 同一 (snapshot, account) 唯一 — 由 DB UNIQUE KEY 保证, 前端也要防重
        // 这里先按 accountId 去重
        Map<Long, NetWorthSnapshotCreateDTO.Entry> dedup = new HashMap<>();
        for (NetWorthSnapshotCreateDTO.Entry e : dto.getEntries()) {
            if (dedup.containsKey(e.getAccountId())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "同一账户在同一快照中只能出现一次: accountId=" + e.getAccountId());
            }
            dedup.put(e.getAccountId(), e);
        }
        // 校验: 账户和分类都存在
        Set<Long> accountIds = dedup.keySet();
        Set<Long> categoryIds = dedup.values().stream().map(NetWorthSnapshotCreateDTO.Entry::getCategoryId).collect(Collectors.toSet());
        Map<Long, AccountDO> accMap = accountMapper.selectBatchIds(accountIds).stream()
                .collect(Collectors.toMap(AccountDO::getId, a -> a));
        if (accMap.size() != accountIds.size()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "存在不存在的账户");
        }
        Map<Long, AccountCategoryDO> catMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(AccountCategoryDO::getId, c -> c));
        if (catMap.size() != categoryIds.size()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "存在不存在的分类");
        }

        BalanceSnapshotDO snap = new BalanceSnapshotDO();
        snap.setSnapshotAt(dto.getSnapshotAt());
        snap.setEventTag(StringUtils.isBlank(dto.getEventTag()) ? null : dto.getEventTag().trim());
        snap.setRemark(StringUtils.isBlank(dto.getRemark()) ? null : dto.getRemark().trim());
        snapshotMapper.insert(snap);

        List<BalanceEntryDO> entries = new ArrayList<>();
        for (NetWorthSnapshotCreateDTO.Entry e : dedup.values()) {
            BalanceEntryDO be = new BalanceEntryDO();
            be.setSnapshotId(snap.getId());
            be.setAccountId(e.getAccountId());
            be.setCategoryId(e.getCategoryId());
            be.setBalance(parseBalance(e.getBalance()));
            entries.add(be);
        }
        for (BalanceEntryDO be : entries) {
            entryMapper.insert(be);
        }
        log.info("NetWorth snapshot saved: id={}, entries={}", snap.getId(), entries.size());

        return buildSnapshotVO(snap, entries, accMap, catMap);
    }

    @Override
    public List<NetWorthSnapshotVO> listSnapshots() {
        List<BalanceSnapshotDO> snaps = snapshotMapper.selectList(new LambdaQueryWrapper<BalanceSnapshotDO>()
                .eq(BalanceSnapshotDO::getDeleted, 0)
                .orderByDesc(BalanceSnapshotDO::getSnapshotAt)
                .orderByDesc(BalanceSnapshotDO::getId));
        if (snaps.isEmpty()) return Collections.emptyList();
        return assembleSnapshotVOs(snaps);
    }

    @Override
    public List<NetWorthEntryVO> listEntries(Long snapshotId) {
        BalanceSnapshotDO snap = snapshotMapper.selectById(snapshotId);
        if (snap == null || snap.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "快照不存在");
        }
        List<BalanceEntryDO> entries = entryMapper.selectList(new LambdaQueryWrapper<BalanceEntryDO>()
                .eq(BalanceEntryDO::getSnapshotId, snapshotId));
        if (entries.isEmpty()) return Collections.emptyList();
        Set<Long> accountIds = entries.stream().map(BalanceEntryDO::getAccountId).collect(Collectors.toSet());
        Set<Long> categoryIds = entries.stream().map(BalanceEntryDO::getCategoryId).collect(Collectors.toSet());
        Map<Long, AccountDO> accMap = accountMapper.selectBatchIds(accountIds).stream()
                .collect(Collectors.toMap(AccountDO::getId, a -> a));
        Map<Long, AccountCategoryDO> catMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(AccountCategoryDO::getId, c -> c));
        return entries.stream().map(e -> {
            NetWorthEntryVO vo = new NetWorthEntryVO();
            vo.setId(e.getId());
            vo.setSnapshotId(e.getSnapshotId());
            vo.setAccountId(e.getAccountId());
            AccountDO a = accMap.get(e.getAccountId());
            if (a != null) vo.setAccountName(a.getName());
            vo.setCategoryId(e.getCategoryId());
            AccountCategoryDO c = catMap.get(e.getCategoryId());
            if (c != null) vo.setCategoryName(c.getName());
            vo.setBalance(e.getBalance());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public NetWorthSnapshotVO latest() {
        BalanceSnapshotDO snap = snapshotMapper.selectOne(new LambdaQueryWrapper<BalanceSnapshotDO>()
                .eq(BalanceSnapshotDO::getDeleted, 0)
                .orderByDesc(BalanceSnapshotDO::getSnapshotAt)
                .orderByDesc(BalanceSnapshotDO::getId)
                .last("LIMIT 1"));
        if (snap == null) return null;
        List<NetWorthSnapshotVO> list = assembleSnapshotVOs(List.of(snap));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSnapshot(Long id) {
        BalanceSnapshotDO snap = snapshotMapper.selectById(id);
        if (snap == null || snap.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "快照不存在");
        }
        snapshotMapper.deleteById(id);
        entryMapper.delete(new LambdaQueryWrapper<BalanceEntryDO>()
                .eq(BalanceEntryDO::getSnapshotId, id));
        log.info("NetWorth snapshot deleted: id={}", id);
    }

    // ============================================================
    // Admin: 分类
    // ============================================================

    @Override
    public List<AccountCategoryVO> listAllCategories() {
        List<AccountCategoryDO> list = categoryMapper.selectList(new LambdaQueryWrapper<AccountCategoryDO>()
                .eq(AccountCategoryDO::getDeleted, 0)
                .orderByAsc(AccountCategoryDO::getSortOrder)
                .orderByAsc(AccountCategoryDO::getId));
        return list.stream().map(this::toCatVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountCategoryDO createCategory(AccountCategoryCreateDTO dto) {
        AccountCategoryDO c = new AccountCategoryDO();
        c.setName(dto.getName().trim());
        c.setSortOrder(dto.getSortOrder() == null ? nextCategorySort() : dto.getSortOrder());
        c.setIsCountedInTotal(dto.getIsCountedInTotal() == null ? 1 : dto.getIsCountedInTotal());
        c.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        categoryMapper.insert(c);
        log.info("Account category created: id={}, name={}", c.getId(), c.getName());
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, AccountCategoryCreateDTO dto) {
        AccountCategoryDO c = categoryMapper.selectById(id);
        if (c == null || c.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "分类不存在");
        }
        if (StringUtils.isNotBlank(dto.getName())) c.setName(dto.getName().trim());
        if (dto.getSortOrder() != null) c.setSortOrder(dto.getSortOrder());
        if (dto.getIsCountedInTotal() != null) c.setIsCountedInTotal(dto.getIsCountedInTotal());
        if (dto.getStatus() != null) c.setStatus(dto.getStatus());
        categoryMapper.updateById(c);
        log.info("Account category updated: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        if (SEED_CATEGORY_IDS.contains(id)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "seed 分类不可删除");
        }
        AccountCategoryDO c = categoryMapper.selectById(id);
        if (c == null || c.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "分类不存在");
        }
        // 校验: 该分类下还有未删账户? 账户没有 category_id 字段, 检查 entry
        Long entryCount = entryMapper.selectCount(new LambdaQueryWrapper<BalanceEntryDO>()
                .eq(BalanceEntryDO::getCategoryId, id));
        if (entryCount != null && entryCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该分类下存在快照明细, 不可删除");
        }
        categoryMapper.deleteById(id);
        log.info("Account category deleted: id={}", id);
    }

    // ============================================================
    // Admin: 账户
    // ============================================================

    @Override
    public List<AccountVO> listAllAccounts() {
        List<AccountDO> list = accountMapper.selectList(new LambdaQueryWrapper<AccountDO>()
                .eq(AccountDO::getDeleted, 0)
                .orderByAsc(AccountDO::getSortOrder)
                .orderByAsc(AccountDO::getId));
        return list.stream().map(this::toAccVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountDO createAccount(AccountCreateDTO dto) {
        AccountDO a = new AccountDO();
        a.setName(dto.getName().trim());
        a.setIcon(dto.getIcon());
        a.setColor(StringUtils.isBlank(dto.getColor()) ? null : dto.getColor());
        a.setSortOrder(dto.getSortOrder() == null ? nextAccountSort() : dto.getSortOrder());
        a.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        accountMapper.insert(a);
        log.info("Account created: id={}, name={}", a.getId(), a.getName());
        return a;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccount(Long id, AccountCreateDTO dto) {
        AccountDO a = accountMapper.selectById(id);
        if (a == null || a.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "账户不存在");
        }
        if (StringUtils.isNotBlank(dto.getName())) a.setName(dto.getName().trim());
        if (dto.getIcon() != null) a.setIcon(dto.getIcon());
        if (dto.getColor() != null) a.setColor(StringUtils.isBlank(dto.getColor()) ? null : dto.getColor());
        if (dto.getSortOrder() != null) a.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) a.setStatus(dto.getStatus());
        accountMapper.updateById(a);
        log.info("Account updated: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long id) {
        AccountDO a = accountMapper.selectById(id);
        if (a == null || a.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "账户不存在");
        }
        // 校验: entry 中是否还有引用
        Long entryCount = entryMapper.selectCount(new LambdaQueryWrapper<BalanceEntryDO>()
                .eq(BalanceEntryDO::getAccountId, id));
        if (entryCount != null && entryCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "该账户下存在快照明细, 不可删除");
        }
        accountMapper.deleteById(id);
        log.info("Account deleted: id={}", id);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private Integer nextCategorySort() {
        Integer max = categoryMapper.selectList(new LambdaQueryWrapper<AccountCategoryDO>()
                        .eq(AccountCategoryDO::getDeleted, 0))
                .stream().map(AccountCategoryDO::getSortOrder).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(-1);
        return max + 1;
    }

    private Integer nextAccountSort() {
        Integer max = accountMapper.selectList(new LambdaQueryWrapper<AccountDO>()
                        .eq(AccountDO::getDeleted, 0))
                .stream().map(AccountDO::getSortOrder).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(-1);
        return max + 1;
    }

    /**
     * 组装多条快照 VO, 实时算 prev_total + delta_to_prev + per-category 合计.
     * 输入按 snapshotAt desc, 所以前一条(prev) 在 i+1.
     */
    private List<NetWorthSnapshotVO> assembleSnapshotVOs(List<BalanceSnapshotDO> snaps) {
        if (snaps.isEmpty()) return Collections.emptyList();
        Set<Long> snapIds = snaps.stream().map(BalanceSnapshotDO::getId).collect(Collectors.toSet());
        List<BalanceEntryDO> allEntries = entryMapper.selectList(new LambdaQueryWrapper<BalanceEntryDO>()
                .in(BalanceEntryDO::getSnapshotId, snapIds));
        Map<Long, List<BalanceEntryDO>> entriesBySnap = allEntries.stream()
                .collect(Collectors.groupingBy(BalanceEntryDO::getSnapshotId));
        // 一次性查所有 account + category
        Set<Long> accountIds = allEntries.stream().map(BalanceEntryDO::getAccountId).collect(Collectors.toSet());
        Set<Long> categoryIds = allEntries.stream().map(BalanceEntryDO::getCategoryId).collect(Collectors.toSet());
        Map<Long, AccountDO> accMap = accountIds.isEmpty() ? Map.of() :
                accountMapper.selectBatchIds(accountIds).stream()
                        .collect(Collectors.toMap(AccountDO::getId, x -> x));
        Map<Long, AccountCategoryDO> catMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(AccountCategoryDO::getId, x -> x));

        List<NetWorthSnapshotVO> result = new ArrayList<>();
        for (int i = 0; i < snaps.size(); i++) {
            BalanceSnapshotDO snap = snaps.get(i);
            List<BalanceEntryDO> entries = entriesBySnap.getOrDefault(snap.getId(), Collections.emptyList());
            NetWorthSnapshotVO vo = buildSnapshotVO(snap, entries, accMap, catMap);
            // prev = i+1 (因为 desc 排序)
            if (i + 1 < snaps.size()) {
                BalanceSnapshotDO prev = snaps.get(i + 1);
                List<BalanceEntryDO> prevEntries = entriesBySnap.getOrDefault(prev.getId(), Collections.emptyList());
                BigDecimal prevTotal = sumCounted(prevEntries, catMap);
                vo.setPrevTotal(prevTotal);
                if (vo.getTotal() != null && prevTotal != null) {
                    vo.setDeltaToPrev(vo.getTotal().subtract(prevTotal));
                }
            }
            result.add(vo);
        }
        return result;
    }

    private NetWorthSnapshotVO buildSnapshotVO(BalanceSnapshotDO snap,
                                               List<BalanceEntryDO> entries,
                                               Map<Long, AccountDO> accMap,
                                               Map<Long, AccountCategoryDO> catMap) {
        NetWorthSnapshotVO vo = new NetWorthSnapshotVO();
        vo.setId(snap.getId());
        vo.setSnapshotAt(snap.getSnapshotAt());
        vo.setEventTag(snap.getEventTag());
        vo.setRemark(snap.getRemark());
        vo.setCreatedAt(snap.getCreatedAt());
        // per-category 合计 (全部分类, 不止 counted 的)
        Map<Long, BigDecimal> sums = new HashMap<>();
        for (BalanceEntryDO e : entries) {
            sums.merge(e.getCategoryId(), e.getBalance() == null ? BigDecimal.ZERO : e.getBalance(), BigDecimal::add);
        }
        List<NetWorthSnapshotVO.CategoryTotal> perCat = new ArrayList<>();
        // 用全部分类(避免漏掉)
        Set<Long> allCatIds = new HashSet<>();
        allCatIds.addAll(catMap.keySet());
        allCatIds.addAll(sums.keySet());
        for (Long catId : allCatIds) {
            AccountCategoryDO c = catMap.get(catId);
            NetWorthSnapshotVO.CategoryTotal ct = new NetWorthSnapshotVO.CategoryTotal();
            ct.setCategoryId(catId);
            ct.setCategoryName(c == null ? "(已删除)" : c.getName());
            ct.setIsCountedInTotal(c == null ? 0 : c.getIsCountedInTotal());
            ct.setTotal(sums.getOrDefault(catId, BigDecimal.ZERO));
            perCat.add(ct);
        }
        perCat.sort(Comparator.comparing(NetWorthSnapshotVO.CategoryTotal::getCategoryId));
        vo.setPerCategoryTotals(perCat);
        // grand total = counted 之和
        vo.setTotal(sumCounted(entries, catMap));
        return vo;
    }

    private BigDecimal sumCounted(List<BalanceEntryDO> entries, Map<Long, AccountCategoryDO> catMap) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BalanceEntryDO e : entries) {
            AccountCategoryDO c = catMap.get(e.getCategoryId());
            int counted = (c == null) ? 0 : (c.getIsCountedInTotal() == null ? 0 : c.getIsCountedInTotal());
            if (counted == 1) {
                sum = sum.add(e.getBalance() == null ? BigDecimal.ZERO : e.getBalance());
            }
        }
        return sum;
    }

    private BigDecimal parseBalance(String s) {
        if (StringUtils.isBlank(s)) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "余额格式不合法: " + s);
        }
    }

    private AccountCategoryVO toCatVO(AccountCategoryDO c) {
        AccountCategoryVO vo = new AccountCategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSortOrder(c.getSortOrder());
        vo.setIsCountedInTotal(c.getIsCountedInTotal());
        vo.setStatus(c.getStatus());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }

    private AccountVO toAccVO(AccountDO a) {
        AccountVO vo = new AccountVO();
        vo.setId(a.getId());
        vo.setName(a.getName());
        vo.setIcon(a.getIcon());
        vo.setColor(a.getColor());
        vo.setSortOrder(a.getSortOrder());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        return vo;
    }
}
