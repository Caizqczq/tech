package com.mtm.backend.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialQueryDTO {
    private String materialType;  // document, audio
    private String subject;
    private String courseLevel;
    private String documentType;
    private String audioType;
    private String keywords;
    private Integer page;
    private Integer limit;
    private String sortBy;        // created_at, file_size, title
    private String sortOrder;     // asc, desc
}