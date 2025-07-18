package com.mtm.backend.service.rag;

import com.mtm.backend.repository.TeachingResource;
import com.mtm.backend.repository.mapper.TeachingResourceMapper;
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 文档服务 - 负责文档的读取、分块和元数据管理
 * 基于Spring AI最佳实践的ETL Pipeline模式
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final TeachingResourceMapper teachingResourceMapper;
    private final TokenTextSplitter textSplitter;
    private final LocalFileUtil localFileUtil;


    /**
     * Extract: 从文件读取文档内容
     * 支持PDF和多种格式（通过Apache Tika）
     */
    public List<Document> extractDocuments(String resourceId) {
        try {
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new IllegalArgumentException("资源不存在: " + resourceId);
            }

            // 读取文件内容
            byte[] content = readFileContent(resource.getFilePath());
            ByteArrayResource byteArrayResource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return resource.getOriginalName();
                }
            };

            List<Document> documents;
            
            // 基于文件类型选择合适的DocumentReader
            if (resource.getContentType().contains("pdf")) {
                PagePdfDocumentReader reader = new PagePdfDocumentReader(byteArrayResource);
                documents = reader.get();
            } else {
                // 利用Apache Tika支持广泛文件格式
                TikaDocumentReader reader = new TikaDocumentReader(byteArrayResource);
                documents = reader.get();
            }

            log.info("成功提取文档 {} 的 {} 个原始片段", resourceId, documents.size());
            return documents;

        } catch (Exception e) {
            log.error("提取文档失败: {}", resourceId, e);
            throw new RuntimeException("文档提取失败", e);
        }
    }

    /**
     * Transform: 文档分块处理
     * 采用语义边界保护策略
     */
    public List<Document> chunkDocuments(List<Document> documents, String resourceId) {
        try {
            // 应用Spring AI推荐的分块策略
            List<Document> chunkedDocs = textSplitter.apply(documents);
            
            // 为每个分块添加必要的元数据
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            enhanceMetadata(chunkedDocs, resource);
            
            log.info("文档 {} 分块完成：{} 个分块", resourceId, chunkedDocs.size());
            return chunkedDocs;

        } catch (Exception e) {
            log.error("文档分块失败: {}", resourceId, e);
            throw new RuntimeException("文档分块失败", e);
        }
    }

    /**
     * 增强文档元数据
     * 遵循Spring AI的元数据最佳实践
     */
    private void enhanceMetadata(List<Document> documents, TeachingResource resource) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            Map<String, Object> metadata = doc.getMetadata();
            
            // 核心标识信息
            metadata.put("id", resource.getId() + "_chunk_" + i);
            metadata.put("resource_id", resource.getId());
            metadata.put("chunk_index", i);
            
            // 业务元数据
            metadata.put("title", resource.getTitle());
            metadata.put("subject", resource.getSubject());
            metadata.put("course_level", resource.getCourseLevel());
            metadata.put("document_type", resource.getDocumentType());
            metadata.put("keywords", resource.getKeywords());
            
            // 技术元数据
            metadata.put("source", resource.getOriginalName());
            metadata.put("file_type", resource.getContentType());
            metadata.put("file_size", resource.getFileSize());
            metadata.put("user_id", resource.getUserId());
            
            // 时间戳
            metadata.put("created_at", timestamp);
            metadata.put("processed_at", timestamp);
        }
    }

    /**
     * 读取文件内容 - 支持本地文件和URL
     */
    private byte[] readFileContent(String filePath) throws Exception {
        if (filePath.startsWith("http")) {
            // 远程文件
            try (var inputStream = new URL(filePath).openStream()) {
                return inputStream.readAllBytes();
            }
        } else {
            // 本地文件 - 使用LocalFileUtil读取
            return localFileUtil.readFileBytes(filePath);
        }
    }

    /**
     * 获取资源基本信息
     */
    public TeachingResource getResource(String resourceId) {
        return teachingResourceMapper.selectById(resourceId);
    }

    /**
     * 检查资源是否存在且用户有权限访问
     */
    public boolean validateAccess(String resourceId, Integer userId) {
        TeachingResource resource = teachingResourceMapper.selectById(resourceId);
        return resource != null && resource.getUserId().equals(userId);
    }
}