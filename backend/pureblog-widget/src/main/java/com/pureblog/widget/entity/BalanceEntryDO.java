package com.pureblog.widget.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pb_balance_entry")
public class BalanceEntryDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long snapshotId;
    private Long accountId;
    /** 录入快照时的 category_id, 历史快照保留当时分类 */
    private Long categoryId;
    private BigDecimal balance;
}
