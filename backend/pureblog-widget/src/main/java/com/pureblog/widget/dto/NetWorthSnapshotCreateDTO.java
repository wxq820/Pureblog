package com.pureblog.widget.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NetWorthSnapshotCreateDTO {

    @NotNull(message = "快照日期不能为空")
    private LocalDateTime snapshotAt;

    private String eventTag;
    private String remark;

    @NotEmpty(message = "快照明细不能为空")
    @Valid
    private List<Entry> entries;

    @Data
    public static class Entry {
        @NotNull
        private Long accountId;
        @NotNull
        private Long categoryId;
        private String balance; // string from frontend, parse in service
    }
}
