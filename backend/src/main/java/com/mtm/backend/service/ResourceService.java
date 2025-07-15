package com.mtm.backend.service;

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
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;

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
    private final LocalFileUtil localFileUtil;
    private final AudioTranscriptionModel audioTranscriptionModel;
    private final VectorStore vectorStore;
    
    /**
     * 上传学术文档
     */
    public ResourceUploadVO uploadDocument(MultipartFile file, DocumentUploadDTO uploadDTO, Integer userId) throws IOException {
        String resourceId = generateResourceId();
        
        // 上传到本地存储
        String folder = String.format("documents/%d/%s", userId, uploadDTO.getResourceType());
        String filePath = localFileUtil.uploadFile(file, folder);
        
        // 保存到数据库
        TeachingResource resource = new TeachingResource();
        resource.setId(resourceId);
        resource.setOriginalName(file.getOriginalFilename());
        resource.setStoredFilename(extractFilenameFromFilePath(filePath));
        resource.setFilePath(filePath);
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
        result.setDownloadUrl(localFileUtil.generateUrl(filePath));
        result.setIsVectorized(resource.getIsVectorized());
        result.setProcessingStatus(resource.getProcessingStatus());
        
        return result;
    }
    
    /**
     * 上传音频文件及转录
     */
    public Object uploadAudio(MultipartFile file, AudioUploadDTO uploadDTO, Integer userId) throws IOException {
        String resourceId = generateResourceId();
        
        // 上传到本地存储
        String folder = String.format("audio/%d/%s", userId, uploadDTO.getResourceType() != null ? uploadDTO.getResourceType() : "general");
        String filePath = localFileUtil.uploadFile(file, folder);
        
        // 保存基本信息到数据库
        TeachingResource resource = new TeachingResource();
        resource.setId(resourceId);
        resource.setOriginalName(file.getOriginalFilename());
        resource.setStoredFilename(extractFilenameFromFilePath(filePath));
        resource.setFilePath(filePath);
        resource.setContentType(file.getContentType());
        resource.setFileSize(file.getSize());
        resource.setResourceType("audio");
        resource.setSubject(uploadDTO.getSubject());
        resource.setDescription(uploadDTO.getDescription());
        resource.setLanguage(uploadDTO.getLanguage());
        resource.setAudioType(uploadDTO.getResourceType());
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
            
            // 删除本地文件
            try {
                localFileUtil.deleteFile(resource.getFilePath());
            } catch (Exception e) {
                log.warn("删除本地文件失败: {}", e.getMessage());
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
            return localFileUtil.generateUrl(resource.getFilePath());
            
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
     */
    private ResourceUploadVO performSyncTranscription(MultipartFile file, TeachingResource resource, AudioUploadDTO uploadDTO) {
        try {
            // 等待一秒确保文件上传完成
            Thread.sleep(1000);
            
            // 检查本地文件是否存在
            if (!localFileUtil.doesFileExist(resource.getFilePath())) {
                throw new RuntimeException("本地文件不存在: " + resource.getFilePath());
            }
            
            // 直接使用文件字节数组，避免文件系统问题
            byte[] audioBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            
            // 创建ByteArrayResource，并设置文件名
            ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
            
            log.info("音频文件大小: {} bytes, 文件名: {}", audioBytes.length, originalFilename);
            
            // 执行转录
            AudioTranscriptionResponse response = audioTranscriptionModel.call(
                new AudioTranscriptionPrompt(
                    audioResource,
                    DashScopeAudioTranscriptionOptions.builder()
                            .withModel("paraformer-realtime-v2")
                            .build()
                )
            );
            
            String transcriptionText = response.getResult().getOutput();
            
            // 更新数据库
            resource.setTranscriptionText(transcriptionText);
            resource.setUpdatedAt(new Date());
            teachingResourceMapper.updateById(resource);
            
            log.info("音频转录成功，资源ID: {}, 转录长度: {} 字符", resource.getId(), transcriptionText.length());
            
            return buildAudioResult(resource, transcriptionText);
            
        } catch (Exception e) {
            log.error("同步转录失败", e);
            throw new RuntimeException("音频转录失败: " + e.getMessage());
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
        result.setDownloadUrl(localFileUtil.generateUrl(resource.getFilePath()));
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
    
    // 删除不再使用的方法，已被extractFilenameFromFilePath替代
    // private String extractFilenameFromOssKey(String ossKey) {
    //     return ossKey.substring(ossKey.lastIndexOf("/") + 1);
    // }
    
    private String extractFilenameFromFilePath(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
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
                .downloadUrl(localFileUtil.generateUrl(resource.getFilePath()))
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
}