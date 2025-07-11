package com.mtm.backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceQueryDTO {
    private String resourceType;
    private String subject;
    private String courseLevel;
    private String keywords;
}