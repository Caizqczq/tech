package com.mtm.backend.service.preview;

import com.mtm.backend.model.VO.FilePreviewVO;
import com.mtm.backend.repository.TeachingResource;
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 音频预览策略
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AudioPreviewStrategy implements FilePreviewStrategy {
    
    private final LocalFileUtil localFileUtil;
    
    @Override
    public boolean supports(String contentType) {
        return contentType != null && contentType.startsWith("audio/");
    }
    
    @Override
    public FilePreviewVO generatePreview(TeachingResource resource) {
        try {
            String previewUrl = localFileUtil.generateUrl(resource.getFilePath());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("duration", resource.getDuration());
            metadata.put("speaker", resource.getSpeaker());
            metadata.put("language", resource.getLanguage());
            metadata.put("size", resource.getFileSize());
            
            return FilePreviewVO.builder()
                    .previewType("audio")
                    .previewUrl(previewUrl)
                    .originalUrl(previewUrl)
                    .previewable(true)
                    .textContent(resource.getTranscriptionText())
                    .metadata(metadata)
                    .build();
                    
        } catch (Exception e) {
            log.error("生成音频预览失败: {}", e.getMessage());
            return FilePreviewVO.builder()
                    .previewType("audio")
                    .previewable(false)
                    .errorMessage("音频预览生成失败: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public String getPreviewType() {
        return "audio";
    }
}