package com.pureblog.widget.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pb_account_category")
public class AccountCategoryDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Integer sortOrder;
    /** 是否计入"目前完整身家". 0/1 = 活钱/定期 = 1, 0 余额 = 0 */
    private Integer isCountedInTotal;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
