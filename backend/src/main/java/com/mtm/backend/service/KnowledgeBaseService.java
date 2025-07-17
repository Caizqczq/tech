package com.mtm.backend.service;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtm.backend.model.DTO.KnowledgeBaseCreateDTO;
import com.mtm.backend.model.VO.KnowledgeBaseListVO;
import com.mtm.backend.model.VO.KnowledgeBaseStatusVO;
import com.mtm.backend.model.VO.KnowledgeBaseVO;
import com.mtm.backend.repository.KnowledgeBase;
import com.mtm.backend.repository.TeachingResource;
import com.mtm.backend.repository.mapper.KnowledgeBaseMapper;
import com.mtm.backend.repository.mapper.TeachingResourceMapper;
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {
    
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final TeachingResourceMapper teachingResourceMapper;
    private final VectorStore vectorStore;
    private final LocalFileUtil localFileUtil;
    
    /**
     * 构建知识库
     */
    public KnowledgeBaseVO createKnowledgeBase(KnowledgeBaseCreateDTO createDTO, Integer userId) {
        try {
            // 验证资源是否存在且属于当前用户
            List<TeachingResource> resources = validateAndGetResources(createDTO.getResourceIds(), userId);
            
            // 创建知识库记录
            String knowledgeBaseId = generateKnowledgeBaseId();
            String taskId = generateTaskId();
            
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setId(knowledgeBaseId);
            knowledgeBase.setName(createDTO.getName());
            knowledgeBase.setDescription(createDTO.getDescription());
            knowledgeBase.setSubject(createDTO.getSubject());
            knowledgeBase.setCourseLevel(createDTO.getCourseLevel());
            knowledgeBase.setResourceIds(JSON.toJSONString(createDTO.getResourceIds()));
            knowledgeBase.setVectorStore(createDTO.getVectorStore() != null ? createDTO.getVectorStore() : "redis");
            knowledgeBase.setChunkSize(createDTO.getChunkSize() != null ? createDTO.getChunkSize() : 1000);
            knowledgeBase.setChunkOverlap(createDTO.getChunkOverlap() != null ? createDTO.getChunkOverlap() : 200);
            knowledgeBase.setStatus("processing");
            knowledgeBase.setProgress(0);
            knowledgeBase.setResourceCount(resources.size());
            knowledgeBase.setChunkCount(0);
            knowledgeBase.setDocumentCount(0);
            knowledgeBase.setMessage(truncateMessage("知识库构建任务已启动"));
            knowledgeBase.setUserId(userId);
            knowledgeBase.setCreatedAt(new Date());
            knowledgeBase.setUpdatedAt(new Date());
            
            knowledgeBaseMapper.insert(knowledgeBase);
            
            // 异步处理文档向量化
            processDocumentsAsync(knowledgeBaseId, resources);
            
            return KnowledgeBaseVO.builder()
                    .id(knowledgeBaseId)
                    .knowledgeBaseId(knowledgeBaseId)
                    .taskId(taskId)
                    .message("知识库构建任务已启动")
                    .estimatedTime(300)
                    .status("processing")
                    .resourceCount(resources.size())
                    .statusUrl("/api/resources/knowledge-base/" + knowledgeBaseId + "/status")
                    .build();
                    
        } catch (Exception e) {
            log.error("创建知识库失败", e);
            throw new RuntimeException("创建知识库失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询知识库状态
     */
    public KnowledgeBaseStatusVO getKnowledgeBaseStatus(String knowledgeBaseId, Integer userId) {
        try {
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
            if (knowledgeBase == null) {
                throw new RuntimeException("知识库不存在");
            }
            
            if (!knowledgeBase.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该知识库");
            }
            
            return KnowledgeBaseStatusVO.builder()
                    .knowledgeBaseId(knowledgeBase.getId())
                    .status(knowledgeBase.getStatus())
                    .progress(knowledgeBase.getProgress())
                    .resourceCount(knowledgeBase.getResourceCount())
                    .chunkCount(knowledgeBase.getChunkCount())
                    .message(knowledgeBase.getMessage())
                    .createdAt(knowledgeBase.getCreatedAt())
                    .completedAt(knowledgeBase.getCompletedAt())
                    .build();
                    
        } catch (Exception e) {
            log.error("查询知识库状态失败", e);
            throw new RuntimeException("查询知识库状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识库列表
     */
    public Map<String, Object> getKnowledgeBaseList(Integer userId, Pageable pageable) {
        try {
            QueryWrapper<KnowledgeBase> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByDesc("created_at");
            
            Page<KnowledgeBase> pageInfo = new Page<>(pageable.getPageNumber(), pageable.getPageSize());
            IPage<KnowledgeBase> knowledgeBasePage = knowledgeBaseMapper.selectPage(pageInfo, queryWrapper);
            
            List<KnowledgeBaseListVO> knowledgeBaseList = knowledgeBasePage.getRecords().stream()
                    .map(this::convertToKnowledgeBaseListVO)
                    .collect(Collectors.toList());
            
            return Map.of(
                "content", knowledgeBaseList,
                "totalElements", knowledgeBasePage.getTotal()
            );
            
        } catch (Exception e) {
            log.error("获取知识库列表失败", e);
            throw new RuntimeException("获取知识库列表失败: " + e.getMessage());
        }
    }
    
    // ============ 私有方法 ============
    
    private List<TeachingResource> validateAndGetResources(List<String> resourceIds, Integer userId) {
        List<TeachingResource> resources = new ArrayList<>();
        for (String resourceId : resourceIds) {
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在: " + resourceId);
            }
            if (!resource.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问资源: " + resourceId);
            }
            resources.add(resource);
        }
        return resources;
    }
    
    @Async
    public void processDocumentsAsync(String knowledgeBaseId, List<TeachingResource> resources) {
        try {
            log.info("开始处理知识库文档向量化: {}", knowledgeBaseId);
            
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
            TokenTextSplitter textSplitter = new TokenTextSplitter(
                knowledgeBase.getChunkSize(), 
                knowledgeBase.getChunkOverlap(), 
                5, 
                10000, 
                true
            );
            
            List<Document> allDocuments = new ArrayList<>();
            int processedCount = 0;
            
            for (TeachingResource resource : resources) {
                try {
                    List<Document> documents = processResource(resource, textSplitter, knowledgeBaseId);
                    allDocuments.addAll(documents);
                    
                    processedCount++;
                    int progress = (processedCount * 100) / resources.size();
                    updateKnowledgeBaseProgress(knowledgeBaseId, progress, "正在处理资源: " + resource.getOriginalName());
                    
                } catch (Exception e) {
                    log.error("处理资源失败: {}", resource.getId(), e);
                }
            }
            
            // 存储到向量数据库
            if (!allDocuments.isEmpty()) {
                vectorStore.add(allDocuments);
                log.info("成功向量化 {} 个文档块", allDocuments.size());
            }
            
            // 更新知识库状态
            knowledgeBase.setStatus("completed");
            knowledgeBase.setProgress(100);
            knowledgeBase.setChunkCount(allDocuments.size());
            knowledgeBase.setDocumentCount(allDocuments.size()); // 设置文档数量（兼容字段）
            knowledgeBase.setMessage(truncateMessage("知识库构建完成"));
            knowledgeBase.setCompletedAt(new Date());
            knowledgeBase.setUpdatedAt(new Date());
            knowledgeBaseMapper.updateById(knowledgeBase);
            
            log.info("知识库构建完成: {}", knowledgeBaseId);
            
        } catch (Exception e) {
            log.error("知识库构建失败: {}", knowledgeBaseId, e);
            updateKnowledgeBaseStatus(knowledgeBaseId, "failed", "构建失败: " + e.getMessage());
        }
    }
    
    private List<Document> processResource(TeachingResource resource, TokenTextSplitter textSplitter, String knowledgeBaseId) throws Exception {
        String downloadUrl = localFileUtil.generateUrl(resource.getFilePath());
        
        List<Document> documents = new ArrayList<>();
        
        if ("document".equals(resource.getResourceType())) {
            // 处理文档文件
            try (InputStream inputStream = new URL(downloadUrl).openStream()) {
                byte[] content = inputStream.readAllBytes();
                ByteArrayResource byteArrayResource = new ByteArrayResource(content);
                
                if (resource.getContentType().contains("pdf")) {
                    PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(byteArrayResource);
                    documents = pdfReader.get();
                } else {
                    TikaDocumentReader tikaReader = new TikaDocumentReader(byteArrayResource);
                    documents = tikaReader.get();
                }
            }
        } else if ("audio".equals(resource.getResourceType()) && resource.getTranscriptionText() != null) {
            // 处理音频转录文本
            Document audioDoc = new Document(resource.getTranscriptionText());
            audioDoc.getMetadata().put("resource_id", resource.getId());
            audioDoc.getMetadata().put("resource_type", "audio");
            audioDoc.getMetadata().put("title", resource.getOriginalName());
            documents.add(audioDoc);
        }
        
        // 为文档添加元数据
        for (Document doc : documents) {
            doc.getMetadata().put("knowledge_base_id", knowledgeBaseId);
            doc.getMetadata().put("resource_id", resource.getId());
            doc.getMetadata().put("subject", resource.getSubject());
            doc.getMetadata().put("course_level", resource.getCourseLevel());
        }
        
        // 分块处理
        return textSplitter.apply(documents);
    }
    
    private void updateKnowledgeBaseProgress(String knowledgeBaseId, int progress, String message) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setId(knowledgeBaseId);
        knowledgeBase.setProgress(progress);
        knowledgeBase.setMessage(truncateMessage(message));
        knowledgeBase.setUpdatedAt(new Date());
        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    private void updateKnowledgeBaseStatus(String knowledgeBaseId, String status, String message) {
        // 记录完整的错误信息到日志中
        if ("failed".equals(status) && message.length() > 490) {
            log.error("知识库构建失败，完整错误信息: {}", message);
        }

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setId(knowledgeBaseId);
        knowledgeBase.setStatus(status);
        knowledgeBase.setMessage(truncateMessage(message));
        knowledgeBase.setUpdatedAt(new Date());
        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    /**
     * 截断消息以适应数据库字段长度限制
     * @param message 原始消息
     * @return 截断后的消息
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return null;
        }

        final int MAX_LENGTH = 490; // 留10字符缓冲

        if (message.length() <= MAX_LENGTH) {
            return message;
        }

        // 截断并添加省略号
        String truncated = message.substring(0, MAX_LENGTH - 3) + "...";
        log.warn("消息被截断，原长度: {}, 截断后长度: {}", message.length(), truncated.length());

        return truncated;
    }
    
    private KnowledgeBaseListVO convertToKnowledgeBaseListVO(KnowledgeBase knowledgeBase) {
        return KnowledgeBaseListVO.builder()
                .id(knowledgeBase.getId())
                .name(knowledgeBase.getName())
                .description(knowledgeBase.getDescription())
                .subject(knowledgeBase.getSubject())
                .courseLevel(knowledgeBase.getCourseLevel())
                .status(knowledgeBase.getStatus())
                .resourceCount(knowledgeBase.getResourceCount())
                .chunkCount(knowledgeBase.getChunkCount())
                .createdAt(knowledgeBase.getCreatedAt())
                .lastUsed(knowledgeBase.getLastUsed())
                .build();
    }
    
    private String generateKnowledgeBaseId() {
        return "kb_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString().replace("-", "");
    }
}
