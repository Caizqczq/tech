package com.mtm.backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioUploadDTO {
    private String transcriptionMode = "sync"; // sync/async/stream
    private Boolean needTranscription = true;
    private String subject;
    private String resourceType; // 修改为resourceType以符合接口文档
    private String description;
    private String speaker;
    private String language = "zh"; // zh/en
    private Boolean autoVectorize = true;
}