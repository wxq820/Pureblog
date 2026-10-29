package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公开: 单条记账记录的展示 VO.
 * 字典名/颜色/图标已就地内联, 前端拿到即可直接渲染, 无需再回查.
 */
@Data
public class LedgerRecordVO {

    private Long id;

    private LocalDate recordDate;

    private Long platformDictId;
    private String platformName;
    private String platformColor;
    private String platformIcon;

    /** 二选一: 字典项时为 name, 自定义文本时为 itemText 原值 */
    private String itemName;
    private String itemColor;

    private BigDecimal amount;

    private LocalDateTime createdAt;
}
