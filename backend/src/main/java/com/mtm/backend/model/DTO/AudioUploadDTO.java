package com.mtm.backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioUploadDTO {
    private String transcriptionMode = "sync"; // sync/async
    private Boolean needTranscription = true;
    private String subject;
    private String audioType;
    private String description;
    private String speaker;
    private String language = "zh";
}