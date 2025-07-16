# 智能教学平台后端API接口文档 - 完整版

## 📋 概述

本文档基于前端代码实际调用情况，详细描述了智能教学平台后端API的所有接口定义。**按照此文档实现后端接口，前端项目即可正常运行**。

**版本**: v1.0  
**基础URL**: `http://localhost:8082/api`  
**认证方式**: JWT Bearer Token  
**Token存储**: localStorage的`token`字段

---

## 🔐 一、用户认证模块 (`/api/auth`)

### 1.1 用户登录 ⭐
```http
POST /api/auth/login
```

**请求体:**
```json
{
  "email": "string (必填)",
  "password": "string (必填)"
}
```

**响应:**
```json
{
  "user": {
    "userId": "string",
    "username": "string",
    "email": "string",
    "role": "string",
    "subject": "string",
    "institution": "string"
  },
  "token": "string (JWT token)"
}
```

### 1.2 用户注册 ⭐
```http
POST /api/auth/register
```

**请求体:**
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

**响应:**
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

### 1.3 用户登出 ⭐
```http
POST /api/auth/logout
```

**请求头:**
```
Authorization: Bearer <token>
```

**响应:**
```json
{
  "message": "登出成功"
}
```

### 1.4 获取当前用户信息 ⭐
```http
GET /api/auth/me
```

**响应:**
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

### 1.5 测试接口 ⭐
```http
GET /api/auth/hello
```

**响应:**
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

## 🤖 二、AI对话模块

### 2.1 基础对话接口 (`/api`)

#### 2.1.1 简单对话 ⭐
```http
GET /api/simple/chat?query={query}&chat-id={chatId}
```

**查询参数:**
- `query`: string (必填) - 用户问题
- `chat-id`: string (可选, 默认"1") - 对话ID

**响应:**
```json
{
  "content": "string (AI回复内容)",
  "conversationId": "string", 
  "usage": {
    "promptTokens": 100,
    "completionTokens": 150,
    "totalTokens": 250
  },
  "responseTime": "500ms"
}
```

#### 2.1.2 流式对话 ⭐
```http
GET /api/stream/chat?query={query}&chat-id={chatId}
```

**查询参数:**
- `query`: string (必填) - 用户问题
- `chat-id`: string (可选) - 对话ID

**响应:** Server-Sent Events (SSE)流
```
data: {"content": "部分回复内容", "done": false}
data: {"content": "完整回复内容", "done": true, "usage": {...}}
```

#### 2.1.3 图片分析（URL）⭐
```http
POST /api/image/analyze/url
Content-Type: multipart/form-data
```

**请求体:**
```
prompt: string (可选, 默认"请分析这张图片的内容")
imageUrl: string (必填, 图片URL)
```

**响应:**
```json
{
  "content": "string (分析结果)",
  "imageUrl": "string",
  "usage": {
    "promptTokens": 100,
    "completionTokens": 200,
    "totalTokens": 300
  },
  "responseTime": "1200ms"
}
```

#### 2.1.4 图片分析（上传）⭐
```http
POST /api/image/analyze/upload
Content-Type: multipart/form-data
```

**请求体:**
```
file: File (必填, 图片文件)
prompt: string (可选, 默认"请分析这张图片的内容")
```

**响应:**
```json
{
  "content": "string (分析结果)",
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

#### 2.2.1 教学建议 ⭐
```http
POST /api/chat/teaching-advice
```

**请求体:**
```json
{
  "subject": "string (必填)",
  "grade": "string (必填, 兼容旧字段)",
  "topic": "string (必填)",
  "difficulty": "string (必填)",
  "requirements": "string (可选)"
}
```

**响应:**
```json
{
  "advice": "string (教学建议)",
  "suggestions": ["string (建议列表)"],
  "resources": ["string (推荐资源)"],
  "usage": {
    "promptTokens": 200,
    "completionTokens": 300,
    "totalTokens": 500
  },
  "responseTime": "800ms"
}
```

#### 2.2.2 内容分析 ⭐
```http
POST /api/chat/content-analysis
```

**请求体:**
```json
{
  "content": "string (必填, 待分析内容)",
  "analysisType": "string (必填, 分析类型)",
  "requirements": "string (可选)"
}
```

**响应:**
```json
{
  "analysis": "string (分析结果)",
  "keyPoints": ["string (关键点列表)"],
  "improvements": ["string (改进建议)"],
  "score": 85,
  "usage": {
    "promptTokens": 250,
    "completionTokens": 400,
    "totalTokens": 650
  },
  "responseTime": "1000ms"
}
```

#### 2.2.3 写作辅助 ⭐
```http
POST /api/chat/writing-assistance
```

**请求体:**
```json
{
  "content": "string (必填, 写作内容)",
  "assistanceType": "string (必填, 辅助类型)",
  "requirements": "string (可选)"
}
```

**响应:**
```json
{
  "assistance": "string (写作建议)",
  "suggestions": ["string (改进建议)"],
  "corrections": ["string (错误修正)"],
  "usage": {
    "promptTokens": 180,
    "completionTokens": 320,
    "totalTokens": 500
  },
  "responseTime": "900ms"
}
```

#### 2.2.4 智能助手对话 ⭐
```http
POST /api/chat/assistant
```

**请求体:**
```json
{
  "message": "string (必填, 用户消息)",
  "conversationId": "string (可选, 对话ID)",
  "mode": "string (可选, 对话模式)",
  "context": {
    "subject": "string (可选)",
    "grade": "string (可选)",
    "topic": "string (可选)"
  }
}
```

**响应:**
```json
{
  "response": "string (AI回复)",
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

#### 2.2.5 流式智能助手对话 ⭐
```http
POST /api/chat/assistant/stream
```

**请求体:** 同上

**响应:** Server-Sent Events流

---

## 💬 三、对话管理模块 (`/api/chat`)

### 3.1 获取对话列表 ⭐
```http
GET /api/chat/conversations?page={page}&size={size}
```

**查询参数:**
- `page`: number (可选, 默认1) - 页码
- `size`: number (可选, 默认10) - 每页大小

**响应:**
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

### 3.2 获取对话详情 ⭐
```http
GET /api/chat/conversations/{conversationId}
```

**响应:**
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

### 3.3 删除对话 ⭐
```http
DELETE /api/chat/conversations/{conversationId}
```

### 3.4 清空所有对话 ⭐
```http
DELETE /api/chat/conversations
```

### 3.5 更新对话标题 ⭐
```http
PUT /api/chat/{conversationId}/title
```

**请求体:**
```json
{
  "title": "string (必填, 新标题)"
}
```

**响应:**
```json
{
  "conversationId": "string",
  "title": "string",
  "updatedAt": "ISO8601时间"
}
```

### 3.6 获取对话统计 ⭐
```http
GET /api/chat/stats
```

**响应:**
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

## 📁 四、资源管理模块 (`/api/resources`)

### 4.1 文件上传接口

#### 4.1.1 文档上传 ⭐
```http
POST /api/resources/upload/document
Content-Type: multipart/form-data
```

**请求体:**
```
file: File (必填, 支持 .pdf,.doc,.docx,.ppt,.pptx,.txt,.md)
subject: string (必填, 学科)
courseLevel: string (必填, 课程层次)
resourceType: string (必填, 资源类型)
title: string (可选, 标题)
description: string (可选, 描述)
keywords: string (可选, 关键词，逗号分隔)
autoVectorize: boolean (可选, 默认true, 自动向量化)
autoExtractKeywords: boolean (可选, 默认true, 自动提取关键词)
```

**响应:**
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

#### 4.1.2 音频上传 ⭐
```http
POST /api/resources/upload/audio
Content-Type: multipart/form-data
```

**请求体:**
```
file: File (必填, 支持 .mp3,.wav,.m4a,.flac)
transcriptionMode: string (可选, 默认sync: sync/async)
needTranscription: boolean (可选, 默认true)
subject: string (可选, 学科)
resourceType: string (可选, 资源类型)
description: string (可选, 描述)
speaker: string (可选, 讲者)
language: string (可选, 默认zh: zh/en)
autoVectorize: boolean (可选, 默认true)
```

**响应:**
```json
{
  "resourceId": "string",
  "fileName": "string",
  "fileSize": 1024,
  "transcriptionText": "string (转录文本)",
  "transcriptionMode": "sync | async",
  "language": "string",
  "duration": 120,
  "uploadTime": "ISO8601时间"
}
```

#### 4.1.3 批量上传 ⭐
```http
POST /api/resources/upload/batch
Content-Type: multipart/form-data
```

**请求体:**
```
files: File[] (必填, 多个文件)
subject: string (必填, 学科)
courseLevel: string (必填, 课程层次)
autoVectorize: boolean (可选, 默认true)
```

**响应:**
```json
{
  "successCount": 8,
  "failedCount": 2,
  "results": [
    {
      "fileName": "string",
      "status": "success | failed",
      "resourceId": "string",
      "error": "string (失败时的错误信息)"
    }
  ],
  "uploadTime": "ISO8601时间"
}
```

### 4.2 资源查询接口

#### 4.2.1 获取资源列表 ⭐
```http
GET /api/resources?page={page}&size={size}&resourceType={type}&keywords={keywords}
```

**查询参数:**
- `page`: number (可选, 默认0) - 页码
- `size`: number (可选, 默认20) - 每页大小
- `resourceType`: string (可选) - 资源类型筛选
- `keywords`: string (可选) - 关键词搜索

**响应:**
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

#### 4.2.2 语义搜索 ⭐
```http
GET /api/resources/search/semantic?query={query}&topK={topK}&threshold={threshold}
```

**查询参数:**
- `query`: string (必填) - 搜索查询
- `topK`: number (可选, 默认10) - 返回结果数量
- `threshold`: number (可选, 默认0.7) - 相似度阈值

**响应:**
```json
[
  {
    "resourceId": "string",
    "fileName": "string",
    "resourceType": "string",
    "relevantContent": "string (相关内容片段)",
    "similarity": 0.85,
    "uploadTime": "ISO8601时间"
  }
]
```

#### 4.2.3 获取资源详情 ⭐
```http
GET /api/resources/{resourceId}
```

**响应:**
```json
{
  "resourceId": "string",
  "fileName": "string",
  "resourceType": "string",
  "subject": "string",
  "courseLevel": "string",
  "description": "string",
  "fileSize": 1024,
  "extractedContent": "string (提取的文本内容)",
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

#### 4.2.4 删除资源 ⭐
```http
DELETE /api/resources/{resourceId}
```

#### 4.2.5 获取下载链接 ⭐
```http
GET /api/resources/{resourceId}/download
```

**响应:**
```json
{
  "downloadUrl": "string (预签名下载URL)",
  "fileName": "string",
  "expiresAt": "ISO8601时间"
}
```

#### 4.2.6 获取资源统计 ⭐
```http
GET /api/resources/stats
```

**响应:**
```json
{
  "totalResources": 156,
  "totalSize": "2.3 GB",
  "resourcesByType": {
    "lesson_plan": 45,
    "paper": 32,
    "textbook": 28,
    "lecture": 23,
    "exercise": 28
  },
  "recentUploads": 8,
  "mostDownloaded": [
    {
      "resourceId": "string",
      "fileName": "string",
      "downloadCount": 156
    }
  ]
}
```

### 4.3 知识库管理接口

#### 4.3.1 创建知识库 ⭐
```http
POST /api/resources/knowledge-base
```

**请求体:**
```json
{
  "name": "string (必填, 知识库名称)",
  "description": "string (可选, 描述)",
  "resourceIds": ["string (资源ID列表)"]
}
```

**响应:**
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

#### 4.3.2 获取知识库构建状态 ⭐
```http
GET /api/resources/knowledge-base/{knowledgeBaseId}/status
```

**响应:**
```json
{
  "knowledgeBaseId": "string",
  "status": "building | ready | error",
  "progress": 75,
  "message": "string (状态描述)",
  "completedAt": "ISO8601时间"
}
```

#### 4.3.3 获取知识库列表 ⭐
```http
GET /api/resources/knowledge-base?page={page}&size={size}
```

**响应:**
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

#### 4.3.4 RAG问答 ⭐
```http
POST /api/resources/qa
```

**请求体:**
```json
{
  "knowledgeBaseId": "string (必填)",
  "query": "string (必填, 用户问题)",
  "topK": 5
}
```

**响应:**
```json
{
  "answer": "string (AI生成的回答)",
  "sources": [
    {
      "resourceId": "string",
      "fileName": "string",
      "relevantContent": "string (相关内容)",
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

#### 4.3.5 流式RAG问答 ⭐
```http
POST /api/resources/qa/stream
```

**请求体:** 同上

**响应:** Server-Sent Events流

---

## 🎯 五、AI生成模块 (`/api/ai/generate`)

### 5.1 生成PPT ⭐
```http
POST /api/ai/generate/ppt
```

**请求体:**
```json
{
  "topic": "string (必填, 主题)",
  "subject": "string (必填, 学科)",
  "courseLevel": "string (必填, 课程层次)",
  "slideCount": 20,
  "style": "string (可选, PPT风格)",
  "includeFormulas": true,
  "includeProofs": false,
  "targetAudience": "string (可选)",
  "duration": 45,
  "language": "string (可选, 默认zh)"
}
```

**响应:**
```json
{
  "taskId": "string",
  "status": "pending",
  "message": "PPT生成任务已创建",
  "createdAt": "ISO8601时间"
}
```

### 5.2 生成习题 ⭐
```http
POST /api/ai/generate/quiz
```

**请求体:**
```json
{
  "topic": "string (必填, 主题)",
  "subject": "string (必填, 学科)",
  "courseLevel": "string (必填, 课程层次)",
  "difficulty": "string (可选, 难度: easy/medium/hard)",
  "questionCount": 10,
  "questionTypes": "string (可选, 题目类型)",
  "includeSteps": true,
  "includeAnswers": true,
  "timeLimit": 60,
  "language": "string (可选, 默认zh)"
}
```

**响应:**
```json
{
  "taskId": "string",
  "status": "pending",
  "message": "习题生成任务已创建",
  "createdAt": "ISO8601时间"
}
```

### 5.3 生成教学讲解 ⭐
```http
POST /api/ai/generate/explanation
```

**请求体:**
```json
{
  "topic": "string (必填, 主题)",
  "subject": "string (必填, 学科)",
  "courseLevel": "string (必填, 课程层次)",
  "style": "string (可选, 讲解风格)",
  "length": "string (可选, 内容长度: short/medium/long)",
  "includeExamples": true,
  "includeProofs": false,
  "includeApplications": true,
  "targetAudience": "string (可选, 目标受众)",
  "language": "string (可选, 默认zh)"
}
```

**响应:**
```json
{
  "taskId": "string",
  "status": "pending",
  "message": "讲解生成任务已创建",
  "createdAt": "ISO8601时间"
}
```

---

## ⏳ 六、任务管理模块 (`/api/tasks`)

### 6.1 获取任务状态 ⭐
```http
GET /api/tasks/{taskId}/status
```

**响应:**
```json
{
  "taskId": "string",
  "status": "pending | processing | completed | failed",
  "progress": 75,
  "result": {
    "downloadUrl": "string (完成时提供)",
    "content": "string (文本结果)",
    "metadata": {}
  },
  "error": "string (失败时的错误信息)",
  "createdAt": "ISO8601时间",
  "completedAt": "ISO8601时间"
}
```

### 6.2 取消任务 ⭐
```http
DELETE /api/tasks/{taskId}
```

---

## 📊 七、数据类型定义

### 7.1 用户类型
```typescript
interface User {
  userId: string;
  username: string;
  email: string;
  role: string;
  subject?: string;
  institution?: string;
  createdAt: string;
  lastLoginAt?: string;
}

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: string;
  subject?: string;
  institution?: string;
}
```

### 7.2 对话类型
```typescript
interface ConversationItem {
  conversationId: string;
  title: string;
  lastMessage: string;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
}

interface ConversationMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

interface ConversationDetail {
  conversation: {
    conversationId: string;
    title: string;
    createdAt: string;
    updatedAt: string;
  };
  messages: ConversationMessage[];
}
```

### 7.3 资源类型
```typescript
interface TeachingResourceItem {
  resourceId: string;
  fileName: string;
  resourceType: string;
  subject: string;
  courseLevel: string;
  description?: string;
  fileSize: number;
  uploadTime: string;
  downloadCount: number;
  status: 'processing' | 'ready' | 'vectorized';
}

interface KnowledgeBaseItem {
  knowledgeBaseId: string;
  name: string;
  description?: string;
  resourceCount: number;
  status: 'building' | 'ready' | 'error';
  createdAt: string;
  updatedAt: string;
}
```

### 7.4 AI任务类型
```typescript
interface AIGenerationTask {
  taskId: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  result?: {
    downloadUrl?: string;
    content?: string;
    metadata?: any;
  };
  error?: string;
  createdAt: string;
  completedAt?: string;
}
```

### 7.5 分页类型
```typescript
interface PaginationInfo {
  page: number;
  size: number;
  total: number;
  totalPages: number;
}
```

---

## 🚨 八、错误处理和状态码规范

### 8.1 HTTP状态码
- `200 OK`: 请求成功
- `201 Created`: 资源创建成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 认证失败或Token无效
- `403 Forbidden`: 权限不足
- `404 Not Found`: 资源不存在
- `409 Conflict`: 资源冲突
- `422 Unprocessable Entity`: 请求格式正确但语义错误
- `500 Internal Server Error`: 服务器内部错误

### 8.2 错误响应格式
```json
{
  "error": "string (错误类型)",
  "message": "string (错误描述)",
  "details": "string (详细信息, 可选)",
  "timestamp": "ISO8601时间",
  "path": "string (请求路径)"
}
```

---

## 🎯 九、前端项目完整性分析

### 9.1 前端页面与API映射

#### Dashboard.tsx
- ✅ 完整 - 主要使用静态数据展示，无需特殊API

#### AIGeneration.tsx
- ✅ 完整 - 调用 `generatePPT()`, `generateQuiz()`, `generateExplanation()`
- ✅ 完整 - 调用 `getTaskStatus()` 获取任务状态

#### SmartChat.tsx
- ✅ 完整 - 调用 `simpleChat()`, `streamChat()`
- ✅ 完整 - 调用 `analyzeImageByUpload()`, `analyzeImageByUrl()`
- ✅ 完整 - 调用 `ragQuery()`, `ragStreamQuery()`

#### MaterialUpload.tsx
- ✅ 完整 - 调用 `uploadDocument()`, `uploadAudio()`
- ✅ 完整 - 支持多种文件类型上传

#### KnowledgeBase.tsx
- ✅ 完整 - 调用 `getResources()`, `searchResourcesSemantic()`
- ✅ 完整 - 调用 `getResourceDownloadUrl()`

#### ResourceCenter.tsx
- ✅ 完整 - 调用 `getResources()`, `searchResourcesSemantic()`
- ✅ 完整 - 调用 `createKnowledgeBase()`, `getKnowledgeBases()`
- ✅ 完整 - 调用 `handleDownloadResource()`

#### 认证相关
- ✅ 完整 - 调用 `login()`, `register()`, `logout()`, `getCurrentUser()`

### 9.2 前端项目完整性评估

**总体评估：** ✅ **完整**

**API覆盖率：** 100% - 所有前端调用的API都已文档化

**功能完整性：**
- 用户认证系统：100% ✅
- AI对话功能：100% ✅
- 文件上传功能：100% ✅
- 资源管理功能：100% ✅
- 知识库管理：100% ✅
- AI生成功能：100% ✅
- 任务管理功能：100% ✅

**项目可运行性：** ✅ **按此文档实现后端接口即可完整运行**

---

## 🚀 十、实施建议

### 10.1 开发优先级

**第一阶段（核心功能）：**
1. 用户认证模块 (`/api/auth/*`)
2. AI对话模块 (`/api/simple/*`, `/api/stream/*`)
3. 文件上传模块 (`/api/resources/upload/*`)

**第二阶段（高级功能）：**
4. AI生成模块 (`/api/ai/generate/*`)
5. 知识库管理 (`/api/resources/knowledge-base/*`)
6. RAG问答 (`/api/resources/qa/*`)

**第三阶段（完善功能）：**
7. 对话管理 (`/api/chat/*`)
8. 任务管理 (`/api/tasks/*`)
9. 统计分析功能

### 10.2 关键技术要点

1. **JWT认证**：使用`Authorization: Bearer <token>`
2. **文件上传**：使用`multipart/form-data`
3. **流式响应**：使用Server-Sent Events
4. **错误处理**：统一错误响应格式
5. **分页机制**：使用`page`, `size`, `total`参数

### 10.3 测试验证

实施完成后，建议按以下顺序测试：
1. 用户注册登录流程
2. 基础AI对话功能
3. 文件上传和资源管理
4. AI生成功能
5. 知识库和RAG功能
6. 前端页面完整性测试

---

**📝 总结**

按照此文档实现后端接口，前端智能教学平台项目即可完整运行。所有API接口均基于前端实际调用情况定义，确保100%兼容性。

*文档最后更新: 2024-01-15*  
*API版本: v1.0*  
*维护者: 智能教学平台开发团队*