package com.pureblog.widget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.widget.dto.FundBuyDTO;
import com.pureblog.widget.dto.FundCreateDTO;
import com.pureblog.widget.dto.FundNavDTO;
import com.pureblog.widget.entity.FundBuyDO;
import com.pureblog.widget.entity.FundDO;
import com.pureblog.widget.entity.FundNavDO;
import com.pureblog.widget.mapper.FundBuyMapper;
import com.pureblog.widget.mapper.FundMapper;
import com.pureblog.widget.mapper.FundNavMapper;
import com.pureblog.widget.service.FundService;
import com.pureblog.widget.vo.FundBuyVO;
import com.pureblog.widget.vo.FundDashboardVO;
import com.pureblog.widget.vo.FundVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundServiceImpl implements FundService {

    private final FundMapper fundMapper;
    private final FundBuyMapper buyMapper;
    private final FundNavMapper navMapper;

    @Override
    public FundDashboardVO dashboard() {
        List<FundDO> funds = fundMapper.selectList(new LambdaQueryWrapper<FundDO>()
                .eq(FundDO::getDeleted, 0)
                .orderByAsc(FundDO::getSortOrder)
                .orderByAsc(FundDO::getId));
        if (funds.isEmpty()) {
            FundDashboardVO d = new FundDashboardVO();
            d.setFunds(Collections.emptyList());
            return d;
        }
        List<Long> fundIds = funds.stream().map(FundDO::getId).collect(Collectors.toList());
        List<FundBuyDO> buys = buyMapper.selectList(new LambdaQueryWrapper<FundBuyDO>()
                .eq(FundBuyDO::getDeleted, 0)
                .in(FundBuyDO::getFundId, fundIds)
                .orderByDesc(FundBuyDO::getBuyDate)
                .orderByDesc(FundBuyDO::getId));
        Map<Long, List<FundBuyDO>> buysByFund = buys.stream().collect(Collectors.groupingBy(FundBuyDO::getFundId));
        List<FundNavDO> navs = navMapper.selectList(new LambdaQueryWrapper<FundNavDO>()
                .eq(FundNavDO::getDeleted, 0)
                .in(FundNavDO::getFundId, fundIds)
                .orderByDesc(FundNavDO::getRecordedAt));
        Map<Long, FundNavDO> latestNavByFund = navs.stream()
                .collect(Collectors.toMap(FundNavDO::getFundId, n -> n, (a, b) -> a));

        List<FundVO> vos = funds.stream()
                .map(f -> toVO(f, latestNavByFund.get(f.getId()), buysByFund.getOrDefault(f.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        FundDashboardVO d = new FundDashboardVO();
        d.setFunds(vos);
        return d;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FundDO saveFund(FundCreateDTO dto) {
        FundDO f;
        if (dto.getId() != null) {
            f = fundMapper.selectById(dto.getId());
            if (f == null || f.getDeleted() != 0) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "基金不存在");
            }
        } else {
            f = new FundDO();
            f.setSortOrder(dto.getSortOrder() == null ? nextSort() : dto.getSortOrder());
            f.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        }
        f.setName(dto.getName().trim());
        f.setCode(StringUtils.isBlank(dto.getCode()) ? null : dto.getCode().trim());
        f.setType(StringUtils.isBlank(dto.getType()) ? null : dto.getType().trim());
        f.setRemark(StringUtils.isBlank(dto.getRemark()) ? null : dto.getRemark().trim());
        if (dto.getStatus() != null) f.setStatus(dto.getStatus());
        if (dto.getId() == null) {
            fundMapper.insert(f);
            log.info("Fund created: id={}, name={}", f.getId(), f.getName());
        } else {
            fundMapper.updateById(f);
            log.info("Fund updated: id={}", f.getId());
        }
        return f;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBuy(FundBuyDTO dto) {
        FundDO f = fundMapper.selectById(dto.getFundId());
        if (f == null || f.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "基金不存在");
        }
        FundBuyDO b = new FundBuyDO();
        b.setFundId(dto.getFundId());
        b.setBuyDate(dto.getBuyDate());
        b.setPrice(dto.getPrice());
        b.setShares(dto.getShares());
        b.setAmount(dto.getPrice().multiply(dto.getShares()).setScale(2, RoundingMode.HALF_UP));
        b.setRemark(StringUtils.isBlank(dto.getRemark()) ? null : dto.getRemark().trim());
        buyMapper.insert(b);
        log.info("Fund buy saved: fundId={}, amount={}", dto.getFundId(), b.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNav(FundNavDTO dto) {
        FundDO f = fundMapper.selectById(dto.getFundId());
        if (f == null || f.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "基金不存在");
        }
        FundNavDO n = new FundNavDO();
        n.setFundId(dto.getFundId());
        n.setNav(dto.getNav());
        n.setRecordedAt(dto.getRecordedAt() == null ? LocalDateTime.now() : dto.getRecordedAt());
        n.setRemark(StringUtils.isBlank(dto.getRemark()) ? null : dto.getRemark().trim());
        navMapper.insert(n);
        log.info("Fund nav saved: fundId={}, nav={}", dto.getFundId(), dto.getNav());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBuy(Long id) {
        FundBuyDO b = buyMapper.selectById(id);
        if (b == null || b.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "买入记录不存在");
        }
        buyMapper.deleteById(id);
        log.info("Fund buy deleted: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFund(Long id) {
        FundDO f = fundMapper.selectById(id);
        if (f == null || f.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "基金不存在");
        }
        fundMapper.deleteById(id);
        buyMapper.delete(new LambdaQueryWrapper<FundBuyDO>().eq(FundBuyDO::getFundId, id));
        navMapper.delete(new LambdaQueryWrapper<FundNavDO>().eq(FundNavDO::getFundId, id));
        log.info("Fund deleted: id={}", id);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private Integer nextSort() {
        Integer max = fundMapper.selectList(new LambdaQueryWrapper<FundDO>()
                        .eq(FundDO::getDeleted, 0))
                .stream().map(FundDO::getSortOrder).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(-1);
        return max + 1;
    }

    private FundVO toVO(FundDO f, FundNavDO latestNav, List<FundBuyDO> buys) {
        FundVO vo = new FundVO();
        vo.setId(f.getId());
        vo.setName(f.getName());
        vo.setCode(f.getCode());
        vo.setType(f.getType());
        vo.setRemark(f.getRemark());
        vo.setSortOrder(f.getSortOrder());
        vo.setStatus(f.getStatus());
        vo.setCreatedAt(f.getCreatedAt());
        vo.setUpdatedAt(f.getUpdatedAt());
        BigDecimal nav = latestNav == null ? null : latestNav.getNav();
        vo.setLatestNav(nav);
        vo.setLatestNavAt(latestNav == null ? null : latestNav.getRecordedAt());
        BigDecimal totalShares = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<FundBuyVO> buyVOs = new ArrayList<>();
        for (FundBuyDO b : buys) {
            if (b.getShares() != null) totalShares = totalShares.add(b.getShares());
            if (b.getAmount() != null) totalCost = totalCost.add(b.getAmount());
            buyVOs.add(toBuyVO(b));
        }
        vo.setTotalShares(totalShares);
        vo.setTotalCost(totalCost);
        BigDecimal mv = (nav == null || totalShares.signum() == 0) ? BigDecimal.ZERO
                : nav.multiply(totalShares).setScale(2, RoundingMode.HALF_UP);
        vo.setMarketValue(mv);
        BigDecimal pnl = mv.subtract(totalCost);
        vo.setPnl(pnl);
        if (totalCost.signum() > 0) {
            vo.setPnlRate(pnl.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        } else {
            vo.setPnlRate(null);
        }
        vo.setBuys(buyVOs);
        return vo;
    }

    private FundBuyVO toBuyVO(FundBuyDO b) {
        FundBuyVO vo = new FundBuyVO();
        vo.setId(b.getId());
        vo.setFundId(b.getFundId());
        vo.setBuyDate(b.getBuyDate());
        vo.setPrice(b.getPrice());
        vo.setShares(b.getShares());
        vo.setAmount(b.getAmount());
        vo.setRemark(b.getRemark());
        vo.setCreatedAt(b.getCreatedAt());
        vo.setUpdatedAt(b.getUpdatedAt());
        return vo;
    }
}
