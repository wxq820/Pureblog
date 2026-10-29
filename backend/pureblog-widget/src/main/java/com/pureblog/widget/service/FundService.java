package com.pureblog.widget.service;

import com.pureblog.widget.dto.FundBuyDTO;
import com.pureblog.widget.dto.FundCreateDTO;
import com.pureblog.widget.dto.FundNavDTO;
import com.pureblog.widget.entity.FundDO;
import com.pureblog.widget.vo.FundDashboardVO;

public interface FundService {

    FundDashboardVO dashboard();

    FundDO saveFund(FundCreateDTO dto);

    void saveBuy(FundBuyDTO dto);

    void saveNav(FundNavDTO dto);

    void deleteBuy(Long id);

    void deleteFund(Long id);
}
