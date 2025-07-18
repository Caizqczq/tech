package com.mtm.backend.service.knowledge;

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
import com.mtm.backend.service.rag.DocumentService;
import com.mtm.backend.service.rag.VectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库服务 - 负责知识库的生命周期管理
 * 基于Spring AI最佳实践的分层架构
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final TeachingResourceMapper teachingResourceMapper;
    private final DocumentService documentService;
    private final VectorService vectorService;

    /**
     * 创建知识库
     */
    public KnowledgeBaseVO createKnowledgeBase(KnowledgeBaseCreateDTO createDTO, Integer userId) {
        try {
            // 验证资源权限
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
            knowledgeBase.setVectorStore("redis"); // 固定使用Redis
            knowledgeBase.setChunkSize(createDTO.getChunkSize() != null ? createDTO.getChunkSize() : 800); // Spring AI推荐值
            knowledgeBase.setChunkOverlap(createDTO.getChunkOverlap() != null ? createDTO.getChunkOverlap() : 200);
            knowledgeBase.setStatus("processing");
            knowledgeBase.setProgress(0);
            knowledgeBase.setResourceCount(resources.size());
            knowledgeBase.setChunkCount(0);
            knowledgeBase.setDocumentCount(0);
            knowledgeBase.setMessage("知识库构建任务已启动");
            knowledgeBase.setUserId(userId);
            knowledgeBase.setCreatedAt(new Date());
            knowledgeBase.setUpdatedAt(new Date());
            
            knowledgeBaseMapper.insert(knowledgeBase);
            
            // 异步处理文档向量化（ETL Pipeline）
            processDocumentsAsync(knowledgeBaseId, createDTO.getResourceIds());
            
            return KnowledgeBaseVO.builder()
                .id(knowledgeBaseId)
                .knowledgeBaseId(knowledgeBaseId)
                .taskId(taskId)
                .message("知识库构建任务已启动")
                .estimatedTime(calculateEstimatedTime(resources.size()))
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
     * 异步处理文档向量化 - 基于Spring AI ETL Pipeline
     */
    @Async
    public void processDocumentsAsync(String knowledgeBaseId, List<String> resourceIds) {
        try {
            log.info("开始处理知识库文档向量化: {}", knowledgeBaseId);
            
            List<Document> allDocuments = new ArrayList<>();
            int processedCount = 0;
            
            for (String resourceId : resourceIds) {
                try {
                    // Extract: 提取文档内容
                    List<Document> rawDocuments = documentService.extractDocuments(resourceId);
                    
                    // Transform: 文档分块
                    List<Document> chunkedDocuments = documentService.chunkDocuments(rawDocuments, resourceId);
                    
                    allDocuments.addAll(chunkedDocuments);
                    
                    processedCount++;
                    int progress = (processedCount * 80) / resourceIds.size(); // 80%用于文档处理
                    updateProgress(knowledgeBaseId, progress, "正在处理文档: " + (processedCount) + "/" + resourceIds.size());
                    
                } catch (Exception e) {
                    log.error("处理资源失败: {}", resourceId, e);
                    // 继续处理其他资源，不中断整个流程
                }
            }
            
            if (!allDocuments.isEmpty()) {
                // Load: 向量化并存储
                updateProgress(knowledgeBaseId, 85, "开始向量化存储...");
                vectorService.storeDocuments(allDocuments, knowledgeBaseId);
                
                // 更新完成状态
                updateKnowledgeBaseCompletion(knowledgeBaseId, allDocuments.size());
                log.info("知识库构建完成: {}, 共处理 {} 个文档块", knowledgeBaseId, allDocuments.size());
            } else {
                updateKnowledgeBaseStatus(knowledgeBaseId, "failed", "没有可处理的文档内容");
            }
            
        } catch (Exception e) {
            log.error("知识库构建失败: {}", knowledgeBaseId, e);
            updateKnowledgeBaseStatus(knowledgeBaseId, "failed", "构建失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识库状态
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
                .map(this::convertToListVO)
                .collect(Collectors.toList());
            
            return Map.of(
                "content", knowledgeBaseList,
                "totalElements", knowledgeBasePage.getTotal(),
                "totalPages", knowledgeBasePage.getPages(),
                "number", knowledgeBasePage.getCurrent() - 1,
                "size", knowledgeBasePage.getSize()
            );
            
        } catch (Exception e) {
            log.error("获取知识库列表失败", e);
            throw new RuntimeException("获取知识库列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除知识库
     */
    public void deleteKnowledgeBase(String knowledgeBaseId, Integer userId) {
        try {
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
            if (knowledgeBase == null) {
                throw new RuntimeException("知识库不存在");
            }
            
            if (!knowledgeBase.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除该知识库");
            }
            
            // 删除向量数据
            vectorService.deleteKnowledgeBaseVectors(knowledgeBaseId);
            
            // 删除数据库记录
            knowledgeBaseMapper.deleteById(knowledgeBaseId);
            
            log.info("成功删除知识库: {}", knowledgeBaseId);
            
        } catch (Exception e) {
            log.error("删除知识库失败: {}", knowledgeBaseId, e);
            throw new RuntimeException("删除知识库失败: " + e.getMessage());
        }
    }

    // ============ 私有方法 ============

    private List<TeachingResource> validateAndGetResources(List<String> resourceIds, Integer userId) {
        List<TeachingResource> resources = new ArrayList<>();
        for (String resourceId : resourceIds) {
            if (!documentService.validateAccess(resourceId, userId)) {
                throw new RuntimeException("无权访问资源: " + resourceId);
            }
            TeachingResource resource = documentService.getResource(resourceId);
            resources.add(resource);
        }
        return resources;
    }

    private void updateProgress(String knowledgeBaseId, int progress, String message) {
        KnowledgeBase update = new KnowledgeBase();
        update.setId(knowledgeBaseId);
        update.setProgress(progress);
        update.setMessage(message);
        update.setUpdatedAt(new Date());
        knowledgeBaseMapper.updateById(update);
    }

    private void updateKnowledgeBaseStatus(String knowledgeBaseId, String status, String message) {
        KnowledgeBase update = new KnowledgeBase();
        update.setId(knowledgeBaseId);
        update.setStatus(status);
        update.setMessage(message);
        update.setUpdatedAt(new Date());
        if ("failed".equals(status) || "completed".equals(status)) {
            update.setCompletedAt(new Date());
        }
        knowledgeBaseMapper.updateById(update);
    }

    private void updateKnowledgeBaseCompletion(String knowledgeBaseId, int chunkCount) {
        KnowledgeBase update = new KnowledgeBase();
        update.setId(knowledgeBaseId);
        update.setStatus("completed");
        update.setProgress(100);
        update.setChunkCount(chunkCount);
        update.setDocumentCount(chunkCount);
        update.setMessage("知识库构建完成");
        update.setCompletedAt(new Date());
        update.setUpdatedAt(new Date());
        knowledgeBaseMapper.updateById(update);
    }

    private KnowledgeBaseListVO convertToListVO(KnowledgeBase knowledgeBase) {
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

    private int calculateEstimatedTime(int resourceCount) {
        // 估算处理时间：每个资源约30秒
        return Math.max(60, resourceCount * 30);
    }

    private String generateKnowledgeBaseId() {
        return "kb_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString().replace("-", "");
    }
}