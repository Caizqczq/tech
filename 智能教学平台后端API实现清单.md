# 智能教学平台后端API实现清单

## 📋 概述
基于前端实际调用的API接口，以下是后端必须实现的接口清单，确保前端能够正常运行。

**基础URL**: `http://localhost:8082/api`
**认证方式**: JWT Bearer Token

---

## 🔐 一、用户认证模块 (`/api/auth`)

### 1.1 用户登录
```java
@PostMapping("/auth/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // 返回格式：
    // {
    //   "user": { "userId": "...", "username": "...", "email": "..." },
    //   "token": "JWT_TOKEN"
    // }
}
```

### 1.2 用户注册
```java
@PostMapping("/auth/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    // 返回格式：
    // {
    //   "userId": "...",
    //   "username": "...",
    //   "email": "...",
    //   "role": "teacher"
    // }
}
```

### 1.3 用户登出
```java
@PostMapping("/auth/logout")
public ResponseEntity<?> logout() {
    // 返回格式：{ "message": "登出成功" }
}
```

### 1.4 获取当前用户
```java
@GetMapping("/auth/me")
public ResponseEntity<?> getCurrentUser() {
    // 返回格式：
    // {
    //   "userId": "...",
    //   "username": "...",
    //   "email": "...",
    //   "role": "teacher"
    // }
}
```

### 1.5 测试接口
```java
@GetMapping("/auth/hello")
public ResponseEntity<?> hello() {
    // 返回格式：
    // {
    //   "message": "Hello from Smart Teaching Platform",
    //   "timestamp": "2024-01-15T10:30:00Z",
    //   "user": { "userId": "...", "username": "...", "email": "..." }
    // }
}
```

---

## 🤖 二、AI对话模块

### 2.1 基础对话 (`/api`)
```java
@GetMapping("/simple/chat")
public ResponseEntity<?> simpleChat(@RequestParam String query, @RequestParam(defaultValue = "1") String chatId) {
    // 返回格式：
    // {
    //   "content": "AI回复内容",
    //   "conversationId": "conv_123",
    //   "usage": { "promptTokens": 100, "completionTokens": 150, "totalTokens": 250 },
    //   "responseTime": "500ms"
    // }
}

@GetMapping("/stream/chat")
public ResponseEntity<?> streamChat(@RequestParam String query, @RequestParam(defaultValue = "1") String chatId) {
    // 返回SSE流
}

@PostMapping("/image/analyze/url")
public ResponseEntity<?> analyzeImageByUrl(@RequestParam String imageUrl, @RequestParam String prompt) {
    // 返回格式：
    // {
    //   "content": "图片分析结果",
    //   "imageUrl": "...",
    //   "usage": { "promptTokens": 100, "completionTokens": 200, "totalTokens": 300 },
    //   "responseTime": "1200ms"
    // }
}

@PostMapping("/image/analyze/upload")
public ResponseEntity<?> analyzeImageByUpload(@RequestParam MultipartFile file, @RequestParam String prompt) {
    // 返回格式：
    // {
    //   "content": "图片分析结果",
    //   "fileName": "image.jpg",
    //   "fileSize": 1024,
    //   "usage": { "promptTokens": 150, "completionTokens": 250, "totalTokens": 400 },
    //   "responseTime": "1500ms"
    // }
}
```

### 2.2 教学AI功能 (`/api/chat`)
```java
@PostMapping("/chat/teaching-advice")
public ResponseEntity<?> getTeachingAdvice(@RequestBody TeachingAdviceRequest request) {
    // 请求字段：subject, grade, topic, difficulty, requirements
    // 返回格式：
    // {
    //   "advice": "教学建议",
    //   "suggestions": ["建议1", "建议2"],
    //   "resources": ["资源1", "资源2"],
    //   "usage": { "promptTokens": 200, "completionTokens": 300, "totalTokens": 500 },
    //   "responseTime": "800ms"
    // }
}

@PostMapping("/chat/content-analysis")
public ResponseEntity<?> analyzeContent(@RequestBody ContentAnalysisRequest request) {
    // 请求字段：content, analysisType, requirements
    // 返回格式：
    // {
    //   "analysis": "分析结果",
    //   "keyPoints": ["要点1", "要点2"],
    //   "improvements": ["改进1", "改进2"],
    //   "score": 85,
    //   "usage": { "promptTokens": 250, "completionTokens": 400, "totalTokens": 650 },
    //   "responseTime": "1000ms"
    // }
}

@PostMapping("/chat/writing-assistance")
public ResponseEntity<?> getWritingAssistance(@RequestBody WritingAssistanceRequest request) {
    // 请求字段：content, assistanceType, requirements
    // 返回格式：
    // {
    //   "assistance": "写作建议",
    //   "suggestions": ["建议1", "建议2"],
    //   "corrections": ["修正1", "修正2"],
    //   "usage": { "promptTokens": 180, "completionTokens": 320, "totalTokens": 500 },
    //   "responseTime": "900ms"
    // }
}

@PostMapping("/chat/assistant")
public ResponseEntity<?> chatWithAssistant(@RequestBody AssistantChatRequest request) {
    // 请求字段：message, conversationId, mode, context
    // 返回格式：
    // {
    //   "response": "AI回复",
    //   "conversationId": "conv_123",
    //   "mode": "teaching",
    //   "context": { "subject": "数学", "grade": "高中", "topic": "函数" },
    //   "usage": { "promptTokens": 150, "completionTokens": 280, "totalTokens": 430 },
    //   "responseTime": "750ms"
    // }
}

@PostMapping("/chat/assistant/stream")
public ResponseEntity<?> streamChatWithAssistant(@RequestBody AssistantChatRequest request) {
    // 返回SSE流
}
```

---

## 💬 三、对话管理模块 (`/api/chat`)

```java
@GetMapping("/chat/conversations")
public ResponseEntity<?> getConversations(@RequestParam(defaultValue = "1") int page, 
                                         @RequestParam(defaultValue = "10") int size) {
    // 返回格式：
    // {
    //   "conversations": [
    //     {
    //       "conversationId": "conv_123",
    //       "title": "数学讨论",
    //       "lastMessage": "最后一条消息",
    //       "messageCount": 5,
    //       "createdAt": "2024-01-15T10:30:00Z",
    //       "updatedAt": "2024-01-15T11:30:00Z"
    //     }
    //   ],
    //   "pagination": { "page": 1, "size": 10, "total": 50, "totalPages": 5 }
    // }
}

@GetMapping("/chat/conversations/{conversationId}")
public ResponseEntity<?> getConversationDetail(@PathVariable String conversationId) {
    // 返回格式：
    // {
    //   "conversation": {
    //     "conversationId": "conv_123",
    //     "title": "数学讨论",
    //     "createdAt": "2024-01-15T10:30:00Z",
    //     "updatedAt": "2024-01-15T11:30:00Z"
    //   },
    //   "messages": [
    //     {
    //       "role": "user",
    //       "content": "用户消息",
    //       "timestamp": "2024-01-15T10:30:00Z"
    //     }
    //   ]
    // }
}

@DeleteMapping("/chat/conversations/{conversationId}")
public ResponseEntity<?> deleteConversation(@PathVariable String conversationId) {
    // 返回格式：{ "message": "删除成功" }
}

@DeleteMapping("/chat/conversations")
public ResponseEntity<?> clearAllConversations() {
    // 返回格式：{ "message": "清空成功" }
}

@PutMapping("/chat/{conversationId}/title")
public ResponseEntity<?> updateConversationTitle(@PathVariable String conversationId, 
                                                @RequestBody Map<String, String> request) {
    // 请求字段：title
    // 返回格式：
    // {
    //   "conversationId": "conv_123",
    //   "title": "新标题",
    //   "updatedAt": "2024-01-15T11:30:00Z"
    // }
}

@GetMapping("/chat/stats")
public ResponseEntity<?> getConversationStats() {
    // 返回格式：
    // {
    //   "totalConversations": 25,
    //   "totalMessages": 150,
    //   "averageMessagesPerConversation": 6.0,
    //   "mostActiveDay": "2024-01-15",
    //   "conversationsByDate": [
    //     { "date": "2024-01-15", "count": 5 }
    //   ]
    // }
}
```

---

## 📁 四、资源管理模块 (`/api/resources`)

### 4.1 文件上传
```java
@PostMapping("/resources/upload/document")
public ResponseEntity<?> uploadDocument(@RequestParam MultipartFile file,
                                       @RequestParam String subject,
                                       @RequestParam String courseLevel,
                                       @RequestParam String resourceType,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) String keywords,
                                       @RequestParam(defaultValue = "true") boolean autoVectorize,
                                       @RequestParam(defaultValue = "true") boolean autoExtractKeywords) {
    // 返回格式：
    // {
    //   "resourceId": "res_123",
    //   "fileName": "document.pdf",
    //   "fileSize": 1024,
    //   "resourceType": "lesson_plan",
    //   "subject": "数学",
    //   "courseLevel": "undergraduate",
    //   "uploadTime": "2024-01-15T10:30:00Z",
    //   "status": "processing",
    //   "extractedKeywords": ["关键词1", "关键词2"],
    //   "vectorizationStatus": "pending"
    // }
}

@PostMapping("/resources/upload/audio")
public ResponseEntity<?> uploadAudio(@RequestParam MultipartFile file,
                                    @RequestParam(defaultValue = "sync") String transcriptionMode,
                                    @RequestParam(defaultValue = "true") boolean needTranscription,
                                    @RequestParam(required = false) String subject,
                                    @RequestParam(required = false) String resourceType,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String speaker,
                                    @RequestParam(defaultValue = "zh") String language,
                                    @RequestParam(defaultValue = "true") boolean autoVectorize) {
    // 返回格式：
    // {
    //   "resourceId": "res_123",
    //   "fileName": "audio.mp3",
    //   "fileSize": 1024,
    //   "transcriptionText": "转录文本",
    //   "transcriptionMode": "sync",
    //   "language": "zh",
    //   "duration": 120,
    //   "uploadTime": "2024-01-15T10:30:00Z"
    // }
}

@PostMapping("/resources/upload/batch")
public ResponseEntity<?> uploadBatch(@RequestParam MultipartFile[] files,
                                    @RequestParam String subject,
                                    @RequestParam String courseLevel,
                                    @RequestParam(defaultValue = "true") boolean autoVectorize) {
    // 返回格式：
    // {
    //   "successCount": 8,
    //   "failedCount": 2,
    //   "results": [
    //     {
    //       "fileName": "file1.pdf",
    //       "status": "success",
    //       "resourceId": "res_123"
    //     }
    //   ],
    //   "uploadTime": "2024-01-15T10:30:00Z"
    // }
}
```

### 4.2 资源查询
```java
@GetMapping("/resources")
public ResponseEntity<?> getResources(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(required = false) String resourceType,
                                     @RequestParam(required = false) String keywords) {
    // 返回格式：
    // {
    //   "content": [
    //     {
    //       "resourceId": "res_123",
    //       "fileName": "document.pdf",
    //       "resourceType": "lesson_plan",
    //       "subject": "数学",
    //       "courseLevel": "undergraduate",
    //       "description": "描述",
    //       "fileSize": 1024,
    //       "uploadTime": "2024-01-15T10:30:00Z",
    //       "downloadCount": 10,
    //       "status": "ready"
    //     }
    //   ],
    //   "number": 0,
    //   "size": 20,
    //   "totalElements": 100,
    //   "totalPages": 5
    // }
}

@GetMapping("/resources/search/semantic")
public ResponseEntity<?> searchResourcesSemantic(@RequestParam String query,
                                                @RequestParam(defaultValue = "10") int topK,
                                                @RequestParam(defaultValue = "0.7") double threshold) {
    // 返回格式：
    // [
    //   {
    //     "resourceId": "res_123",
    //     "fileName": "document.pdf",
    //     "resourceType": "lesson_plan",
    //     "relevantContent": "相关内容片段",
    //     "similarity": 0.85,
    //     "uploadTime": "2024-01-15T10:30:00Z"
    //   }
    // ]
}

@GetMapping("/resources/{resourceId}")
public ResponseEntity<?> getResourceDetail(@PathVariable String resourceId) {
    // 返回格式：
    // {
    //   "resourceId": "res_123",
    //   "fileName": "document.pdf",
    //   "resourceType": "lesson_plan",
    //   "subject": "数学",
    //   "courseLevel": "undergraduate",
    //   "description": "描述",
    //   "fileSize": 1024,
    //   "extractedContent": "提取的文本内容",
    //   "metadata": {
    //     "pageCount": 50,
    //     "wordCount": 5000,
    //     "language": "zh",
    //     "keywords": ["关键词1", "关键词2"]
    //   },
    //   "uploadTime": "2024-01-15T10:30:00Z",
    //   "downloadCount": 15
    // }
}

@DeleteMapping("/resources/{resourceId}")
public ResponseEntity<?> deleteResource(@PathVariable String resourceId) {
    // 返回格式：{ "message": "删除成功" }
}

@GetMapping("/resources/{resourceId}/download")
public ResponseEntity<?> getResourceDownloadUrl(@PathVariable String resourceId) {
    // 返回格式：
    // {
    //   "downloadUrl": "https://example.com/download/res_123",
    //   "fileName": "document.pdf",
    //   "expiresAt": "2024-01-15T12:30:00Z"
    // }
}

@GetMapping("/resources/stats")
public ResponseEntity<?> getResourceStats() {
    // 返回格式：
    // {
    //   "totalResources": 100,
    //   "totalSize": "2.3 GB",
    //   "resourcesByType": {
    //     "lesson_plan": 45,
    //     "paper": 30,
    //     "textbook": 25
    //   },
    //   "uploadTrend": [
    //     { "date": "2024-01-15", "count": 5 }
    //   ]
    // }
}
```

---

## 🧠 五、知识库管理模块 (`/api/resources`)

```java
@PostMapping("/resources/knowledge-base")
public ResponseEntity<?> createKnowledgeBase(@RequestBody KnowledgeBaseRequest request) {
    // 请求字段：name, description, resourceIds
    // 返回格式：
    // {
    //   "knowledgeBaseId": "kb_123",
    //   "name": "数学知识库",
    //   "description": "描述",
    //   "resourceCount": 5,
    //   "status": "building",
    //   "createdAt": "2024-01-15T10:30:00Z"
    // }
}

@GetMapping("/resources/knowledge-base/{knowledgeBaseId}/status")
public ResponseEntity<?> getKnowledgeBaseBuildStatus(@PathVariable String knowledgeBaseId) {
    // 返回格式：
    // {
    //   "knowledgeBaseId": "kb_123",
    //   "status": "building",
    //   "progress": 75,
    //   "message": "正在构建中...",
    //   "completedAt": null
    // }
}

@GetMapping("/resources/knowledge-base")
public ResponseEntity<?> getKnowledgeBases(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
    // 返回格式：
    // {
    //   "knowledgeBases": [
    //     {
    //       "knowledgeBaseId": "kb_123",
    //       "name": "数学知识库",
    //       "description": "描述",
    //       "resourceCount": 10,
    //       "status": "ready",
    //       "createdAt": "2024-01-15T10:30:00Z",
    //       "updatedAt": "2024-01-15T11:30:00Z"
    //     }
    //   ],
    //   "pagination": { "page": 1, "size": 10, "total": 5, "totalPages": 1 }
    // }
}

@PostMapping("/resources/qa")
public ResponseEntity<?> ragQuery(@RequestBody RagQueryRequest request) {
    // 请求字段：knowledgeBaseId, query, topK
    // 返回格式：
    // {
    //   "answer": "AI生成的回答",
    //   "sources": [
    //     {
    //       "resourceId": "res_123",
    //       "fileName": "document.pdf",
    //       "relevantContent": "相关内容",
    //       "similarity": 0.9
    //     }
    //   ],
    //   "query": "用户问题",
    //   "knowledgeBaseId": "kb_123",
    //   "usage": { "promptTokens": 200, "completionTokens": 300, "totalTokens": 500 },
    //   "responseTime": "1200ms"
    // }
}

@PostMapping("/resources/qa/stream")
public ResponseEntity<?> ragStreamQuery(@RequestBody RagQueryRequest request) {
    // 返回SSE流
}
```

---

## 🎯 六、AI生成模块 (`/api/ai/generate`)

```java
@PostMapping("/ai/generate/ppt")
public ResponseEntity<?> generatePPT(@RequestBody PPTGenerationRequest request) {
    // 请求字段：topic, subject, courseLevel, slideCount, style, includeFormulas, includeProofs, targetAudience, duration, language
    // 返回格式：
    // {
    //   "taskId": "task_123",
    //   "status": "pending",
    //   "message": "PPT生成任务已创建",
    //   "createdAt": "2024-01-15T10:30:00Z"
    // }
}

@PostMapping("/ai/generate/quiz")
public ResponseEntity<?> generateQuiz(@RequestBody QuizGenerationRequest request) {
    // 请求字段：topic, subject, courseLevel, difficulty, questionCount, questionTypes, includeSteps, includeAnswers, timeLimit, language
    // 返回格式：
    // {
    //   "taskId": "task_123",
    //   "status": "pending",
    //   "message": "习题生成任务已创建",
    //   "createdAt": "2024-01-15T10:30:00Z"
    // }
}

@PostMapping("/ai/generate/explanation")
public ResponseEntity<?> generateExplanation(@RequestBody ExplanationGenerationRequest request) {
    // 请求字段：topic, subject, courseLevel, style, length, includeExamples, includeProofs, includeApplications, targetAudience, language
    // 返回格式：
    // {
    //   "taskId": "task_123",
    //   "status": "pending",
    //   "message": "讲解生成任务已创建",
    //   "createdAt": "2024-01-15T10:30:00Z"
    // }
}
```

---

## ⏳ 七、任务管理模块 (`/api/tasks`)

```java
@GetMapping("/tasks/{taskId}/status")
public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
    // 返回格式：
    // {
    //   "taskId": "task_123",
    //   "status": "pending",
    //   "progress": 75,
    //   "result": {
    //     "downloadUrl": "https://example.com/download/task_123",
    //     "content": "生成的内容",
    //     "metadata": {}
    //   },
    //   "error": null,
    //   "createdAt": "2024-01-15T10:30:00Z",
    //   "completedAt": null
    // }
}

@DeleteMapping("/tasks/{taskId}")
public ResponseEntity<?> cancelTask(@PathVariable String taskId) {
    // 返回格式：{ "message": "任务已取消" }
}
```

---

## 🚨 八、错误处理规范

### HTTP状态码
- `200 OK`: 请求成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 认证失败
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误

### 错误响应格式
```json
{
  "error": "ERROR_CODE",
  "message": "错误描述",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/endpoint"
}
```

---

## 📝 九、实现优先级

### 🔴 高优先级（必须实现）
1. **用户认证模块** - 所有接口
2. **基础AI对话** - `/simple/chat`, `/stream/chat`
3. **文件上传** - `/resources/upload/document`, `/resources/upload/audio`
4. **资源查询** - `/resources`, `/resources/{id}`
5. **AI生成** - `/ai/generate/explanation` (PPT和Quiz可稍后)

### 🟡 中优先级（建议实现）
1. **对话管理** - 对话历史和管理
2. **知识库管理** - RAG问答功能
3. **任务管理** - 生成任务状态跟踪

### 🟢 低优先级（可选）
1. **统计功能** - 各种stats接口
2. **图片分析** - 图片相关AI功能
3. **高级教学AI** - 教学建议等

---

**✅ 实现建议：**
1. 先实现高优先级接口，确保基础功能可用
2. 返回数据格式严格按照前端期望的格式
3. 认证机制必须正确实现JWT
4. 文件上传功能是核心，需要重点实现
5. AI生成功能可以先返回模拟数据，后续完善

按照这个清单实现后端接口，前端项目就能够正常运行！