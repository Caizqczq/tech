package com.mtm.backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchUploadDTO {
    private String subject;
    private String courseLevel;
    private Boolean autoVectorize = true;
}