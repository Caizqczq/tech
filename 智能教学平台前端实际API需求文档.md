# 智能教学平台前端实际API需求文档

## 📋 概述

本文档基于前端`src/services/api.ts`的实际API调用分析，详细描述了后端必须实现的接口，确保前后端完美对接。

**版本**: v1.0  
**基础URL**: `http://localhost:8082/api`  
**认证方式**: JWT Bearer Token  
**Token获取**: localStorage.getItem('token')

---

## 🔐 一、用户认证模块 (`/api/auth`)

### 1.1 用户登录
```http
POST /api/auth/login
```

**前端调用**: `apiService.login(credentials)`

**请求体**:
```json
{
  "email": "string (必填)",
  "password": "string (必填)"
}
```

**响应**:
```json
{
  "user": {
    "userId": "string",
    "username": "string",
    "email": "string",
    "role": "string (可选)",
    "subject": "string (可选)",
    "institution": "string (可选)"
  },
  "token": "string (JWT token)"
}
```

### 1.2 用户注册
```http
POST /api/auth/register
```

**前端调用**: `apiService.register(userData)`

**请求体**:
```json
{
  "username": "string (必填)",
  "email": "string (必填)",
  "password": "string (必填)",
  "role": "string (可选)",
  "subject": "string (可选)",
  "institution": "string (可选)"
}
```

**响应**:
```json
{
  "userId": "string",
  "username": "string",
  "email": "string",
  "role": "string",
  "subject": "string",
  "institution": "string",
  "createdAt": "ISO8601时间"
}
```

### 1.3 用户登出
```http
POST /api/auth/logout
```

**前端调用**: `apiService.logout()`

**响应**: 空响应体，状态码200

### 1.4 获取当前用户信息
```http
GET /api/auth/me
```

**前端调用**: `apiService.getCurrentUser()`

**响应**:
```json
{
  "userId": "string",
  "username": "string",
  "email": "string",
  "role": "string",
  "subject": "string",
  "institution": "string",
  "createdAt": "ISO8601时间",
  "lastLoginAt": "ISO8601时间"
}
```

---

## 🤖 二、AI对话模块

### 2.1 基础对话接口 (`/api`)

#### 2.1.1 简单对话
```http
GET /api/simple/chat?query={query}&chat-id={chatId}
```

**前端调用**: `apiService.simpleChat(query, chatId)`

**查询参数**:
- `query`: string (必填) - 用户问题
- `chat-id`: string (可选, 默认"1") - 对话ID

**响应**:
```json
{
  "content": "string",
  "conversationId": "string",
  "usage": {
    "promptTokens": 100,
    "completionTokens": 150,
    "totalTokens": 250
  },
  "responseTime": "500ms"
}
```

#### 2.1.2 流式对话
```http
GET /api/stream/chat?query={query}&chat-id={chatId}
```

**前端调用**: `apiService.streamChat(query, chatId)`

**响应**: Server-Sent Events流

#### 2.1.3 图片分析（URL）
```http
POST /api/image/analyze/url
Content-Type: multipart/form-data
```

**前端调用**: `apiService.analyzeImageByUrl(imageUrl, prompt)`

**请求体**:
```
prompt: string (可选)
imageUrl: string (必填)
```

**响应**:
```json
{
  "content": "string",
  "imageUrl": "string",
  "usage": {
    "promptTokens": 100,
    "completionTokens": 200,
    "totalTokens": 300
  },
  "responseTime": "1200ms"
}
```

#### 2.1.4 图片分析（上传）
```http
POST /api/image/analyze/upload
Content-Type: multipart/form-data
```

**前端调用**: `apiService.analyzeImageByUpload(file, prompt)`

**请求体**:
```
file: File (必填)
prompt: string (可选)
```

**响应**:
```json
{
  "content": "string",
  "fileName": "string",
  "fileSize": 1024,
  "usage": {
    "promptTokens": 150,
    "completionTokens": 250,
    "totalTokens": 400
  },
  "responseTime": "1500ms"
}
```

### 2.2 教学AI对话接口 (`/api/chat`)

#### 2.2.1 教学建议
```http
POST /api/chat/teaching-advice
```

**前端调用**: `apiService.getTeachingAdvice(request)`

**请求体**:
```json
{
  "subject": "string (必填)",
  "grade": "string (必填)",
  "topic": "string (必填)",
  "difficulty": "string (必填)",
  "requirements": "string (可选)"
}
```

**响应**:
```json
{
  "advice": "string",
  "suggestions": ["string"],
  "resources": ["string"],
  "usage": {
    "promptTokens": 200,
    "completionTokens": 300,
    "totalTokens": 500
  },
  "responseTime": "800ms"
}
```

#### 2.2.2 内容分析
```http
POST /api/chat/content-analysis
```

**前端调用**: `apiService.analyzeContent(request)`

**请求体**:
```json
{
  "content": "string (必填)",
  "analysisType": "string (必填)",
  "requirements": "string (可选)"
}
```

**响应**:
```json
{
  "analysis": "string",
  "keyPoints": ["string"],
  "improvements": ["string"],
  "score": 85,
  "usage": {
    "promptTokens": 250,
    "completionTokens": 400,
    "totalTokens": 650
  },
  "responseTime": "1000ms"
}
```

#### 2.2.3 写作辅助
```http
POST /api/chat/writing-assistance
```

**前端调用**: `apiService.getWritingAssistance(request)`

**请求体**:
```json
{
  "content": "string (必填)",
  "assistanceType": "string (必填)",
  "requirements": "string (可选)"
}
```

**响应**:
```json
{
  "assistance": "string",
  "suggestions": ["string"],
  "corrections": ["string"],
  "usage": {
    "promptTokens": 180,
    "completionTokens": 320,
    "totalTokens": 500
  },
  "responseTime": "900ms"
}
```

#### 2.2.4 智能助手对话
```http
POST /api/chat/assistant
```

**前端调用**: `apiService.chatWithAssistant(request)`

**请求体**:
```json
{
  "message": "string (必填)",
  "conversationId": "string (可选)",
  "mode": "string (可选)",
  "context": {
    "subject": "string (可选)",
    "grade": "string (可选)",
    "topic": "string (可选)"
  }
}
```

**响应**:
```json
{
  "response": "string",
  "conversationId": "string",
  "mode": "string",
  "context": {
    "subject": "string",
    "grade": "string",
    "topic": "string"
  },
  "usage": {
    "promptTokens": 150,
    "completionTokens": 280,
    "totalTokens": 430
  },
  "responseTime": "750ms"
}
```

#### 2.2.5 流式智能助手对话
```http
POST /api/chat/assistant/stream
```

**前端调用**: `apiService.chatWithAssistantStream(request)`

**请求体**: 同上

**响应**: Server-Sent Events流

---

## 💬 三、对话管理模块 (`/api/chat`)

### 3.1 获取对话列表
```http
GET /api/chat/conversations?page={page}&size={size}
```

**前端调用**: `apiService.getConversations(page, size)`

**查询参数**:
- `page`: number (可选, 默认1)
- `size`: number (可选, 默认10)

**响应**:
```json
{
  "conversations": [
    {
      "conversationId": "string",
      "title": "string",
      "lastMessage": "string",
      "messageCount": 5,
      "createdAt": "ISO8601时间",
      "updatedAt": "ISO8601时间"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 50,
    "totalPages": 5
  }
}
```

### 3.2 获取对话详情
```http
GET /api/chat/conversations/{conversationId}
```

**前端调用**: `apiService.getConversationDetail(conversationId)`

**响应**:
```json
{
  "conversation": {
    "conversationId": "string",
    "title": "string",
    "createdAt": "ISO8601时间",
    "updatedAt": "ISO8601时间"
  },
  "messages": [
    {
      "role": "user | assistant",
      "content": "string",
      "timestamp": "ISO8601时间"
    }
  ]
}
```

### 3.3 删除对话
```http
DELETE /api/chat/conversations/{conversationId}
```

**前端调用**: `apiService.deleteConversation(conversationId)`

### 3.4 清空所有对话
```http
DELETE /api/chat/conversations
```

**前端调用**: `apiService.clearAllConversations()`

### 3.5 更新对话标题
```http
PUT /api/chat/{conversationId}/title
```

**前端调用**: `apiService.updateConversationTitle(conversationId, title)`

**请求体**:
```json
{
  "title": "string (必填)"
}
```

**响应**:
```json
{
  "conversationId": "string",
  "title": "string",
  "updatedAt": "ISO8601时间"
}
```

### 3.6 获取对话统计
```http
GET /api/chat/stats
```

**前端调用**: `apiService.getConversationStats()`

**响应**:
```json
{
  "totalConversations": 25,
  "totalMessages": 150,
  "averageMessagesPerConversation": 6.0,
  "mostActiveDay": "2024-01-15",
  "conversationsByDate": [
    {
      "date": "2024-01-15",
      "count": 5
    }
  ]
}
```

---

## 🎯 四、AI生成模块 (`/api/ai/generate`)

### 4.1 生成教学讲解
```http
POST /api/ai/generate/explanation
```

**前端调用**: `apiService.generateExplanation(request)`

**请求体**:
```json
{
  "topic": "string (必填)",
  "subject": "string (必填)",
  "courseLevel": "string (必填)",
  "style": "string (可选)",
  "length": "string (可选)",
  "includeExamples": true,
  "includeProofs": false,
  "includeApplications": true,
  "targetAudience": "string (可选)",
  "language": "string (可选, 默认zh)"
}
```

**响应**:
```json
{
  "taskId": "string",
  "status": "pending | processing | completed | failed",
  "message": "string",
  "createdAt": "ISO8601时间"
}
```

### 4.2 生成PPT
```http
POST /api/ai/generate/ppt
```

**前端调用**: `apiService.generatePPT(request)`

**请求体**:
```json
{
  "topic": "string (必填)",
  "subject": "string (必填)",
  "courseLevel": "string (必填)",
  "slideCount": 20,
  "style": "string (可选)",
  "includeFormulas": true,
  "includeProofs": false,
  "targetAudience": "string (可选)",
  "duration": 45,
  "language": "string (可选, 默认zh)"
}
```

**响应**:
```json
{
  "taskId": "string",
  "status": "pending",
  "message": "PPT生成任务已创建",
  "createdAt": "ISO8601时间"
}
```

### 4.3 生成习题
```http
POST /api/ai/generate/quiz
```

**前端调用**: `apiService.generateQuiz(request)`

**请求体**:
```json
{
  "topic": "string (必填)",
  "subject": "string (必填)",
  "courseLevel": "string (必填)",
  "difficulty": "string (可选)",
  "questionCount": 10,
  "questionTypes": "string (可选)",
  "includeSteps": true,
  "includeAnswers": true,
  "timeLimit": 60,
  "language": "string (可选, 默认zh)"
}
```

**响应**:
```json
{
  "taskId": "string",
  "status": "pending",
  "message": "习题生成任务已创建",
  "createdAt": "ISO8601时间"
}
```

---

## 📁 五、资源管理模块 (`/api/resources`)

### 5.1 文档上传
```http
POST /api/resources/upload/document
Content-Type: multipart/form-data
```

**前端调用**: `apiService.uploadDocument(file, params)`

**请求体**:
```
file: File (必填)
subject: string (必填)
courseLevel: string (必填)
resourceType: string (必填)
title: string (可选)
description: string (可选)
keywords: string (可选)
autoVectorize: boolean (可选, 默认true)
autoExtractKeywords: boolean (可选, 默认true)
```

**响应**:
```json
{
  "resourceId": "string",
  "fileName": "string",
  "fileSize": 1024,
  "resourceType": "string",
  "subject": "string",
  "courseLevel": "string",
  "uploadTime": "ISO8601时间",
  "status": "processing | ready | vectorized",
  "extractedKeywords": ["string"],
  "vectorizationStatus": "pending | processing | completed"
}
```

### 5.2 音频上传
```http
POST /api/resources/upload/audio
Content-Type: multipart/form-data
```

**前端调用**: `apiService.uploadAudio(file, params)`

**请求体**:
```
file: File (必填)
transcriptionMode: string (可选, 默认sync)
needTranscription: boolean (可选, 默认true)
subject: string (可选)
resourceType: string (可选)
description: string (可选)
speaker: string (可选)
language: string (可选, 默认zh)
autoVectorize: boolean (可选, 默认true)
```

**响应**:
```json
{
  "resourceId": "string",
  "fileName": "string",
  "fileSize": 1024,
  "transcriptionText": "string",
  "transcriptionMode": "sync | async",
  "language": "string",
  "duration": 120,
  "uploadTime": "ISO8601时间"
}
```

### 5.3 批量上传
```http
POST /api/resources/upload/batch
Content-Type: multipart/form-data
```

**前端调用**: `apiService.uploadBatch(files, params)`

**请求体**:
```
files: File[] (必填)
subject: string (必填)
courseLevel: string (必填)
autoVectorize: boolean (可选, 默认true)
```

**响应**:
```json
{
  "successCount": 8,
  "failedCount": 2,
  "results": [
    {
      "fileName": "string",
      "status": "success | failed",
      "resourceId": "string",
      "error": "string (失败时)"
    }
  ],
  "uploadTime": "ISO8601时间"
}
```

### 5.4 获取资源列表
```http
GET /api/resources?page={page}&size={size}&resourceType={type}&keywords={keywords}
```

**前端调用**: `apiService.getResources(params)`

**查询参数**:
- `page`: number (可选, 默认0)
- `size`: number (可选, 默认20)
- `resourceType`: string (可选)
- `keywords`: string (可选)

**响应**:
```json
{
  "content": [
    {
      "resourceId": "string",
      "fileName": "string",
      "resourceType": "string",
      "subject": "string",
      "courseLevel": "string",
      "description": "string",
      "fileSize": 1024,
      "uploadTime": "ISO8601时间",
      "downloadCount": 10,
      "status": "processing | ready | vectorized"
    }
  ],
  "number": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### 5.5 语义搜索
```http
GET /api/resources/search/semantic?query={query}&topK={topK}&threshold={threshold}
```

**前端调用**: `apiService.searchResourcesSemantic(params)`

**查询参数**:
- `query`: string (必填)
- `topK`: number (可选, 默认10)
- `threshold`: number (可选, 默认0.7)

**响应**:
```json
[
  {
    "resourceId": "string",
    "fileName": "string",
    "resourceType": "string",
    "relevantContent": "string",
    "similarity": 0.85,
    "uploadTime": "ISO8601时间"
  }
]
```

### 5.6 获取资源详情
```http
GET /api/resources/{resourceId}
```

**前端调用**: `apiService.getResourceDetail(resourceId)`

**响应**:
```json
{
  "resourceId": "string",
  "fileName": "string",
  "resourceType": "string",
  "subject": "string",
  "courseLevel": "string",
  "description": "string",
  "fileSize": 1024,
  "extractedContent": "string",
  "metadata": {
    "pageCount": 50,
    "wordCount": 5000,
    "language": "zh",
    "keywords": ["string"]
  },
  "uploadTime": "ISO8601时间",
  "downloadCount": 15
}
```

### 5.7 删除资源
```http
DELETE /api/resources/{resourceId}
```

**前端调用**: `apiService.deleteResource(resourceId)`

### 5.8 获取下载链接
```http
GET /api/resources/{resourceId}/download
```

**前端调用**: `apiService.getResourceDownloadUrl(resourceId)`

**响应**:
```json
{
  "downloadUrl": "string",
  "fileName": "string",
  "expiresAt": "ISO8601时间"
}
```

---

## 🧠 六、知识库管理模块 (`/api/resources/knowledge-base`)

### 6.1 创建知识库
```http
POST /api/resources/knowledge-base
```

**前端调用**: `apiService.createKnowledgeBase(params)`

**请求体**:
```json
{
  "name": "string (必填)",
  "description": "string (可选)",
  "resourceIds": ["string"]
}
```

**响应**:
```json
{
  "knowledgeBaseId": "string",
  "name": "string",
  "description": "string",
  "resourceCount": 5,
  "status": "building | ready | error",
  "createdAt": "ISO8601时间"
}
```

### 6.2 获取知识库构建状态
```http
GET /api/resources/knowledge-base/{knowledgeBaseId}/status
```

**前端调用**: `apiService.getKnowledgeBaseBuildStatus(knowledgeBaseId)`

**响应**:
```json
{
  "knowledgeBaseId": "string",
  "status": "building | ready | error",
  "progress": 75,
  "message": "string",
  "completedAt": "ISO8601时间"
}
```

### 6.3 获取知识库列表
```http
GET /api/resources/knowledge-base?page={page}&size={size}
```

**前端调用**: `apiService.getKnowledgeBases(page, size)`

**响应**:
```json
{
  "knowledgeBases": [
    {
      "knowledgeBaseId": "string",
      "name": "string",
      "description": "string",
      "resourceCount": 10,
      "status": "ready",
      "createdAt": "ISO8601时间",
      "updatedAt": "ISO8601时间"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 5,
    "totalPages": 1
  }
}
```

### 6.4 RAG问答
```http
POST /api/resources/qa
```

**前端调用**: `apiService.ragQuery(knowledgeBaseId, query, topK)`

**请求体**:
```json
{
  "knowledgeBaseId": "string (必填)",
  "query": "string (必填)",
  "topK": 5
}
```

**响应**:
```json
{
  "answer": "string",
  "sources": [
    {
      "resourceId": "string",
      "fileName": "string",
      "relevantContent": "string",
      "similarity": 0.9
    }
  ],
  "query": "string",
  "knowledgeBaseId": "string",
  "usage": {
    "promptTokens": 200,
    "completionTokens": 300,
    "totalTokens": 500
  },
  "responseTime": "1200ms"
}
```

### 6.5 流式RAG问答
```http
POST /api/resources/qa/stream
```

**前端调用**: `apiService.ragStreamQuery(knowledgeBaseId, query, topK)`

**请求体**: 同上

**响应**: Server-Sent Events流

---

## ⏳ 七、任务管理模块 (`/api/tasks`)

### 7.1 获取任务状态
```http
GET /api/tasks/{taskId}/status
```

**前端调用**: `apiService.getTaskStatus(taskId)`

**响应**:
```json
{
  "taskId": "string",
  "status": "pending | processing | completed | failed",
  "progress": 75,
  "result": {
    "downloadUrl": "string (完成时)",
    "content": "string",
    "metadata": {}
  },
  "error": "string (失败时)",
  "createdAt": "ISO8601时间",
  "completedAt": "ISO8601时间"
}
```

---

## 🔧 八、测试接口

### 8.1 Hello测试
```http
GET /api/auth/hello
```

**前端调用**: `apiService.testHello()`

**响应**:
```json
{
  "message": "Hello from Smart Teaching Platform",
  "timestamp": "ISO8601时间",
  "user": {
    "userId": "string",
    "username": "string",
    "email": "string"
  }
}
```

---

## 🚨 九、错误处理规范

### 9.1 HTTP状态码
- `200 OK`: 请求成功
- `201 Created`: 资源创建成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 认证失败
- `403 Forbidden`: 权限不足
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器错误

### 9.2 错误响应格式
```json
{
  "error": "string (错误类型)",
  "message": "string (错误描述)",
  "details": "string (详细信息)",
  "timestamp": "ISO8601时间",
  "path": "string (请求路径)"
}
```

---

## 📊 十、实现优先级

### 🔴 高优先级（必须实现）
1. **用户认证模块** - 登录、注册、用户信息获取
2. **基础AI对话** - 简单对话、图片分析
3. **资源管理** - 文档上传、资源列表、下载
4. **AI生成** - PPT生成、Quiz生成、讲解生成
5. **任务管理** - 任务状态查询

### 🟡 中优先级（建议实现）
1. **教学AI对话** - 教学建议、内容分析、写作辅助
2. **对话管理** - 对话列表、对话详情、标题更新
3. **知识库管理** - 知识库创建、RAG问答

### 🟢 低优先级（可选实现）
1. **流式接口** - 流式对话、流式RAG
2. **统计功能** - 对话统计、资源统计
3. **批量操作** - 批量上传、批量删除

---

**总结**: 本文档基于前端实际API调用分析，提供了完整准确的接口规范。后端按照此文档实现，即可保证前后端完美对接，所有功能正常运行。

*文档版本: v1.0*  
*更新时间: 2024-01-15*  
*基于: 前端 src/services/api.ts 实际调用分析*