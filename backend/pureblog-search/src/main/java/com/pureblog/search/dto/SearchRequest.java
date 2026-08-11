package com.pureblog.search.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String keyword;
    private Long categoryId;
    private Long tagId;
    private String sortBy = "relevance";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer size = 10;
}
