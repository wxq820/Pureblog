package com.pureblog.common.base;

import lombok.Data;

@Data
public abstract class PageBase {
    private Integer page = 1;
    private Integer size = 10;

    public Long getOffset() {
        return (long) (page - 1) * size;
    }
}
