# 智能教学平台API接口文档

## 接口规范

### 基础信息
- **接口协议：** HTTP/HTTPS
- **数据格式：** JSON
- **字符编码：** UTF-8
- **接口版本：** v1.0
- **基础URL：** `http://localhost:8080`

### 认证机制
- **认证方式：** JWT (JSON Web Token)
- **Token位置：** HTTP Header
- **Token格式：** `Authorization: Bearer <token>`
- **Token有效期：** 24小时
- **刷新机制：** Token过期后需重新登录

### 请求规范
- **Content-Type：** `application/json`
- **请求方法：** GET, POST, PUT, DELETE
- **参数传递：**
  - GET请求：Query参数
  - POST/PUT请求：Request Body (JSON格式)
  - 文件上传：multipart/form-data

### 响应规范
- **响应格式：** 标准HTTP响应
- **成功响应：** HTTP 2xx状态码 + 数据
- **错误响应：** HTTP 4xx/5xx状态码 + 错误信息
- **时间格式：** ISO 8601 (yyyy-MM-ddTHH:mm:ss)

## 1. 用户管理模块

### 1.1 用户注册

**接口描述：** 教师用户注册账号

**请求信息：**
- **URL：** `POST /api/auth/register`
- **Content-Type：** `application/json`
- **认证：** 无需认证

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名，2-20个字符 | "张老师" |
| email | String | 是 | 邮箱地址，需符合邮箱格式 | "teacher@example.com" |
| password | String | 是 | 密码，6-20个字符 | "123456" |

**请求示例：**
```json
{
  "username": "张老师",
  "email": "teacher@example.com",
  "password": "123456"
}
```

**响应参数：**
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Integer | 用户ID | 1 |
| username | String | 用户名 | "张老师" |
| email | String | 邮箱地址 | "teacher@example.com" |
| createdAt | String | 创建时间 | "2024-01-01T10:00:00" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "username": "张老师",
  "email": "teacher@example.com",
  "createdAt": "2024-01-01T10:00:00"
}
```

**错误响应：**
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "邮箱已存在",
  "path": "/api/auth/register"
}
```

### 1.2 用户登录

**接口描述：** 教师用户登录获取访问令牌

**请求信息：**
- **URL：** `POST /api/auth/login`
- **Content-Type：** `application/json`
- **认证：** 无需认证

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| email | String | 是 | 邮箱地址 | "teacher@example.com" |
| password | String | 是 | 密码 | "123456" |

**请求示例：**
```json
{
  "email": "teacher@example.com",
  "password": "123456"
}
```

**响应参数：**
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| token | String | JWT访问令牌 | "eyJhbGciOiJIUzI1NiIs..." |
| user | Object | 用户信息对象 | - |
| user.id | Integer | 用户ID | 1 |
| user.username | String | 用户名 | "张老师" |
| user.email | String | 邮箱地址 | "teacher@example.com" |
| user.avatar | String | 头像URL | "http://example.com/avatar.jpg" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "张老师",
    "email": "teacher@example.com",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

**错误响应：**
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "邮箱或密码错误",
  "path": "/api/auth/login"
}
```

### 1.3 用户登出

**接口描述：** 用户登出，将token加入黑名单

**请求信息：**
- **URL：** `POST /api/auth/logout`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求头：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json
```

**错误响应：**
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token无效或已过期",
  "path": "/api/auth/logout"
}
```

### 1.4 获取当前用户信息

**接口描述：** 获取当前登录用户的详细信息

**请求信息：**
- **URL：** `GET /api/auth/me`
- **认证：** 需要JWT Token

**请求头：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应参数：**
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Integer | 用户ID | 1 |
| username | String | 用户名 | "张老师" |
| email | String | 邮箱地址 | "teacher@example.com" |
| avatar | String | 头像URL | "http://example.com/avatar.jpg" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "username": "张老师",
  "email": "teacher@example.com",
  "avatar": "http://example.com/avatar.jpg"
}
```

### 1.5 测试接口

**接口描述：** 验证JWT拦截器是否工作正常的测试接口

**请求信息：**
- **URL：** `GET /api/auth/hello`
- **认证：** 需要JWT Token

**请求头：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain

Hello 张老师!
```

## 2. 智能对话模块

### 2.1 简单对话

**接口描述：** 与AI进行简单的文本对话，支持教学知识点咨询

**请求信息：**
- **URL：** `GET /simple/chat`
- **认证：** 无需认证（建议后续版本添加认证）

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 默认值 | 示例 |
|--------|------|------|------|--------|------|
| query | String | 否 | 对话内容/问题 | "你好,能简单介绍一下自己吗" | "什么是光合作用" |
| chat-id | String | 否 | 对话会话ID，用于保持上下文 | "1" | "123" |

**请求示例：**
```
GET /simple/chat?query=什么是光合作用&chat-id=123
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=UTF-8

光合作用是植物利用阳光、二氧化碳和水制造有机物的过程。在叶绿体中，叶绿素吸收光能，将二氧化碳和水转化为葡萄糖，同时释放氧气。这个过程可以用化学方程式表示为：6CO₂ + 6H₂O + 光能 → C₆H₁₂O₆ + 6O₂
```

### 2.2 流式对话

**接口描述：** 获取流式AI对话响应，实时返回生成内容

**请求信息：**
- **URL：** `GET /stream/chat`
- **认证：** 无需认证

**响应格式：** Server-Sent Events (SSE)

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=UTF-8
Transfer-Encoding: chunked

data: 您好！
data: 我是智能教学助手
data: ，专门为教师提供
data: 教学支持和知识咨询服务
data: 。我可以帮助您...
```

### 2.3 图片分析（URL方式）

**接口描述：** 通过图片URL进行多模态图像分析，适用于教学图片解析

**请求信息：**
- **URL：** `POST /image/analyze/url`
- **Content-Type：** `application/x-www-form-urlencoded`
- **认证：** 无需认证

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 默认值 | 示例 |
|--------|------|------|------|--------|------|
| prompt | String | 否 | 分析提示词 | "请分析这张图片的内容" | "分析这张生物图片中的细胞结构" |
| imageUrl | String | 是 | 图片的URL地址 | - | "https://example.com/image.jpg" |

**请求示例：**
```
POST /image/analyze/url
Content-Type: application/x-www-form-urlencoded

prompt=分析这张生物图片中的细胞结构&imageUrl=https://example.com/cell.jpg
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=UTF-8

这张图片显示了植物叶片的横截面结构，可以清楚地看到：
1. 上表皮：位于叶片上方，细胞排列紧密
2. 栅栏组织：含有大量叶绿体，是光合作用的主要场所
3. 海绵组织：细胞间隙较大，便于气体交换
4. 下表皮：含有气孔，调节气体进出
5. 维管束：包含木质部和韧皮部，负责物质运输
```

**错误响应：**
```
HTTP/1.1 400 Bad Request
Content-Type: text/plain; charset=UTF-8

图片分析失败: 无法访问指定的图片URL
```

### 2.4 图片分析（文件上传）

**接口描述：** 通过文件上传进行图像分析，支持教学素材图片解析

**请求信息：**
- **URL：** `POST /image/analyze/upload`
- **Content-Type：** `multipart/form-data`
- **认证：** 无需认证

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 默认值 | 限制 |
|--------|------|------|------|--------|------|
| prompt | String | 否 | 分析提示词 | "请分析这张图片的内容" | 最大500字符 |
| file | File | 是 | 图片文件 | - | 支持jpg/png/gif，最大10MB |

**请求示例：**
```
POST /image/analyze/upload
Content-Type: multipart/form-data

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="prompt"

分析这张数学几何图形
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="geometry.png"
Content-Type: image/png

[二进制图片数据]
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=UTF-8

这张图片显示了数学几何图形，包含以下元素：
1. 一个等边三角形ABC，边长为5cm
2. 三角形内接一个圆，圆心为O
3. 图中标注了角度：∠BAC = 60°
4. 这是一个关于三角形内切圆的几何题目
5. 可以用来讲解三角形的性质和圆的相关知识点
```

**错误响应：**
```
HTTP/1.1 400 Bad Request
Content-Type: text/plain; charset=UTF-8

请上传图片文件
```

## 3. 多模态素材上传模块（待实现）

### 3.1 文档上传

**接口描述：** 上传教学文档素材（Word、PDF格式）

**请求信息：**
- **URL：** `POST /api/materials/upload/document`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 文档文件 | 支持doc/docx/pdf，最大50MB |
| subject | String | 是 | 学科分类 | 如：数学、语文、英语等 |
| grade | String | 是 | 年级 | 如：小学、初中、高中 |
| title | String | 否 | 文档标题 | 最大100字符 |
| description | String | 否 | 文档描述 | 最大500字符 |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "doc_123456",
  "filename": "光合作用教案.docx",
  "originalName": "photosynthesis_lesson.docx",
  "subject": "生物",
  "grade": "高中",
  "size": 2048000,
  "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "https://example.com/files/doc_123456.docx"
}
```

### 3.2 图片素材上传

**接口描述：** 上传教学图片素材

**请求信息：**
- **URL：** `POST /api/materials/upload/image`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 图片文件 | 支持jpg/png/gif，最大10MB |
| subject | String | 否 | 学科分类 | 如：数学、物理、化学等 |
| description | String | 否 | 图片描述 | 最大200字符 |
| tags | String | 否 | 标签，逗号分隔 | 如：细胞,显微镜,生物 |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "img_123456",
  "filename": "cell_structure.jpg",
  "originalName": "细胞结构图.jpg",
  "subject": "生物",
  "description": "植物细胞结构示意图",
  "tags": ["细胞", "植物", "结构"],
  "size": 1024000,
  "width": 1920,
  "height": 1080,
  "uploadedAt": "2024-01-01T10:00:00",
  "thumbnailUrl": "https://example.com/thumbnails/img_123456_thumb.jpg",
  "downloadUrl": "https://example.com/images/img_123456.jpg"
}
```

### 3.3 语音素材上传

**接口描述：** 上传语音素材并可选择转换为文字

**请求信息：**
- **URL：** `POST /api/materials/upload/audio`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 语音文件 | 支持mp3/wav/m4a，最大100MB |
| needTranscription | Boolean | 否 | 是否需要语音转文字 | 默认false |
| subject | String | 否 | 学科分类 | - |
| description | String | 否 | 语音描述 | 最大200字符 |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "audio_123456",
  "filename": "lesson_recording.mp3",
  "originalName": "数学课录音.mp3",
  "subject": "数学",
  "description": "三角函数课程录音",
  "duration": 1800,
  "size": 15360000,
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "https://example.com/audio/audio_123456.mp3",
  "transcription": "今天我们来学习三角函数的基本概念..."
}
```

## 4. AI资源自动制作模块（待实现）

### 4.1 生成PPT课件

**接口描述：** 基于主题自动生成PPT课件

**请求信息：**
- **URL：** `POST /api/ai/generate/ppt`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 课件主题 | "光合作用" |
| subject | String | 是 | 学科 | "生物" |
| grade | String | 是 | 年级 | "高中" |
| slideCount | Integer | 否 | 幻灯片数量 | 20 |
| style | String | 否 | 课件风格 | "professional/creative/simple" |
| includeImages | Boolean | 否 | 是否包含图片 | true |

**请求示例：**
```json
{
  "topic": "光合作用",
  "subject": "生物",
  "grade": "高中",
  "slideCount": 20,
  "style": "professional",
  "includeImages": true
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "ppt_task_123456",
  "message": "PPT生成任务已启动",
  "estimatedTime": 300,
  "status": "processing"
}
```

### 4.2 生成习题

**接口描述：** 基于知识点自动生成习题

**请求信息：**
- **URL：** `POST /api/ai/generate/quiz`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 知识点主题 | "三角函数" |
| subject | String | 是 | 学科 | "数学" |
| difficulty | String | 否 | 难度等级 | "easy/medium/hard" |
| questionCount | Integer | 否 | 题目数量 | 10 |
| questionTypes | Array | 否 | 题型 | ["choice", "fill", "essay"] |

**请求示例：**
```json
{
  "topic": "三角函数",
  "subject": "数学",
  "difficulty": "medium",
  "questionCount": 10,
  "questionTypes": ["choice", "fill", "essay"]
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "quiz_task_123456",
  "message": "习题生成任务已启动",
  "estimatedTime": 120,
  "status": "processing"
}
```

### 4.3 生成讲解文本

**接口描述：** 基于知识点生成详细的讲解文本

**请求信息：**
- **URL：** `POST /api/ai/generate/explanation`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 知识点主题 | "牛顿第一定律" |
| subject | String | 是 | 学科 | "物理" |
| grade | String | 是 | 年级 | "高中" |
| style | String | 否 | 讲解风格 | "detailed/simple/interactive" |
| length | String | 否 | 文本长度 | "short/medium/long" |

**请求示例：**
```json
{
  "topic": "牛顿第一定律",
  "subject": "物理",
  "grade": "高中",
  "style": "detailed",
  "length": "medium"
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "explanation_task_123456",
  "message": "讲解文本生成任务已启动",
  "estimatedTime": 60,
  "status": "processing"
}
```

## 5. 教学资源管理模块（待实现）

### 5.1 获取资源列表

**接口描述：** 获取教学资源列表，支持分页和筛选

**请求信息：**
- **URL：** `GET /api/resources`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码 | 1 |
| limit | Integer | 否 | 每页数量 | 20 |
| subject | String | 否 | 学科筛选 | "数学" |
| grade | String | 否 | 年级筛选 | "高中" |
| keyword | String | 否 | 关键词搜索 | "三角函数" |
| type | String | 否 | 资源类型 | "ppt/quiz/document/image" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "resources": [
    {
      "id": "res_123456",
      "title": "三角函数基础知识PPT",
      "type": "ppt",
      "subject": "数学",
      "grade": "高中",
      "description": "包含正弦、余弦、正切函数的基本概念",
      "tags": ["三角函数", "数学", "高中"],
      "author": "张老师",
      "downloadCount": 156,
      "rating": 4.8,
      "fileSize": 2048000,
      "thumbnailUrl": "https://example.com/thumbnails/res_123456.jpg",
      "downloadUrl": "https://example.com/resources/res_123456.pptx",
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "total": 150,
  "page": 1,
  "limit": 20,
  "totalPages": 8
}
```

### 5.2 获取资源分类

**接口描述：** 获取系统支持的资源分类信息

**请求信息：**
- **URL：** `GET /api/resources/categories`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "subjects": [
    {"code": "math", "name": "数学"},
    {"code": "chinese", "name": "语文"},
    {"code": "english", "name": "英语"},
    {"code": "physics", "name": "物理"},
    {"code": "chemistry", "name": "化学"},
    {"code": "biology", "name": "生物"}
  ],
  "grades": [
    {"code": "primary", "name": "小学"},
    {"code": "middle", "name": "初中"},
    {"code": "high", "name": "高中"}
  ],
  "types": [
    {"code": "ppt", "name": "PPT课件"},
    {"code": "quiz", "name": "习题"},
    {"code": "document", "name": "文档"},
    {"code": "image", "name": "图片"},
    {"code": "audio", "name": "音频"}
  ]
}
```

### 5.3 资源预览

**接口描述：** 获取资源的预览信息

**请求信息：**
- **URL：** `GET /api/resources/{id}/preview`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 资源ID |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "res_123456",
  "title": "三角函数基础知识PPT",
  "type": "ppt",
  "subject": "数学",
  "grade": "高中",
  "description": "包含正弦、余弦、正切函数的基本概念",
  "content": "第一章：三角函数的定义\n1.1 角的概念\n1.2 弧度制...",
  "slideCount": 25,
  "previewImages": [
    "https://example.com/previews/res_123456_slide1.jpg",
    "https://example.com/previews/res_123456_slide2.jpg"
  ],
  "downloadUrl": "https://example.com/resources/res_123456.pptx"
}
```

## 6. RAG个性化知识库模块（待实现）

### 6.1 构建知识库

**接口描述：** 基于上传的教材教案自动构建RAG知识库

**请求信息：**
- **URL：** `POST /api/knowledge/build`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| materials | Array | 是 | 素材文件ID列表 | ["doc_123", "doc_456"] |
| subject | String | 是 | 学科 | "数学" |
| grade | String | 是 | 年级 | "高中" |
| name | String | 是 | 知识库名称 | "高中数学知识库" |
| description | String | 否 | 知识库描述 | "包含高中数学全部知识点" |

**请求示例：**
```json
{
  "materials": ["doc_123456", "doc_789012"],
  "subject": "数学",
  "grade": "高中",
  "name": "高中数学知识库",
  "description": "包含三角函数、导数、积分等知识点"
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "kb_build_123456",
  "knowledgeBaseId": "kb_123456",
  "message": "知识库构建任务已启动",
  "estimatedTime": 600,
  "status": "processing"
}
```

### 6.2 知识库查询

**接口描述：** 在RAG知识库中搜索相关知识点

**请求信息：**
- **URL：** `GET /api/knowledge/search`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 查询内容 | "三角函数的性质" |
| knowledgeBaseId | String | 否 | 知识库ID | "kb_123456" |
| subject | String | 否 | 学科范围 | "数学" |
| limit | Integer | 否 | 返回结果数量 | 10 |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "results": [
    {
      "id": "chunk_123456",
      "content": "三角函数具有周期性、奇偶性、单调性等重要性质...",
      "source": "高中数学教材第三章",
      "relevanceScore": 0.95,
      "knowledgePoint": "三角函数性质",
      "pageNumber": 45
    }
  ],
  "total": 5,
  "query": "三角函数的性质",
  "searchTime": 0.15
}
```

### 6.3 任务状态查询

**接口描述：** 查询异步任务（生成、构建等）的执行状态

**请求信息：**
- **URL：** `GET /api/tasks/{taskId}/status`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | String | 是 | 任务ID |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "taskId": "ppt_task_123456",
  "type": "ppt_generation",
  "status": "completed",
  "progress": 100,
  "currentStep": "任务已完成",
  "result": {
    "resourceId": "res_789012",
    "downloadUrl": "https://example.com/generated/ppt_123456.pptx",
    "previewUrl": "https://example.com/preview/ppt_123456"
  },
  "startedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-01T10:05:00",
  "estimatedTime": 300,
  "actualTime": 300
}
```

## HTTP状态码说明

| 状态码 | 英文描述 | 中文说明 | 使用场景 |
|--------|----------|----------|----------|
| 200 | OK | 请求成功 | 正常返回数据 |
| 201 | Created | 资源创建成功 | 注册、上传文件成功 |
| 202 | Accepted | 请求已接受 | 异步任务已启动 |
| 400 | Bad Request | 请求参数错误 | 参数缺失、格式错误 |
| 401 | Unauthorized | 未授权 | Token无效、过期或缺失 |
| 403 | Forbidden | 禁止访问 | 权限不足 |
| 404 | Not Found | 资源不存在 | 请求的资源不存在 |
| 409 | Conflict | 资源冲突 | 邮箱已存在、重复操作 |
| 413 | Payload Too Large | 请求体过大 | 文件上传超过限制 |
| 422 | Unprocessable Entity | 请求格式正确但语义错误 | 业务逻辑错误 |
| 429 | Too Many Requests | 请求过于频繁 | 触发限流 |
| 500 | Internal Server Error | 服务器内部错误 | 系统异常 |
| 502 | Bad Gateway | 网关错误 | 上游服务异常 |
| 503 | Service Unavailable | 服务不可用 | 系统维护、过载 |

## 错误响应格式

所有错误响应都遵循统一格式：

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "具体错误描述信息",
  "path": "/api/auth/login",
  "details": {
    "field": "email",
    "code": "INVALID_FORMAT",
    "description": "邮箱格式不正确"
  }
}
```

## 接口限制和注意事项

### 文件上传限制
- **图片文件：** 最大10MB，支持jpg/png/gif格式
- **文档文件：** 最大50MB，支持pdf/doc/docx格式
- **音频文件：** 最大100MB，支持mp3/wav/m4a格式
- **单次上传：** 最多5个文件

### 请求频率限制
- **普通接口：** 每分钟100次请求
- **AI生成接口：** 每分钟10次请求
- **文件上传：** 每分钟20次请求

### 数据格式要求
- **时间格式：** ISO 8601标准 (yyyy-MM-ddTHH:mm:ss)
- **字符编码：** UTF-8
- **JSON格式：** 严格遵循JSON规范
- **文件名：** 支持中英文，不超过255字符

### Token管理
- **有效期：** 24小时
- **刷新机制：** Token过期后需重新登录
- **安全性：** 请妥善保管Token，避免泄露
- **黑名单：** 登出后Token立即失效

### 异步任务
- **超时时间：** 最长30分钟
- **状态查询：** 建议每5秒查询一次状态
- **结果保存：** 生成结果保存7天
- **并发限制：** 每用户最多3个并发任务
