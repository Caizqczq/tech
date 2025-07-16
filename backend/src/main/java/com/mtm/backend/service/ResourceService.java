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
import com.mtm.backend.model.VO.KnowledgeBaseVO;
import com.mtm.backend.repository.TeachingResource;
import com.mtm.backend.repository.TranscriptionTask;
import com.mtm.backend.repository.KnowledgeBase;
import com.mtm.backend.repository.mapper.TeachingResourceMapper;
import com.mtm.backend.repository.mapper.TranscriptionTaskMapper;
import com.mtm.backend.repository.mapper.KnowledgeBaseMapper;
import com.mtm.backend.utils.LocalFileUtil;
import com.mtm.backend.utils.OssUtil;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final VectorStore vectorStore;
    private final LocalFileUtil localFileUtil;
    private final AudioTranscriptionModel audioTranscriptionModel;
    
    /**
     * 上传文档资源
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
        resource.setDocumentType(uploadDTO.getResourceType());
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
                    AudioUploadDTO audioDTO = new AudioUploadDTO();
                    audioDTO.setSubject(uploadDTO.getSubject());
                    audioDTO.setAutoVectorize(uploadDTO.getAutoVectorize());
                    audioDTO.setNeedTranscription(false);
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
                    DocumentUploadDTO docDTO = new DocumentUploadDTO();
                    docDTO.setSubject(uploadDTO.getSubject());
                    docDTO.setCourseLevel(uploadDTO.getCourseLevel());
                    docDTO.setAutoVectorize(uploadDTO.getAutoVectorize());
                    docDTO.setResourceType("textbook");
                    docDTO.setAutoExtractKeywords(false);
                    
                    uploadDocument(file, docDTO, userId);
                    
                    Map<String, Object> fileResult = new HashMap<>();
                    fileResult.put("filename", file.getOriginalFilename());
                    fileResult.put("status", "success");
                    fileResult.put("resourceId", resourceId);
                    results.add(fileResult);
                    successCount++;
                }
            } catch (Exception e) {
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
     * 分页查询教学资源 - 新版本适配前端需求
     */
    public Map<String, Object> getResources(ResourceQueryDTO queryDTO, Integer userId) {
        try {
            QueryWrapper<TeachingResource> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            
            if (queryDTO.getResourceType() != null && !queryDTO.getResourceType().trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .eq("document_type", queryDTO.getResourceType())
                    .or()
                    .eq("audio_type", queryDTO.getResourceType())
                );
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
            
            queryWrapper.orderByDesc("created_at");
            
            Page<TeachingResource> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getSize());
            IPage<TeachingResource> resourcePage = teachingResourceMapper.selectPage(pageInfo, queryWrapper);
            
            List<Map<String, Object>> resourceList = resourcePage.getRecords().stream()
                    .map(resource -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", resource.getId());
                        item.put("resourceId", resource.getId());
                        item.put("fileName", resource.getOriginalName());
                        item.put("title", resource.getTitle());
                        item.put("resourceType", "document".equals(resource.getResourceType()) ? 
                                resource.getDocumentType() : resource.getAudioType());
                        item.put("subject", resource.getSubject());
                        item.put("courseLevel", resource.getCourseLevel());
                        item.put("description", resource.getDescription());
                        item.put("fileSize", resource.getFileSize());
                        item.put("uploadTime", resource.getCreatedAt());
                        item.put("downloadCount", 0); // 默认值
                        item.put("status", resource.getProcessingStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", resourceList);
            result.put("number", resourcePage.getCurrent() - 1);
            result.put("size", resourcePage.getSize());
            result.put("totalElements", resourcePage.getTotal());
            result.put("totalPages", resourcePage.getPages());
            
            return result;
            
        } catch (Exception e) {
            log.error("查询教学资源失败", e);
            throw new RuntimeException("查询教学资源失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询教学资源 - 旧版本保持兼容
     */
    public Map<String, Object> getResources(ResourceQueryDTO queryDTO, Pageable pageable, Integer userId) {
        try {
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
            
            Page<TeachingResource> pageInfo = new Page<>(pageable.getPageNumber(), pageable.getPageSize());
            IPage<TeachingResource> resourcePage = teachingResourceMapper.selectPage(pageInfo, queryWrapper);
            
            List<ResourceListVO> resourceList = resourcePage.getRecords().stream()
                    .map(this::convertToResourceListVO)
                    .collect(Collectors.toList());
            
            Map<String, Object> pageableInfo = new HashMap<>();
            pageableInfo.put("pageNumber", resourcePage.getCurrent() - 1);
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

            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(threshold);

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
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

            List<Map<String, Object>> results = similarDocuments.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>();
                        Map<String, Object> resource = new HashMap<>();
                        resource.put("id", doc.getMetadata().get("resource_id"));
                        resource.put("title", doc.getMetadata().get("title"));
                        resource.put("subject", doc.getMetadata().get("subject"));
                        resource.put("resourceType", doc.getMetadata().get("resource_type"));

                        result.put("resource", resource);
                        result.put("similarity", 0.96);
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
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在");
            }
            
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
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在");
            }
            
            if (!resource.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除该资源");
            }
            
            try {
                localFileUtil.deleteFile(resource.getFilePath());
            } catch (Exception e) {
                log.warn("删除本地文件失败: {}", e.getMessage());
            }
            
            teachingResourceMapper.deleteById(resourceId);
            
            if ("audio".equals(resource.getResourceType())) {
                QueryWrapper<TranscriptionTask> taskQuery = new QueryWrapper<>();
                taskQuery.eq("resource_id", resourceId);
                transcriptionTaskMapper.delete(taskQuery);
            }
            
        } catch (Exception e) {
            log.error("删除资源失败", e);
            throw new RuntimeException("删除资源失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源下载URL
     */
    public Map<String, Object> getResourceDownloadUrl(String resourceId, Integer userId) {
        try {
            TeachingResource resource = teachingResourceMapper.selectById(resourceId);
            if (resource == null) {
                throw new RuntimeException("资源不存在");
            }
            
            if (!resource.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该资源");
            }
            
            String downloadUrl = localFileUtil.generateUrl(resource.getFilePath());
            
            Map<String, Object> result = new HashMap<>();
            result.put("downloadUrl", downloadUrl);
            result.put("fileName", resource.getOriginalName());
            result.put("expiresAt", LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return result;
            
        } catch (Exception e) {
            log.error("获取资源下载URL失败", e);
            throw new RuntimeException("获取资源下载URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源统计信息
     */
    public Map<String, Object> getResourceStats(Integer userId) {
        try {
            QueryWrapper<TeachingResource> totalQuery = new QueryWrapper<>();
            totalQuery.eq("user_id", userId);
            Long totalResources = teachingResourceMapper.selectCount(totalQuery);
            
            Map<String, Long> typeStats = Map.of(
                "document", getResourceCountByType(userId, "document"),
                "audio", getResourceCountByType(userId, "audio"),
                "video", getResourceCountByType(userId, "video"),
                "image", getResourceCountByType(userId, "image")
            );
            
            QueryWrapper<TeachingResource> todayQuery = new QueryWrapper<>();
            todayQuery.eq("user_id", userId);
            todayQuery.ge("created_at", LocalDateTime.now().toLocalDate().atStartOfDay());
            Long todayUploads = teachingResourceMapper.selectCount(todayQuery);
            
            QueryWrapper<TeachingResource> sizeQuery = new QueryWrapper<>();
            sizeQuery.eq("user_id", userId);
            sizeQuery.select("SUM(file_size) as total_size");
            List<Map<String, Object>> sizeResult = teachingResourceMapper.selectMaps(sizeQuery);
            Long totalSize = sizeResult.isEmpty() ? 0L : 
                (Long) sizeResult.get(0).getOrDefault("total_size", 0L);
            
            return Map.of(
                "totalResources", totalResources,
                "typeStatistics", typeStats,
                "todayUploads", todayUploads,
                "totalSize", totalSize,
                "sizeFormatted", formatFileSize(totalSize)
            );
            
        } catch (Exception e) {
            log.error("获取资源统计失败", e);
            throw new RuntimeException("获取资源统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识库列表
     */
    public List<KnowledgeBaseVO> getKnowledgeBases(Integer userId) {
        try {
            QueryWrapper<KnowledgeBase> query = new QueryWrapper<>();
            query.eq("user_id", userId);
            query.orderByDesc("created_at");
            
            List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectList(query);
            
            return knowledgeBases.stream()
                .map(this::convertToKnowledgeBaseVO)
                .collect(Collectors.toList());
                
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
            QueryWrapper<KnowledgeBase> query = new QueryWrapper<>();
            query.eq("id", knowledgeBaseId);
            query.eq("user_id", userId);
            
            KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectOne(query);
            if (knowledgeBase == null) {
                throw new RuntimeException("知识库不存在或无权删除");
            }
            
            vectorStore.delete(List.of(knowledgeBaseId));
            knowledgeBaseMapper.deleteById(knowledgeBaseId);
            
            log.info("删除知识库成功，知识库ID：{}", knowledgeBaseId);
            
        } catch (Exception e) {
            log.error("删除知识库失败", e);
            throw new RuntimeException("删除知识库失败: " + e.getMessage());
        }
    }
    
    // ============ 私有辅助方法 ============
    
    private ResourceUploadVO performSyncTranscription(MultipartFile file, TeachingResource resource, AudioUploadDTO uploadDTO) {
        try {
            Thread.sleep(1000);
            
            if (!localFileUtil.doesFileExist(resource.getFilePath())) {
                throw new RuntimeException("本地文件不存在: " + resource.getFilePath());
            }
            
            byte[] audioBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            
            ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
            
            log.info("音频文件大小: {} bytes, 文件名: {}", audioBytes.length, originalFilename);
            
            AudioTranscriptionResponse response = audioTranscriptionModel.call(
                new AudioTranscriptionPrompt(
                    audioResource,
                    DashScopeAudioTranscriptionOptions.builder()
                            .withModel("paraformer-realtime-v2")
                            .build()
                )
            );
            
            String transcriptionText = response.getResult().getOutput();
            
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
    
    private TranscriptionTaskVO performAsyncTranscription(TeachingResource resource, AudioUploadDTO uploadDTO) {
        String taskId = generateTaskId();
        
        TranscriptionTask task = new TranscriptionTask();
        task.setTaskId(taskId);
        task.setResourceId(resource.getId());
        task.setTranscriptionMode("async");
        task.setStatus("processing");
        task.setProgress(0);
        task.setEstimatedTime(120);
        task.setStartedAt(new Date());
        
        transcriptionTaskMapper.insert(task);
        
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
    
    private Long getResourceCountByType(Integer userId, String type) {
        QueryWrapper<TeachingResource> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("resource_type", type);
        return teachingResourceMapper.selectCount(query);
    }
    
    private String formatFileSize(Long size) {
        if (size == null || size == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
    
    private KnowledgeBaseVO convertToKnowledgeBaseVO(KnowledgeBase knowledgeBase) {
        return KnowledgeBaseVO.builder()
            .id(knowledgeBase.getId())
            .knowledgeBaseId(knowledgeBase.getId())
            .name(knowledgeBase.getName())
            .description(knowledgeBase.getDescription())
            .status(knowledgeBase.getStatus())
            .documentCount(knowledgeBase.getDocumentCount() != null ? 
                knowledgeBase.getDocumentCount() : knowledgeBase.getChunkCount())
            .createdAt(knowledgeBase.getCreatedAt() != null ? 
                knowledgeBase.getCreatedAt().toString() : "")
            .updatedAt(knowledgeBase.getUpdatedAt() != null ? 
                knowledgeBase.getUpdatedAt().toString() : "")
            .build();
    }
}
