package com.mtm.backend.service.impl;

import com.mtm.backend.model.VO.KnowledgeItemVO;
import com.mtm.backend.model.VO.PaginationVO;
import com.mtm.backend.model.VO.UploadResultVO;
import com.mtm.backend.service.KnowledgeService;
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeServiceImpl implements KnowledgeService {
    
    private final VectorStore vectorStore;
    private final LocalFileUtil localFileUtil;
    
    @Override
    public PaginationVO<KnowledgeItemVO> getKnowledgeItems(int page, int size, String subject, 
                                                          String category, String search, Integer userId) {
        try {
            log.info("获取知识库项目列表，用户ID：{}，页码：{}，大小：{}", userId, page, size);
            
            // 构建搜索请求
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .topK(size * 2) // 获取更多结果用于分页
                    .similarityThreshold(0.1);
            
            // 添加搜索条件
            if (search != null && !search.trim().isEmpty()) {
                searchBuilder.query(search);
            } else {
                searchBuilder.query("*"); // 获取所有文档
            }
            
            // 添加过滤条件
            List<String> filters = new ArrayList<>();
            filters.add("user_id == " + userId);
            
            if (subject != null && !subject.trim().isEmpty()) {
                filters.add("subject == '" + subject + "'");
            }
            
            if (category != null && !category.trim().isEmpty()) {
                filters.add("category == '" + category + "'");
            }
            
            if (!filters.isEmpty()) {
                searchBuilder.filterExpression(String.join(" && ", filters));
            }
            
            // 执行搜索
            List<Document> documents = vectorStore.similaritySearch(searchBuilder.build());
            
            // 转换为VO对象
            List<KnowledgeItemVO> items = documents.stream()
                    .map(this::convertToKnowledgeItemVO)
                    .collect(Collectors.toList());
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, items.size());
            List<KnowledgeItemVO> pageItems = items.subList(start, end);
            
            return PaginationVO.<KnowledgeItemVO>builder()
                    .items(pageItems)
                    .total(items.size())
                    .page(page)
                    .size(size)
                    .totalPages((int) Math.ceil((double) items.size() / size))
                    .build();
                    
        } catch (Exception e) {
            log.error("获取知识库项目失败", e);
            throw new RuntimeException("获取知识库项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public UploadResultVO uploadDocument(MultipartFile file, String subject, String category, 
                                        String title, String content, String tags, Integer userId) {
        try {
            log.info("上传文档到知识库，用户ID：{}，文件名：{}", userId, file.getOriginalFilename());
            
            // 生成文件ID
            String fileId = UUID.randomUUID().toString().replace("-", "");
            
            // 上传文件到本地存储
            String folder = String.format("knowledge/%d", userId);
            String filePath = localFileUtil.uploadFile(file, folder);
            String fileUrl = localFileUtil.generateUrl(filePath);
            
            // 处理文档内容
            List<Document> documents = processDocument(file, fileId, subject, category, title, tags, userId, filePath);
            
            // 存储到向量数据库
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("成功向量化 {} 个文档块", documents.size());
            }
            
            return UploadResultVO.builder()
                    .id(fileId)
                    .fileName(file.getOriginalFilename())
                    .fileType(getFileType(file.getOriginalFilename()))
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .status("processed")
                    .message("文档上传并处理成功")
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
                    
        } catch (Exception e) {
            log.error("上传文档失败", e);
            throw new RuntimeException("上传文档失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteKnowledgeItem(String itemId, Integer userId) {
        try {
            log.info("删除知识库项目，用户ID：{}，项目ID：{}", userId, itemId);
            
            // 从向量数据库删除
            vectorStore.delete(List.of(itemId));
            
            // TODO: 如果需要删除本地文件，可以在这里添加逻辑
            // 需要先从向量数据库查询文件路径，然后调用 localFileUtil.deleteFile()
            
            log.info("成功删除知识库项目：{}", itemId);
            
        } catch (Exception e) {
            log.error("删除知识库项目失败", e);
            throw new RuntimeException("删除知识库项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public KnowledgeItemVO updateKnowledgeItem(String itemId, String title, String content, 
                                              String tags, Integer userId) {
        try {
            log.info("更新知识库项目，用户ID：{}，项目ID：{}", userId, itemId);
            
            // 先删除原有文档
            deleteKnowledgeItem(itemId, userId);
            
            // 创建新文档
            Document document = new Document(content);
            document.getMetadata().put("id", itemId);
            document.getMetadata().put("title", title);
            document.getMetadata().put("tags", tags);
            document.getMetadata().put("user_id", userId);
            document.getMetadata().put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 重新添加到向量数据库
            vectorStore.add(List.of(document));
            
            return convertToKnowledgeItemVO(document);
            
        } catch (Exception e) {
            log.error("更新知识库项目失败", e);
            throw new RuntimeException("更新知识库项目失败: " + e.getMessage());
        }
    }
    
    private List<Document> processDocument(MultipartFile file, String fileId, String subject, 
                                         String category, String title, String tags, Integer userId, String filePath) throws Exception {
        
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        
        List<Document> documents;
        String fileName = file.getOriginalFilename().toLowerCase();
        
        // 根据文件类型选择不同的文档读取器
        if (fileName.endsWith(".pdf")) {
            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            documents = reader.get();
        } else {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            documents = reader.get();
        }
        
        // 文档分块
        TokenTextSplitter textSplitter = new TokenTextSplitter(1000, 200, 5, 10000, true);
        documents = textSplitter.apply(documents);
        
        // 添加元数据
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            doc.getMetadata().put("id", fileId + "_chunk_" + i);
            doc.getMetadata().put("file_id", fileId);
            doc.getMetadata().put("source", file.getOriginalFilename());
            doc.getMetadata().put("file_path", filePath); // 本地文件路径
            doc.getMetadata().put("file_url", localFileUtil.generateUrl(filePath)); // 访问URL
            doc.getMetadata().put("subject", subject != null ? subject : "");
            doc.getMetadata().put("category", category != null ? category : "");
            doc.getMetadata().put("title", title != null ? title : file.getOriginalFilename());
            doc.getMetadata().put("tags", tags != null ? tags : "");
            doc.getMetadata().put("user_id", userId);
            doc.getMetadata().put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return documents;
    }
    
    private KnowledgeItemVO convertToKnowledgeItemVO(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        
        return KnowledgeItemVO.builder()
                .id((String) metadata.get("id"))
                .title((String) metadata.getOrDefault("title", ""))
                .content(document.getText()) // 修改为 getText()
                .source((String) metadata.getOrDefault("source", ""))
                .subject((String) metadata.getOrDefault("subject", ""))
                .grade((String) metadata.getOrDefault("grade", ""))
                .tags(parseTags((String) metadata.getOrDefault("tags", "")))
                .similarity((Double) metadata.get("distance"))
                .createdAt((String) metadata.getOrDefault("created_at", ""))
                .build();
    }
    
    private List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(tagsStr.split(","));
    }
    
    private String getFileType(String fileName) {
        if (fileName == null) return "unknown";
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "document";
            case "doc":
            case "docx": return "document";
            case "txt": return "document";
            case "jpg":
            case "jpeg":
            case "png": return "image";
            case "mp3":
            case "wav": return "audio";
            default: return "document";
        }
    }
}

