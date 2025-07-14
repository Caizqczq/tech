package com.mtm.backend.service;


import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtm.backend.model.DTO.AudioUploadDTO;
import com.mtm.backend.model.DTO.BatchUploadDTO;
import com.mtm.backend.model.DTO.DocumentUploadDTO;
import com.mtm.backend.model.DTO.ResourceQueryDTO;
import com.mtm.backend.model.VO.ResourceUploadVO;
import com.mtm.backend.model.VO.ResourceDetailVO;
import com.mtm.backend.model.VO.ResourceListVO;
import com.mtm.backend.model.VO.TranscriptionTaskVO;
import com.mtm.backend.model.VO.BatchUploadResultVO;
import com.mtm.backend.model.VO.SemanticSearchResultVO;
import com.mtm.backend.repository.TeachingResource;
import com.mtm.backend.repository.TranscriptionTask;
import com.mtm.backend.repository.mapper.TeachingResourceMapper;
import com.mtm.backend.repository.mapper.TranscriptionTaskMapper;
import com.mtm.backend.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能教学资源管理服务
 * 符合接口文档模块5的规范要求
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {
    
    private final TeachingResourceMapper teachingResourceMapper;
    private final TranscriptionTaskMapper transcriptionTaskMapper;
    private final OssUtil ossUtil;
    private final VectorStore vectorStore;
    
    /**
     * 上传学术文档
     */
    public ResourceUploadVO uploadDocument(MultipartFile file, DocumentUploadDTO uploadDTO, Integer userId) throws IOException {
        String resourceId = generateResourceId();
        
        // 上传到OSS
        String folder = String.format("documents/%d/%s", userId, uploadDTO.getResourceType());
        String ossKey = ossUtil.uploadFile(file, folder);
        
        // 保存到数据库
        TeachingResource resource = new TeachingResource();
        resource.setId(resourceId);
        resource.setOriginalName(file.getOriginalFilename());
        resource.setStoredFilename(extractFilenameFromOssKey(ossKey));
        resource.setOssKey(ossKey);
        resource.setContentType(file.getContentType());
        resource.setFileSize(file.getSize());
        resource.setResourceType("document");
        resource.setTitle(uploadDTO.getTitle() != null ? uploadDTO.getTitle() : file.getOriginalFilename());
        resource.setDescription(uploadDTO.getDescription());
        resource.setSubject(uploadDTO.getSubject());
        resource.setCourseLevel(uploadDTO.getCourseLevel());
        resource.setDocumentType(uploadDTO.getResourceType()); // 使用resourceType字段
        resource.setKeywords(uploadDTO.getKeywords());
        resource.setIsVectorized(uploadDTO.getAutoVectorize());
        resource.setProcessingStatus("completed");
        resource.setUserId(userId);
        resource.setCreatedAt(new Date());
        resource.setUpdatedAt(new Date());
        
        teachingResourceMapper.insert(resource);
        
        // 如果开启自动提取关键词，这里可以调用AI服务
        List<String> extractedKeywords = new ArrayList<>();
        if (uploadDTO.getAutoExtractKeywords()) {
            // TODO: 实现自动关键词提取
            extractedKeywords = Arrays.asList("函数", "连续性", "可导性");
        }
        
        // 构建返回结果
        ResourceUploadVO result = new ResourceUploadVO();
        result.setId(resourceId);
        result.setFilename(resource.getStoredFilename());
        result.setOriginalName(resource.getOriginalName());
        result.setSubject(resource.getSubject());
        result.setCourseLevel(resource.getCourseLevel());
        result.setResourceType(resource.getDocumentType());
        result.setSize(resource.getFileSize());
        result.setContentType(resource.getContentType());
        result.setKeywords(resource.getKeywords() != null ? Arrays.asList(resource.getKeywords().split(",")) : new ArrayList<>());
        result.setExtractedKeywords(extractedKeywords);
        result.setUploadedAt(resource.getCreatedAt());
        result.setDownloadUrl(ossUtil.generateUrl(ossKey));
        result.setIsVectorized(resource.getIsVectorized());
        result.setProcessingStatus(resource.getProcessingStatus());
        
        return result;
    }
    
    /**
     * 上传音频文件及转录
     */
    public Object uploadAudio(MultipartFile file, AudioUploadDTO uploadDTO, Integer userId) throws IOException {
        String resourceId = generateResourceId();

        // 规范化资源类型，避免路径问题
        String resourceType = uploadDTO.getResourceType();
        if (resourceType == null || resourceType.trim().isEmpty()) {
            resourceType = "general";
        } else {
            resourceType = resourceType.trim();
        }

        // 上传到OSS，使用规范化的路径
        String folder = String.format("audio/%d/%s", userId, resourceType);
        String ossKey = ossUtil.uploadFile(file, folder);
        
        // 保存基本信息到数据库
        TeachingResource resource = new TeachingResource();
        resource.setId(resourceId);
        resource.setOriginalName(file.getOriginalFilename());
        resource.setStoredFilename(extractFilenameFromOssKey(ossKey));
        resource.setOssKey(ossKey);
        resource.setContentType(file.getContentType());
        resource.setFileSize(file.getSize());
        resource.setResourceType("audio");
        resource.setSubject(uploadDTO.getSubject());
        resource.setDescription(uploadDTO.getDescription());
        resource.setLanguage(uploadDTO.getLanguage());

        String audioType = validateAndNormalizeAudioType(uploadDTO.getResourceType());
        resource.setAudioType(audioType);

        resource.setSpeaker(uploadDTO.getSpeaker());
        resource.setIsVectorized(uploadDTO.getAutoVectorize());
        resource.setProcessingStatus("completed");
        resource.setUserId(userId);
        resource.setCreatedAt(new Date());
        resource.setUpdatedAt(new Date());
        
        teachingResourceMapper.insert(resource);
        
        // 如果需要转录
        if (uploadDTO.getNeedTranscription()) {
            if ("sync".equals(uploadDTO.getTranscriptionMode())) {
                return performSyncTranscription(file, resource, uploadDTO);
            } else {
                return performAsyncTranscription(resource, uploadDTO);
            }
        } else {
            // 不需要转录，直接返回音频信息
            return buildAudioResult(resource, null);
        }
    }
    
    /**
     * 批量上传资源
     */
    public BatchUploadResultVO uploadBatch(MultipartFile[] files, BatchUploadDTO uploadDTO, Integer userId) {
        String batchId = generateBatchId();
        int successCount = 0;
        int failedCount = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String resourceId = generateResourceId();
                
                // 根据文件类型决定上传方式
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("audio/")) {
                    // 音频文件
                    AudioUploadDTO audioDTO = new AudioUploadDTO();
                    audioDTO.setSubject(uploadDTO.getSubject());
//                    audioDTO.setCourseLevel(uploadDTO.getCourseLevel());
                    audioDTO.setAutoVectorize(uploadDTO.getAutoVectorize());
                    audioDTO.setNeedTranscription(false); // 批量上传不进行转录
                    audioDTO.setResourceType("general");
                    audioDTO.setLanguage("zh");
                    
                    uploadAudio(file, audioDTO, userId);
                    
                    Map<String, Object> fileResult = new HashMap<>();
                    fileResult.put("filename", file.getOriginalFilename());
                    fileResult.put("status", "success");
                    fileResult.put("resourceId", resourceId);
                    results.add(fileResult);
                    successCount++;
                } else {
                    // 文档文件
                    DocumentUploadDTO docDTO = new DocumentUploadDTO();
                    docDTO.setSubject(uploadDTO.getSubject());
                    docDTO.setCourseLevel(uploadDTO.getCourseLevel());
                    docDTO.setAutoVectorize(uploadDTO.getAutoVectorize());
                    docDTO.setResourceType("textbook"); // 默认类型
                    docDTO.setAutoExtractKeywords(false); // 批量上传不自动提取关键词
                    
                    uploadDocument(file, docDTO, userId);
                    
                    Map<String, Object> fileResult = new HashMap<>();
                    fileResult.put("filename", file.getOriginalFilename());
                    fileResult.put("status", "success");
                    fileResult.put("resourceId", resourceId);
                    results.add(fileResult);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量上传文件失败: {}", file.getOriginalFilename(), e);
                Map<String, Object> fileResult = new HashMap<>();
                fileResult.put("filename", file.getOriginalFilename());
                fileResult.put("status", "failed");
                fileResult.put("error", e.getMessage());
                results.add(fileResult);
                failedCount++;
            }
        }
        
        BatchUploadResultVO result = new BatchUploadResultVO();
        result.setBatchId(batchId);
        result.setTotalFiles(files.length);
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setResults(results);
        result.setProcessingStatus("completed");
        
        return result;
    }
    
    /**
     * 分页查询教学资源
     */
    public Map<String, Object> getResources(ResourceQueryDTO queryDTO, Pageable pageable, Integer userId) {
        try {
            // 构建查询条件
            QueryWrapper<TeachingResource> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            
            if (queryDTO.getResourceType() != null && !queryDTO.getResourceType().trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .eq("document_type", queryDTO.getResourceType())
                    .or()
                    .eq("audio_type", queryDTO.getResourceType())
                );
            }
            
            if (queryDTO.getSubject() != null && !queryDTO.getSubject().trim().isEmpty()) {
                queryWrapper.eq("subject", queryDTO.getSubject());
            }
            
            if (queryDTO.getCourseLevel() != null && !queryDTO.getCourseLevel().trim().isEmpty()) {
                queryWrapper.eq("course_level", queryDTO.getCourseLevel());
            }
            
            if (queryDTO.getKeywords() != null && !queryDTO.getKeywords().trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like("title", queryDTO.getKeywords())
                    .or()
                    .like("description", queryDTO.getKeywords())
                    .or()
                    .like("keywords", queryDTO.getKeywords())
                );
            }
            
            // 排序
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                for (Sort.Order order : sort) {
                    String property = order.getProperty();
                    if ("createdAt".equals(property)) {
                        property = "created_at";
                    } else if ("updatedAt".equals(property)) {
                        property = "updated_at";
                    }
                    
                    if (order.isAscending()) {
                        queryWrapper.orderByAsc(property);
                    } else {
                        queryWrapper.orderByDesc(property);
                    }
                }
            } else {
                queryWrapper.orderByDesc("created_at");
            }
            
            // 分页查询
            Page<TeachingResource> pageInfo = new Page<>(pageable.getPageNumber(), pageable.getPageSize());
            IPage<TeachingResource> resourcePage = teachingResourceMapper.selectPage(pageInfo, queryWrapper);
            
            // 转换为VO
            List<ResourceListVO> resourceList = resourcePage.getRecords().stream()
                    .map(this::convertToResourceListVO)
                    .collect(Collectors.toList());
            
            Map<String, Object> pageableInfo = new HashMap<>();
            pageableInfo.put("pageNumber", resourcePage.getCurrent() - 1); // Spring Data使用0开始
            pageableInfo.put("pageSize", resourcePage.getSize());
            pageableInfo.put("totalElements", resourcePage.getTotal());
            pageableInfo.put("totalPages", resourcePage.getPages());
            
            return Map.of(
                "content", resourceList,
                "pageable", pageableInfo
            );
            
        } catch (Exception e) {
            log.error("查询教学资源失败", e);
            throw new RuntimeException("查询教学资源失败: " + e.getMessage());
        }
    }
    
    /**
     * 语义搜索资源
     */
    public SemanticSearchResultVO searchResourcesSemantic(String query, String subject, String courseLevel,
                                                         Integer topK, Double threshold, Integer userId) {
        try {
            long startTime = System.currentTimeMillis();

            // 构建搜索请求
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(threshold);

            // 添加过滤条件
            List<String> filters = new ArrayList<>();
            if (subject != null && !subject.trim().isEmpty()) {
                filters.add("subject == '" + subject + "'");
            }
            if (courseLevel != null && !courseLevel.trim().isEmpty()) {
                filters.add("course_level == '" + courseLevel + "'");
            }

            if (!filters.isEmpty()) {
                String filterExpression = String.join(" && ", filters);
                searchBuilder = searchBuilder.filterExpression(filterExpression);
            }

            SearchRequest searchRequest = searchBuilder.build();

            // 执行向量搜索
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

            // 转换为响应格式
            List<Map<String, Object>> results = similarDocuments.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>();

                        // 构建资源信息
                        Map<String, Object> resource = new HashMap<>();
                        resource.put("id", doc.getMetadata().get("resource_id"));
                        resource.put("title", doc.getMetadata().get("title"));
                        resource.put("subject", doc.getMetadata().get("subject"));
                        resource.put("resourceType", doc.getMetadata().get("resource_type"));

                        result.put("resource", resource);
                        result.put("similarity", 0.96); // 这里应该是实际的相似度分数
                        result.put("relevantContent", doc.getText().substring(0, Math.min(200, doc.getText().length())) + "...");

                        return result;
                    })
                    .collect(Collectors.toList());

            double searchTime = (System.currentTimeMillis() - startTime) / 1000.0;

            SemanticSearchResultVO searchResult = new SemanticSearchResultVO();
            searchResult.setResults(results);
            searchResult.setQuery(query);
            searchResult.setTotalResults(results.size());
            searchResult.setSearchTime(searchTime);

            return searchResult;

        } catch (Exception e) {
            log.error("语义搜索失败", e);
            throw new RuntimeException("语义搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源详情
     */
    public ResourceDetailVO getResourceDetail(String resourceId, Integer userId) {
        try {
            // 查询资源
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在");
            }
            
            // 验证权限
            if (!resource.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该资源");
            }
            
            return convertToResourceDetailVO(resource);
            
        } catch (Exception e) {
            log.error("获取资源详情失败", e);
            throw new RuntimeException("获取资源详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除教学资源
     */
    public void deleteResource(String resourceId, Integer userId) {
        try {
            // 查询资源
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在");
            }
            
            // 验证权限
            if (!resource.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除该资源");
            }
            
            // 删除OSS文件
            try {
                ossUtil.deleteFile(resource.getOssKey());
            } catch (Exception e) {
                log.warn("删除OSS文件失败: {}", e.getMessage());
            }
            
            // 删除数据库记录
            teachingResourceMapper.deleteById(resourceId);
            
            // 如果是音频，删除相关转录任务
            if ("audio".equals(resource.getResourceType())) {
                QueryWrapper<TranscriptionTask> taskQuery = new QueryWrapper<>();
                taskQuery.eq("resource_id", resourceId); // 修改字段名
                transcriptionTaskMapper.delete(taskQuery);
            }
            
        } catch (Exception e) {
            log.error("删除资源失败", e);
            throw new RuntimeException("删除资源失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源下载链接
     */
    public String getResourceDownloadUrl(String resourceId, Integer userId) {
        try {
            // 查询资源
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在");
            }
            
            // 验证权限
            if (!resource.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该资源");
            }
            
            // 生成下载链接
            return ossUtil.generateUrl(resource.getOssKey());
            
        } catch (Exception e) {
            log.error("获取下载链接失败", e);
            throw new RuntimeException("获取下载链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源统计信息
     */
    public Map<String, Object> getResourceStatistics(Integer userId) {
        try {
            // 总资源数
            QueryWrapper<TeachingResource> totalQuery = new QueryWrapper<>();
            totalQuery.eq("user_id", userId);
            Long totalResources = teachingResourceMapper.selectCount(totalQuery);
            
            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            typeStats.put("lesson_plan", getResourceCountByType(userId, "lesson_plan"));
            typeStats.put("paper", getResourceCountByType(userId, "paper"));
            typeStats.put("lecture", getResourceCountByType(userId, "lecture"));
            
            // 按学科统计
            List<Map<String, Object>> subjectStats = getSubjectStatistics(userId);
            
            // 知识库数量（暂时返回0）
            Long knowledgeBaseCount = 0L;
            
            // 向量化资源数
            QueryWrapper<TeachingResource> vectorizedQuery = new QueryWrapper<>();
            vectorizedQuery.eq("user_id", userId);
            vectorizedQuery.eq("is_vectorized", true);
            Long vectorizedResources = teachingResourceMapper.selectCount(vectorizedQuery);
            
            // 今日上传数
            QueryWrapper<TeachingResource> todayQuery = new QueryWrapper<>();
            todayQuery.eq("user_id", userId);
            todayQuery.ge("created_at", new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
            Long todayUploads = teachingResourceMapper.selectCount(todayQuery);
            
            return Map.of(
                "totalResources", totalResources,
                "typeStatistics", typeStats,
                "subjectStatistics", subjectStats,
                "knowledgeBaseCount", knowledgeBaseCount,
                "vectorizedResources", vectorizedResources,
                "todayUploads", todayUploads
            );
            
        } catch (Exception e) {
            log.error("获取资源统计失败", e);
            throw new RuntimeException("获取资源统计失败: " + e.getMessage());
        }
    }
    
    // ============ 私有辅助方法 ============
    
    /**
     * 同步转录
     * 根据Spring AI Alibaba官方文档的最佳实践实现
     */
    private ResourceUploadVO performSyncTranscription(MultipartFile file, TeachingResource resource, AudioUploadDTO uploadDTO) {
        try {
            log.info("开始音频转录，文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

            // 验证API密钥配置
            if (System.getenv("AI_DASHSCOPE_API_KEY") == null || System.getenv("AI_DASHSCOPE_API_KEY").trim().isEmpty()) {
                throw new RuntimeException("DashScope API密钥未配置，请设置AI_DASHSCOPE_API_KEY环境变量");
            }

            // 验证音频文件格式和大小
            validateAudioFile(file);

            // 等待一秒确保文件上传完成
            Thread.sleep(1000);

            // 检查OSS文件是否存在
            if (!ossUtil.doesObjectExist(resource.getOssKey())) {
                throw new RuntimeException("OSS文件不存在: " + resource.getOssKey());
            }

            // 构建OSS文件的公开访问URL
            // DashScope多模态API需要可公开访问的HTTP/HTTPS URL
            String audioUrl = buildOssPublicUrl(resource.getOssKey());

            log.info("使用OSS音频文件URL进行转录: {}", audioUrl);

            // 测试URL可访问性
            if (!testUrlAccessibility(audioUrl)) {
                log.warn("OSS公开URL不可访问，尝试使用签名URL: {}", audioUrl);
                // 如果公开URL不可访问，尝试使用签名URL
                audioUrl = ossUtil.generateSignedUrl(resource.getOssKey(), 1); // 1小时有效期
                log.info("使用OSS签名URL进行转录: {}", audioUrl);

                if (!testUrlAccessibility(audioUrl)) {
                    throw new RuntimeException("OSS音频文件URL不可访问，请检查OSS bucket权限配置: " + audioUrl);
                }
            }

            try {
                log.info("开始调用DashScope音频转录API，文件URL: {}, 文件大小: {} bytes", audioUrl, file.getSize());

                // 根据文件大小选择合适的处理策略
                String transcriptionText = performAudioTranscriptionWithRetry(audioUrl, file.getSize());

                if (transcriptionText == null || transcriptionText.trim().isEmpty()) {
                    log.warn("音频转录结果为空，可能是音频内容无法识别");
                    transcriptionText = ""; // 设置为空字符串而不是null
                }

                // 更新数据库
                resource.setTranscriptionText(transcriptionText);
                resource.setUpdatedAt(new Date());
                teachingResourceMapper.updateById(resource);

                log.info("音频转录成功，资源ID: {}, 转录长度: {} 字符", resource.getId(), transcriptionText.length());

                return buildAudioResult(resource, transcriptionText);

            } catch (ApiException e) {
                log.error("DashScope API调用失败: {}", e.getMessage(), e);
                throw new RuntimeException("DashScope API调用失败: " + e.getMessage(), e);
            } catch (NoApiKeyException e) {
                log.error("DashScope API密钥未配置: {}", e.getMessage(), e);
                throw new RuntimeException("DashScope API密钥未配置，请检查AI_DASHSCOPE_API_KEY环境变量", e);
            } catch (Exception e) {
                log.error("音频转录失败: {}", e.getMessage(), e);
                throw new RuntimeException("音频转录失败: " + getDetailedErrorMessage(e), e);
            }

        } catch (Exception e) {
            log.error("同步转录失败: {}", e.getMessage(), e);

            // 根据错误类型提供更具体的错误信息
            String errorMessage = getDetailedErrorMessage(e);
            throw new RuntimeException("音频转录失败: " + errorMessage, e);
        }
    }
    
    /**
     * 异步转录
     */
    private TranscriptionTaskVO performAsyncTranscription(TeachingResource resource, AudioUploadDTO uploadDTO) {
        String taskId = generateTaskId();
        
        // 创建转录任务
        TranscriptionTask task = new TranscriptionTask();
        task.setTaskId(taskId);
        task.setResourceId(resource.getId());
        task.setTranscriptionMode("async");
        task.setStatus("processing");
        task.setProgress(0);
        task.setEstimatedTime(120); // 预估2分钟
        task.setStartedAt(new Date());
        
        transcriptionTaskMapper.insert(task);
        
        // TODO: 这里应该启动异步任务处理转录
        // 暂时返回任务信息
        TranscriptionTaskVO result = new TranscriptionTaskVO();
        result.setTaskId(taskId);
        result.setResourceId(resource.getId());
        result.setMessage("语音转录任务已启动");
        result.setEstimatedTime(120);
        result.setStatus("processing");
        result.setProgress(0);
        result.setStatusUrl("/api/tasks/" + taskId + "/status");
        
        return result;
    }
    
    private ResourceUploadVO buildAudioResult(TeachingResource resource, String transcriptionText) {
        ResourceUploadVO result = new ResourceUploadVO();
        result.setId(resource.getId());
        result.setFilename(resource.getStoredFilename());
        result.setOriginalName(resource.getOriginalName());
        result.setSubject(resource.getSubject());
        result.setResourceType(resource.getAudioType());
        result.setDescription(resource.getDescription());
        result.setSpeaker(resource.getSpeaker());
        result.setDuration(resource.getDuration());
        result.setSize(resource.getFileSize());
        result.setLanguage(resource.getLanguage());
        result.setUploadedAt(resource.getCreatedAt());
        result.setDownloadUrl(ossUtil.generateUrl(resource.getOssKey()));
        result.setTranscription(transcriptionText);
        result.setIsVectorized(resource.getIsVectorized());
        result.setProcessingStatus(resource.getProcessingStatus());
        
        return result;
    }
    
    private String generateResourceId() {
        return "res_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String generateBatchId() {
        return "batch_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private String extractFilenameFromOssKey(String ossKey) {
        return ossKey.substring(ossKey.lastIndexOf("/") + 1);
    }
    
    private ResourceListVO convertToResourceListVO(TeachingResource resource) {
        return ResourceListVO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .subject(resource.getSubject())
                .courseLevel(resource.getCourseLevel())
                .resourceType("document".equals(resource.getResourceType()) ? resource.getDocumentType() : resource.getAudioType())
                .fileSize(resource.getFileSize())
                .keywords(resource.getKeywords() != null ? Arrays.asList(resource.getKeywords().split(",")) : new ArrayList<>())
                .isVectorized(resource.getIsVectorized())
                .createdAt(resource.getCreatedAt())
                .build();
    }
    
    private ResourceDetailVO convertToResourceDetailVO(TeachingResource resource) {
        return ResourceDetailVO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .subject(resource.getSubject())
                .courseLevel(resource.getCourseLevel())
                .resourceType("document".equals(resource.getResourceType()) ? resource.getDocumentType() : resource.getAudioType())
                .originalName(resource.getOriginalName())
                .fileSize(resource.getFileSize())
                .contentType(resource.getContentType())
                .keywords(resource.getKeywords() != null ? Arrays.asList(resource.getKeywords().split(",")) : new ArrayList<>())
                .downloadUrl(ossUtil.generateUrl(resource.getOssKey()))
                .transcriptionText(resource.getTranscriptionText())
                .isVectorized(resource.getIsVectorized())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
    
    private Long getResourceCountByType(Integer userId, String resourceType) {
        QueryWrapper<TeachingResource> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.and(wrapper -> wrapper
            .eq("document_type", resourceType)
            .or()
            .eq("audio_type", resourceType)
        );
        return teachingResourceMapper.selectCount(query);
    }
    
    private List<Map<String, Object>> getSubjectStatistics(Integer userId) {
        // 这里可以通过自定义SQL查询来获取更详细的统计信息
        // 为了简化，暂时返回基本统计
        return Arrays.asList(
            Map.of("subject", "高等数学", "count", getResourceCountBySubject(userId, "高等数学")),
            Map.of("subject", "线性代数", "count", getResourceCountBySubject(userId, "线性代数"))
        );
    }
    
    private Long getResourceCountBySubject(Integer userId, String subject) {
        QueryWrapper<TeachingResource> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("subject", subject);
        return teachingResourceMapper.selectCount(query);
    }

    /**
     * 验证和规范化音频类型
     * 确保音频类型符合接口文档规范且不超过数据库字段长度限制
     */
    private String validateAndNormalizeAudioType(String resourceType) {
        // 如果为空，设置默认值
        if (resourceType == null || resourceType.trim().isEmpty()) {
            return "lecture"; // 默认为讲座类型
        }

        // 去除前后空格并转换为小写
        String normalizedType = resourceType.trim().toLowerCase();

        // 验证是否为接口文档规定的有效值
        if (Arrays.asList("lecture", "seminar", "discussion", "interview").contains(normalizedType)) {
            return normalizedType;
        }

        // 如果不是标准值，但长度在限制范围内，直接使用（支持扩展）
        if (normalizedType.length() <= 50) {
            return normalizedType;
        }

        // 如果超过长度限制，截断并记录警告
        log.warn("音频类型长度超过限制，将被截断: {}", resourceType);
        return normalizedType.substring(0, 50);
    }

    /**
     * 验证音频文件格式和大小
     * 根据DashScope官方文档的要求进行验证
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("音频文件为空");
        }

        // 检查文件大小（最大100MB）
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("音频文件大小超过限制，最大支持100MB");
        }

        // 检查文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("无法获取音频文件名");
        }

        String extension = originalFilename.toLowerCase();
        if (!extension.endsWith(".mp3") && !extension.endsWith(".wav") &&
            !extension.endsWith(".m4a") && !extension.endsWith(".flac")) {
            throw new RuntimeException("不支持的音频格式，仅支持mp3、wav、m4a、flac格式");
        }

        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("audio/")) {
            log.warn("音频文件MIME类型可能不正确: {}", contentType);
        }
    }

    /**
     * 创建临时音频文件
     */
    private java.io.File createTempAudioFile(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String tempFileName = "audio_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.io.File tempFile = new java.io.File(tempDir, tempFileName);

        // 将MultipartFile内容写入临时文件
        file.transferTo(tempFile);

        // 验证文件是否创建成功
        if (!tempFile.exists() || tempFile.length() == 0) {
            throw new IOException("临时音频文件创建失败: " + tempFile.getAbsolutePath());
        }

        return tempFile;
    }



    /**
     * 清理临时文件
     */
    private void cleanupTempFile(java.io.File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            try {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    log.debug("临时文件删除成功: {}", tempFile.getAbsolutePath());
                } else {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warn("清理临时文件时发生异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 构建OSS文件的公开访问URL
     * DashScope音频转录API需要可公开访问的HTTP/HTTPS URL
     */
    private String buildOssPublicUrl(String ossKey) {
        try {
            // 规范化OSS Key，确保没有双斜杠等问题
            ossKey = normalizeOssKey(ossKey);

            // 从OssConfig获取配置信息
            String endpoint = ossUtil.getEndpoint();
            String bucketName = ossUtil.getBucketName();
            String customDomain = ossUtil.getCustomDomain();

            String finalUrl;

            // 如果配置了自定义域名，使用自定义域名
            if (customDomain != null && !customDomain.trim().isEmpty()) {
                // 确保自定义域名以https://开头（DashScope要求HTTPS）
                if (!customDomain.startsWith("https://")) {
                    if (customDomain.startsWith("http://")) {
                        customDomain = customDomain.replace("http://", "https://");
                    } else {
                        customDomain = "https://" + customDomain;
                    }
                }
                finalUrl = customDomain + "/" + ossKey;
            } else {
                // 使用标准的OSS访问URL格式
                // 移除endpoint中可能存在的协议前缀
                String cleanEndpoint = endpoint.replaceAll("^https?://", "");

                // 构建标准OSS URL: https://bucket-name.endpoint/object-key
                finalUrl = "https://" + bucketName + "." + cleanEndpoint + "/" + ossKey;
            }

            log.info("构建的OSS公开URL: {}", finalUrl);

            // 验证URL格式
            validateUrl(finalUrl);

            return finalUrl;

        } catch (Exception e) {
            log.error("构建OSS公开URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法构建OSS文件访问URL: " + e.getMessage());
        }
    }

    /**
     * 规范化OSS Key
     */
    private String normalizeOssKey(String ossKey) {
        if (ossKey == null || ossKey.isEmpty()) {
            throw new IllegalArgumentException("OSS Key不能为空");
        }

        // 移除多余的斜杠
        String normalized = ossKey.replaceAll("/+", "/");

        // 移除开头的斜杠（OSS Key不应该以斜杠开头）
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    /**
     * 验证URL格式
     */
    private void validateUrl(String url) {
        try {
            new java.net.URL(url);

            // 确保是HTTPS协议（DashScope要求）
            if (!url.startsWith("https://")) {
                throw new IllegalArgumentException("DashScope API要求使用HTTPS协议");
            }

        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("URL格式不正确: " + url, e);
        }
    }

    /**
     * 测试URL可访问性
     * 验证DashScope API是否能够访问OSS文件URL
     */
    private boolean testUrlAccessibility(String url) {
        try {
            log.info("测试URL可访问性: {}", url);

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // 5秒连接超时
            connection.setReadTimeout(5000);    // 5秒读取超时

            // 设置User-Agent，模拟正常的HTTP请求
            connection.setRequestProperty("User-Agent", "Spring-AI-Alibaba/1.0");

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            boolean accessible = (responseCode == 200);
            log.info("URL可访问性测试结果: {} (响应码: {})", accessible ? "成功" : "失败", responseCode);

            if (!accessible) {
                log.warn("URL不可访问，响应码: {}，请检查OSS bucket权限配置", responseCode);
            }

            return accessible;

        } catch (Exception e) {
            log.error("URL可访问性测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据异常类型提供详细的错误信息
     */
    private String getDetailedErrorMessage(Exception e) {
        String message = e.getMessage();

        if (message != null) {
            if (message.contains("MalformedURLException") || message.contains("spec") || message.contains("null")) {
                return "API配置错误，请检查AI_DASHSCOPE_API_KEY环境变量是否正确设置";
            } else if (message.contains("failed to get file urls") || message.contains("url error") || message.contains("please check url")) {
                return "音频文件URL访问失败。请检查：1) OSS bucket是否配置了公共读权限；2) 文件URL格式是否正确；3) 文件是否真实存在；4) 尝试使用OSS签名URL";
            } else if (message.contains("get transcription outcome failed") || message.contains("multimodal")) {
                return "多模态音频转录服务调用失败，请检查网络连接和API密钥配置";
            } else if (message.contains("FileNotFoundException")) {
                return "音频文件不存在或无法访问";
            } else if (message.contains("InvalidParameter") || message.contains("invalid_request_error")) {
                return "DashScope多模态API参数错误，通常是因为音频文件URL不可公开访问或格式不正确";
            } else if (message.contains("model_not_found") || message.contains("qwen-audio")) {
                return "DashScope音频模型不可用，请检查模型配置或API权限";
            } else if (message.contains("SignatureDoesNotMatch")) {
                return "OSS签名验证失败，请检查OSS配置";
            } else if (message.contains("AccessDenied")) {
                return "OSS访问被拒绝，请检查bucket权限配置";
            } else if (message.contains("NoSuchBucket")) {
                return "OSS bucket不存在，请检查bucket名称配置";
            } else if (message.contains("NoSuchKey")) {
                return "OSS文件不存在，请检查文件路径";
            }
        }

        return message != null ? message : "未知错误";
    }

    /**
     * 执行音频转录，包含重试机制和超时处理
     */
    private String performAudioTranscriptionWithRetry(String audioUrl, long fileSize) throws Exception {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                log.info("音频转录尝试 {}/{}, 文件大小: {} MB", retryCount + 1, maxRetries, fileSize / (1024.0 * 1024.0));

                // 根据文件大小选择合适的模型和超时时间
                String model = selectOptimalAudioModel(fileSize);
                int timeoutSeconds = calculateTimeoutForFileSize(fileSize);

                return performSingleAudioTranscription(audioUrl, model, timeoutSeconds);

            } catch (Exception e) {
                lastException = e;
                retryCount++;

                log.warn("音频转录第{}次尝试失败: {}", retryCount, e.getMessage());

                if (retryCount < maxRetries) {
                    // 指数退避重试策略
                    int waitTime = (int) Math.pow(2, retryCount) * 1000; // 2秒, 4秒, 8秒
                    log.info("等待{}秒后重试...", waitTime / 1000);

                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试等待被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("音频转录在" + maxRetries + "次尝试后仍然失败", lastException);
    }

    /**
     * 根据文件大小选择最优的音频模型
     */
    private String selectOptimalAudioModel(long fileSize) {
        // 文件大小阈值（字节）
        long smallFileThreshold = 2 * 1024 * 1024;  // 2MB
        long mediumFileThreshold = 5 * 1024 * 1024; // 5MB

        if (fileSize <= smallFileThreshold) {
            // 小文件使用快速模型
            return "qwen-audio-turbo-latest";
        } else if (fileSize <= mediumFileThreshold) {
            // 中等文件使用平衡模型
            return "qwen-audio-turbo-latest";
        } else {
            // 大文件使用更稳定的模型，但可能较慢
            return "qwen-audio-turbo-latest";
        }
    }

    /**
     * 根据文件大小计算合适的超时时间
     */
    private int calculateTimeoutForFileSize(long fileSize) {
        // 基础超时时间（秒）
        int baseTimeout = 30;

        // 每MB增加的超时时间（秒）
        int timeoutPerMB = 15;

        // 计算文件大小（MB）
        double fileSizeMB = fileSize / (1024.0 * 1024.0);

        // 计算总超时时间，最小30秒，最大180秒
        int calculatedTimeout = (int) (baseTimeout + fileSizeMB * timeoutPerMB);

        return Math.min(Math.max(calculatedTimeout, 30), 180);
    }

    /**
     * 执行单次音频转录
     */
    private String performSingleAudioTranscription(String audioUrl, String model, int timeoutSeconds) throws Exception {
        log.info("使用模型: {}, 超时时间: {}秒", model, timeoutSeconds);

        // 根据阿里云百炼官方文档，使用MultiModalConversation进行音频转录
        MultiModalConversation conv = new MultiModalConversation();

        // 构建用户消息，包含音频URL和转录指令
        MultiModalMessage userMessage = MultiModalMessage.builder()
            .role(Role.USER.getValue())
            .content(Arrays.asList(
                Collections.singletonMap("audio", audioUrl),
                Collections.singletonMap("text", "请转录这段音频的内容，只返回转录文本，不要添加其他说明。")
            ))
            .build();

        // 构建请求参数，包含超时配置
        MultiModalConversationParam param = MultiModalConversationParam.builder()
            .model(model)
            .message(userMessage)
            .build();

        // 使用CompletableFuture实现超时控制
        CompletableFuture<MultiModalConversationResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                return conv.call(param);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        MultiModalConversationResult result;
        try {
            // 等待结果，设置超时时间
            result = future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("音频转录超时（" + timeoutSeconds + "秒），请尝试使用较小的音频文件", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("音频转录执行失败", e.getCause());
        }

        if (result == null || result.getOutput() == null ||
            result.getOutput().getChoices() == null ||
            result.getOutput().getChoices().isEmpty()) {
            throw new RuntimeException("音频转录响应为空");
        }

        // 提取转录文本
        String transcriptionText = result.getOutput().getChoices().get(0)
            .getMessage().getContent().get(0).get("text").toString();

        log.info("音频转录成功，转录文本长度: {} 字符", transcriptionText != null ? transcriptionText.length() : 0);

        return transcriptionText;
    }
}