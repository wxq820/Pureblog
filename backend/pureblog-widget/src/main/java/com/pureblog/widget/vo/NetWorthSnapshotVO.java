package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NetWorthSnapshotVO {

    private Long id;
    private LocalDateTime snapshotAt;
    private String eventTag;
    private String remark;
    private LocalDateTime createdAt;

    /** 当时快照所有账户余额之和 (按 is_counted_in_total=1 的 category 求和). */
    private BigDecimal total;

    /** 上一次快照的 total, 用于算"相较上次". 第一次为 null. */
    private BigDecimal prevTotal;

    /** total - prevTotal, 第一次为 null. */
    private BigDecimal deltaToPrev;

    /** 按 category 分组的合计. key = categoryId, value = sum */
    private List<CategoryTotal> perCategoryTotals;

    @Data
    public static class CategoryTotal {
        private Long categoryId;
        private String categoryName;
        private Integer isCountedInTotal;
        private BigDecimal total;
    }
}
