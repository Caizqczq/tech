package com.mtm.backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUploadDTO {
    private String subject;
    private String courseLevel;
    private String resourceType; // 修改为resourceType以符合接口文档
    private String title;
    private String description;
    private String keywords;
    private Boolean autoVectorize;
    private Boolean autoExtractKeywords;
}