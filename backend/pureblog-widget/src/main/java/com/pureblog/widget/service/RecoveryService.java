package com.pureblog.widget.service;

import com.pureblog.widget.dto.PositionCreateDTO;
import com.pureblog.widget.dto.PositionValueDTO;
import com.pureblog.widget.entity.PositionDO;
import com.pureblog.widget.vo.PositionVO;
import com.pureblog.widget.vo.RecoveryDashboardVO;

import java.util.List;

public interface RecoveryService {

    RecoveryDashboardVO dashboard();

    PositionDO savePosition(PositionCreateDTO dto);

    void saveValue(PositionValueDTO dto);

    void deletePosition(Long id);
}
