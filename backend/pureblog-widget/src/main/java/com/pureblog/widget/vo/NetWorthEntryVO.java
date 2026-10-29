package com.pureblog.widget.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NetWorthEntryVO {

    private Long id;
    private Long snapshotId;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal balance;
}
