package com.pureblog.widget.service;

import com.pureblog.widget.dto.LedgerDictCreateDTO;
import com.pureblog.widget.dto.LedgerRecordCreateDTO;
import com.pureblog.widget.entity.LedgerRecordDO;
import com.pureblog.widget.vo.LedgerDictVO;
import com.pureblog.widget.vo.LedgerRecordVO;

import java.util.List;

public interface LedgerService {

    /** 公开: 拉指定 type 的启用字典项. */
    List<LedgerDictVO> listDict(Integer type);

    /** 公开: 立即新增一个字典项 (任何人加, 全局可见). */
    LedgerDictVO addDict(LedgerDictCreateDTO dto);

    /** 公开: 新增一条记账. */
    LedgerRecordDO addRecord(LedgerRecordCreateDTO dto);

    /** 公开: 按设备ID查询最近的记账, 按日期倒序, 字典信息已内联. */
    List<LedgerRecordVO> listRecentRecord(String deviceId, Integer limit);
}
