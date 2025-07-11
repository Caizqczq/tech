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
public class BatchUploadResultVO {
    private String batchId;
    private Integer totalFiles;
    private Integer successCount;
    private Integer failedCount;
    private List<Map<String, Object>> results;
    private String processingStatus;
}