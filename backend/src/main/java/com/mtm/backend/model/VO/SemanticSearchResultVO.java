package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SemanticSearchResultVO {
    private List<Map<String, Object>> results;
    private String query;
    private Integer totalResults;
    private Double searchTime;
}