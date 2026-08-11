package com.pureblog.search.vo;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private List<SearchVO> articles;
    private long total;
    private long page;
    private long size;
    private long totalPages;
    private long tookMs;
}
