package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptionTaskVO {
    private String taskId;
    private String materialId;
    private String message;
    private Integer estimatedTime;
    private String status;
    private Integer progress;
    private String statusUrl;
    
    // 完成时的结果
    private TranscriptionResult result;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TranscriptionResult {
        private String audioId;
        private TranscriptionDetail transcription;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TranscriptionDetail {
        private String text;
        private Double confidence;
        private Integer duration;
    }
}