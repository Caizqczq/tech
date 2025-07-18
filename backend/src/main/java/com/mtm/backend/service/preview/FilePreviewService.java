package com.mtm.backend.service.preview;

import com.mtm.backend.model.VO.FilePreviewVO;
import com.mtm.backend.repository.TeachingResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文件预览服务
 * 统一管理不同类型文件的预览策略
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FilePreviewService {
    
    private final List<FilePreviewStrategy> previewStrategies;
    
    /**
     * 生成文件预览信息
     * @param resource 资源信息
     * @return 预览信息
     */
    public FilePreviewVO generatePreview(TeachingResource resource) {
        if (resource == null) {
            return FilePreviewVO.builder()
                    .previewType("unsupported")
                    .previewable(false)
                    .errorMessage("资源不存在")
                    .build();
        }
        
        // 查找支持的预览策略
        for (FilePreviewStrategy strategy : previewStrategies) {
            if (strategy.supports(resource.getContentType())) {
                log.info("使用 {} 策略预览文件: {}", strategy.getPreviewType(), resource.getOriginalName());
                return strategy.generatePreview(resource);
            }
        }
        
        // 没有找到合适的策略，返回不支持预览
        log.warn("不支持的文件类型: {}", resource.getContentType());
        return FilePreviewVO.builder()
                .previewType("unsupported")
                .previewable(false)
                .errorMessage("不支持的文件类型: " + resource.getContentType())
                .build();
    }
    
    /**
     * 检查文件是否支持预览
     * @param contentType 文件MIME类型
     * @return 是否支持预览
     */
    public boolean isPreviewSupported(String contentType) {
        return previewStrategies.stream()
                .anyMatch(strategy -> strategy.supports(contentType));
    }
    
    /**
     * 获取支持的预览类型
     * @return 预览类型列表
     */
    public List<String> getSupportedPreviewTypes() {
        return previewStrategies.stream()
                .map(FilePreviewStrategy::getPreviewType)
                .distinct()
                .toList();
    }
}