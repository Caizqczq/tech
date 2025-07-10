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
    private String documentType;
    private String title;
    private String description;
    private String keywords;
}