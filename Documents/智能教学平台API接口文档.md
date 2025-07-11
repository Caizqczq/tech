# 高等教育智能教学平台API接口文档

## 接口规范

### 基础信息
- **接口协议：** HTTP/HTTPS
- **数据格式：** JSON
- **字符编码：** UTF-8
- **接口版本：** v1.0
- **基础URL：** `http://localhost:8080`
- **适用场景：** 高等教育（大学、研究生教育）

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

**接口描述：** 高校教师用户注册账号

**请求信息：**
- **URL：** `POST /api/auth/register`
- **Content-Type：** `application/json`
- **认证：** 无需认证

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名，2-20个字符 | "张教授" |
| email | String | 是 | 邮箱地址，需符合邮箱格式 | "zhang.prof@university.edu.cn" |
| password | String | 是 | 密码，6-20个字符 | "123456" |

**请求示例：**
```json
{
  "username": "张教授",
  "email": "zhang.prof@university.edu.cn",
  "password": "123456"
}
```

**响应参数：**
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Integer | 用户ID | 1 |
| username | String | 用户名 | "张教授" |
| email | String | 邮箱地址 | "zhang.prof@university.edu.cn" |
| createdAt | String | 创建时间 | "2024-01-01T10:00:00" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "username": "张教授",
  "email": "zhang.prof@university.edu.cn",
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

**接口描述：** 高校教师用户登录获取访问令牌

**请求信息：**
- **URL：** `POST /api/auth/login`
- **Content-Type：** `application/json`
- **认证：** 无需认证

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| email | String | 是 | 邮箱地址 | "zhang.prof@university.edu.cn" |
| password | String | 是 | 密码 | "123456" |

**请求示例：**
```json
{
  "email": "zhang.prof@university.edu.cn",
  "password": "123456"
}
```

**响应参数：**
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| token | String | JWT访问令牌 | "eyJhbGciOiJIUzI1NiIs..." |
| user | Object | 用户信息对象 | - |
| user.id | Integer | 用户ID | 1 |
| user.username | String | 用户名 | "张教授" |
| user.email | String | 邮箱地址 | "zhang.prof@university.edu.cn" |
| user.avatar | String | 头像URL | "http://example.com/avatar.jpg" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "张教授",
    "email": "zhang.prof@university.edu.cn",
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
| username | String | 用户名 | "张教授" |
| email | String | 邮箱地址 | "zhang.prof@university.edu.cn" |
| avatar | String | 头像URL | "http://example.com/avatar.jpg" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "username": "张教授",
  "email": "zhang.prof@university.edu.cn",
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

Hello 张教授!
```

## 2. 智能对话模块

### 2.1 简单对话接口

**接口描述：** 基于Spring AI Alibaba ChatClient的简单对话接口，支持会话记忆功能

**请求信息：**
- **URL：** `GET /simple/chat`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 否 | 用户问题 | "你好,能简单介绍一下自己吗" |
| chat-id | String | 否 | 会话ID，保持对话连续性 | "1" |

**请求示例：**
```
GET /simple/chat?query=请解释微积分中导数的几何意义&chat-id=chat_123456
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8

导数的几何意义是函数图像在某一点处的切线斜率。具体来说：
1. 对于函数f(x)在点x₀处的导数f'(x₀)，表示函数图像在点(x₀, f(x₀))处的切线斜率
2. 这个斜率反映了函数在该点的瞬时变化率
3. 如果导数为正，函数在该点递增；如果导数为负，函数在该点递减
4. 导数的绝对值越大，函数变化越剧烈
```

### 2.2 流式对话接口

**接口描述：** 支持Server-Sent Events的流式对话响应，实时返回生成内容

**请求信息：**
- **URL：** `GET /stream/chat`
- **认证：** 需要JWT Token

**成功响应：** (Server-Sent Events格式)
```
HTTP/1.1 200 OK
Content-Type: text/event-stream; charset=utf-8
Cache-Control: no-cache
Connection: keep-alive

data: 导数的几何意义

data: 是函数图像在某一点处

data: 的切线斜率。具体来说...

[EOF]
```

### 2.3 图片分析接口 - URL方式

**接口描述：** 基于Spring AI Alibaba多模态模型，通过图片URL进行图像内容分析

**请求信息：**
- **URL：** `POST /image/analyze/url`
- **Content-Type：** `application/x-www-form-urlencoded`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| prompt | String | 否 | 分析提示词 | "请分析这张图片的内容" |
| imageUrl | String | 是 | 图片URL地址 | "https://example.com/image.jpg" |

**请求示例：**
```
POST /image/analyze/url
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

prompt=请分析这张数学图表的内容&imageUrl=https://example.com/chart.jpg
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8

这是一张关于函数y=x²的图像，显示了抛物线的特征：
1. 开口向上的抛物线
2. 顶点在原点(0,0)
3. 关于y轴对称
4. 函数值在x=0处达到最小值0
```

**错误响应：**
```
HTTP/1.1 500 Internal Server Error
Content-Type: text/plain; charset=utf-8

图片分析失败: 无法访问指定的图片URL
```

### 2.4 图片分析接口 - 文件上传方式

**接口描述：** 基于Spring AI Alibaba多模态模型，通过文件上传进行图像内容分析

**请求信息：**
- **URL：** `POST /image/analyze/upload`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 图片文件 | 支持jpg/png/gif等图片格式 |
| prompt | String | 否 | 分析提示词 | "请分析这张图片的内容" |

**请求示例：**
```
POST /image/analyze/upload
Content-Type: multipart/form-data
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="math_chart.jpg"
Content-Type: image/jpeg

[binary image data]
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="prompt"

请详细分析这张数学图表
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

**成功响应：**
```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8

这是一张数学函数图像，包含以下要素：
1. 坐标系：标准的笛卡尔坐标系，有x轴和y轴
2. 函数曲线：红色的抛物线，方程为y=x²
3. 特征点：顶点在原点(0,0)，开口向上
4. 对称性：关于y轴对称
5. 适用于高等数学函数课程教学
```

**错误响应：**
```
HTTP/1.1 400 Bad Request
Content-Type: text/plain; charset=utf-8

请上传图片文件
```

**请求信息：**
- **URL：** `POST /api/chat/teaching-advice`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 教学主题 | "线性代数中的矩阵运算" |
| subject | String | 是 | 学科 | "数学" |
| difficulty | String | 否 | 难度级别 | "undergraduate/graduate" |
| studentLevel | String | 否 | 学生水平 | "大一/大二/研究生" |

**请求示例：**
```json
{
  "topic": "线性代数中的矩阵运算",
  "subject": "数学",
  "difficulty": "undergraduate",
  "studentLevel": "大一"
}
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "teachingMethods": [
    {
      "method": "可视化教学",
      "description": "使用图形化工具展示矩阵运算过程",
      "tools": ["MATLAB", "Python matplotlib"]
    },
    {
      "method": "分步骤讲解",
      "description": "将复杂的矩阵运算分解为简单步骤",
      "steps": ["矩阵定义", "基本运算规则", "实际应用"]
    }
  ],
  "commonDifficulties": ["矩阵乘法规则理解", "维度匹配问题"],
  "practiceExercises": ["2x2矩阵乘法练习", "矩阵转置运算"],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2.3 课程内容分析

**接口描述：** 分析上传的教学素材并提供内容总结和建议

**请求信息：**
- **URL：** `POST /api/chat/content-analysis`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 教学素材文件 | 支持pdf/docx/pptx/jpg/png |
| analysisType | String | 否 | 分析类型 | "summary/difficulty/structure" |
| subject | String | 否 | 学科领域 | "数学/物理/化学等" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "contentSummary": "该文档介绍了高等数学中的极限概念，包含定义、性质和计算方法",
  "keyPoints": [
    "极限的ε-δ定义",
    "极限的四则运算法则",
    "重要极限公式"
  ],
  "difficultyLevel": "中等",
  "suggestedTeachingTime": "2课时",
  "prerequisites": ["函数概念", "数列基础"],
  "teachingSuggestions": [
    "建议先复习函数的基本概念",
    "通过图形直观理解极限概念",
    "多做计算练习巩固方法"
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2.4 学术写作辅助

**接口描述：** 为教师提供学术论文、教案等写作建议

**请求信息：**
- **URL：** `POST /api/chat/writing-assistance`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| content | String | 是 | 需要改进的文本内容 | "本课程旨在介绍微积分的基本概念..." |
| writingType | String | 是 | 写作类型 | "lesson_plan/paper/syllabus" |
| improvementAreas | Array | 否 | 需要改进的方面 | ["clarity", "structure", "academic_tone"] |

**请求示例：**
```json
{
  "content": "本课程旨在介绍微积分的基本概念，包括极限、导数和积分。学生将学习这些概念的定义和应用。",
  "writingType": "lesson_plan",
  "improvementAreas": ["clarity", "structure"]
}
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "improvedContent": "本课程系统介绍微积分的核心概念，涵盖极限理论、导数计算与应用、积分方法与技巧三个主要模块。通过理论讲解与实例分析相结合的方式，帮助学生深入理解微积分的数学思想，掌握解决实际问题的方法。",
  "improvements": [
    {
      "area": "结构优化",
      "suggestion": "明确了课程的三个主要模块"
    },
    {
      "area": "表达清晰",
      "suggestion": "使用了更具体的描述词汇"
    }
  ],
  "additionalSuggestions": [
    "建议添加具体的学习目标",
    "可以补充课程的评估方式"
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2.5 智能对话助手

**接口描述：** 通用智能对话助手，支持连续对话和上下文理解，为教师提供全方位教学支持

**请求信息：**
- **URL：** `POST /api/chat/assistant`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| message | String | 是 | 用户消息 | "我想了解如何更好地教授线性代数" |
| conversationId | String | 否 | 对话会话ID，保持上下文连续性 | "conv_123456" |
| assistantMode | String | 否 | 助手模式 | "general/teaching/research/writing" |
| context | Object | 否 | 上下文信息 | - |
| context.subject | String | 否 | 当前讨论的学科 | "线性代数" |
| context.courseLevel | String | 否 | 课程层次 | "本科" |
| context.currentTopic | String | 否 | 当前话题 | "矩阵运算" |

**请求示例：**
```json
{
  "message": "我想了解如何更好地教授线性代数中的矩阵运算，学生总是觉得很抽象",
  "conversationId": "conv_123456",
  "assistantMode": "teaching",
  "context": {
    "subject": "线性代数",
    "courseLevel": "本科",
    "currentTopic": "矩阵运算"
  }
}
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "response": "理解您的困扰！矩阵运算确实是线性代数中学生感到抽象的部分。我建议采用以下几种教学策略：\n\n1. **几何可视化**：使用图形展示矩阵变换的几何意义，比如旋转、缩放、反射等\n2. **实际应用导入**：从图像处理、经济模型等实际问题引入矩阵运算\n3. **分步骤教学**：先教2×2矩阵，再逐步扩展到更大维度\n4. **动手计算**：让学生多做手工计算，建立直觉\n\n您希望我详细展开哪个方面的教学方法？",
  "conversationId": "conv_123456",
  "messageId": "msg_789012",
  "suggestions": [
    "详细了解几何可视化方法",
    "获取矩阵运算的实际应用案例",
    "查看相关教学资源推荐",
    "讨论学生常见错误及解决方案"
  ],
  "relatedTopics": ["线性变换", "特征值与特征向量", "矩阵分解"],
  "assistantMode": "teaching",
  "timestamp": "2024-01-01T10:00:00"
}
```

### 2.6 流式对话助手

**接口描述：** 流式智能对话助手，实时返回生成内容，提供更好的交互体验

**请求信息：**
- **URL：** `POST /api/chat/assistant/stream`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| message | String | 是 | 用户消息 | "请解释一下拉格朗日乘数法的原理" |
| conversationId | String | 否 | 对话会话ID | "conv_123456" |
| assistantMode | String | 否 | 助手模式 | "general/teaching/research" |
| streamOptions | Object | 否 | 流式选项 | - |
| streamOptions.includeThinking | Boolean | 否 | 是否包含思考过程 | false |
| streamOptions.chunkSize | Integer | 否 | 数据块大小 | 50 |

**请求示例：**
```json
{
  "message": "请解释一下拉格朗日乘数法的原理，并给出一个具体例子",
  "conversationId": "conv_123456",
  "assistantMode": "teaching",
  "streamOptions": {
    "includeThinking": false,
    "chunkSize": 50
  }
}
```

**成功响应：** (Server-Sent Events格式)
```
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive

data: {"type": "start", "conversationId": "conv_123456", "messageId": "msg_789012"}

data: {"type": "content", "content": "拉格朗日乘数法是"}

data: {"type": "content", "content": "解决约束优化问题的"}

data: {"type": "content", "content": "重要数学方法。其核心思想是"}

data: {"type": "content", "content": "将约束条件转化为"}

data: {"type": "content", "content": "无约束优化问题..."}

data: {"type": "suggestions", "suggestions": ["查看具体计算步骤", "了解几何意义", "获取练习题"]}

data: {"type": "end", "timestamp": "2024-01-01T10:00:00"}
```

### 2.7 对话历史管理

**接口描述：** 管理用户的对话历史记录

**请求信息：**
- **URL：** `GET /api/chat/conversations`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码 | 1 |
| limit | Integer | 否 | 每页数量 | 20 |
| assistantMode | String | 否 | 助手模式筛选 | "teaching" |
| startDate | String | 否 | 开始日期 | "2024-01-01" |
| endDate | String | 否 | 结束日期 | "2024-01-31" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "conversations": [
    {
      "conversationId": "conv_123456",
      "title": "线性代数教学方法讨论",
      "assistantMode": "teaching",
      "messageCount": 15,
      "lastMessage": "谢谢您的建议，我会尝试使用可视化方法",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T11:30:00",
      "tags": ["线性代数", "教学方法", "可视化"]
    }
  ],
  "total": 25,
  "page": 1,
  "limit": 20
}
```

### 2.8 获取对话详情

**接口描述：** 获取特定对话的详细消息记录

**请求信息：**
- **URL：** `GET /api/chat/conversations/{conversationId}`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | String | 是 | 对话ID |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "conversationId": "conv_123456",
  "title": "线性代数教学方法讨论",
  "assistantMode": "teaching",
  "messages": [
    {
      "messageId": "msg_001",
      "role": "user",
      "content": "我想了解如何更好地教授线性代数",
      "timestamp": "2024-01-01T10:00:00"
    },
    {
      "messageId": "msg_002",
      "role": "assistant",
      "content": "线性代数确实是一门需要特殊教学方法的课程...",
      "suggestions": ["详细了解可视化方法", "获取教学资源"],
      "timestamp": "2024-01-01T10:01:00"
    }
  ],
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T11:30:00"
}
```

## 3. 多模态素材上传模块

### 3.1 学术文档上传

**接口描述：** 基于Spring Boot标准实现的文档上传接口，支持教案、论文、课件等学术资料

**请求信息：**
- **URL：** `POST /api/materials/upload/document`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 文档文件 | 支持doc/docx/pdf/pptx，最大50MB |
| subject | String | 是 | 学科分类 | 如：高等数学、线性代数、概率论等 |
| courseLevel | String | 是 | 课程层次 | 如：本科、研究生、博士 |
| documentType | String | 是 | 文档类型 | lesson_plan/syllabus/paper/textbook |
| title | String | 否 | 文档标题 | 最大100字符 |
| description | String | 否 | 文档描述 | 最大500字符 |
| keywords | String | 否 | 关键词标签（逗号分隔） | 如："导数,微积分,极限" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "doc_123456",
  "filename": "高等数学导数教案.docx",
  "originalName": "calculus_derivative_lesson.docx",
  "subject": "高等数学",
  "courseLevel": "本科",
  "documentType": "lesson_plan",
  "size": 2048000,
  "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "keywords": ["导数", "微积分", "极限"],
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "/api/files/download/doc_123456"
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
  "message": "不支持的文件类型",
  "path": "/api/materials/upload/document"
}
```

### 3.2 学术语音素材上传及转文字

**接口描述：** 基于Spring AI Alibaba AudioTranscriptionModel的语音上传和转文字功能

**请求信息：**
- **URL：** `POST /api/materials/upload/audio`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 语音文件 | 支持mp3/wav/m4a/flac，最大100MB |
| transcriptionMode | String | 否 | 转录模式 | sync/async，默认sync |
| needTranscription | Boolean | 否 | 是否需要语音转文字 | 默认true |
| subject | String | 否 | 学科分类 | - |
| audioType | String | 否 | 音频类型 | lecture/seminar/discussion/interview |
| description | String | 否 | 语音描述 | 最大200字符 |
| speaker | String | 否 | 主讲人 | 教授姓名或职称 |
| language | String | 否 | 语言 | zh/en，默认zh |

**同步转录成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "audio_123456",
  "filename": "calculus_lecture.mp3",
  "originalName": "微积分专题讲座.mp3",
  "subject": "高等数学",
  "audioType": "lecture",
  "description": "微积分基本定理专题讲座",
  "speaker": "张教授",
  "duration": 3600,
  "size": 25600000,
  "language": "zh",
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "/api/files/download/audio_123456",
  "transcription": {
    "text": "今天我们来深入探讨微积分基本定理，这是连接微分和积分的重要桥梁...",
    "confidence": 0.96,
    "segments": [
      {
        "start": 0.0,
        "end": 5.2,
        "text": "今天我们来深入探讨微积分基本定理"
      }
    ]
  },
  "keyPoints": ["微积分基本定理", "牛顿-莱布尼茨公式", "定积分计算"]
}
```

**异步转录成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "id": "audio_123456",
  "filename": "calculus_lecture.mp3",
  "taskId": "transcription_task_789012",
  "status": "processing",
  "message": "语音转录任务已启动",
  "estimatedTime": 120,
  "uploadedAt": "2024-01-01T10:00:00",
  "statusUrl": "/api/tasks/transcription_task_789012/status"
}
```

### 3.3 分页查询教学素材列表

**接口描述：** 分页查询用户上传的教学素材列表，支持多维度筛选和排序

**请求信息：**
- **URL：** `GET /api/materials`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| materialType | String | 否 | 素材类型筛选 | document/audio |
| subject | String | 否 | 学科分类筛选 | "高等数学" |
| courseLevel | String | 否 | 课程层次筛选 | undergraduate/graduate/doctoral |
| documentType | String | 否 | 文档类型筛选 | lesson_plan/syllabus/paper/textbook/exercise |
| audioType | String | 否 | 音频类型筛选 | lecture/seminar/discussion/interview |
| keywords | String | 否 | 关键词搜索 | "微积分" |
| page | Integer | 否 | 页码，默认1 | 1 |
| limit | Integer | 否 | 每页数量，默认10，最大100 | 10 |
| sortBy | String | 否 | 排序字段，默认created_at | created_at/file_size/title |
| sortOrder | String | 否 | 排序方向，默认desc | asc/desc |

**请求示例：**
```
GET /api/materials?materialType=document&subject=高等数学&page=1&limit=10&sortBy=created_at&sortOrder=desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "materials": [
    {
      "materialId": "mat_123456",
      "originalName": "高等数学教案.pdf",
      "materialType": "document",
      "contentType": "application/pdf",
      "fileSize": 2048576,
      "title": "微积分基础教案",
      "subject": "高等数学",
      "courseLevel": "undergraduate",
      "documentType": "lesson_plan",
      "audioType": null,
      "createdAt": "2024-01-01T10:00:00"
    },
    {
      "materialId": "mat_789012",
      "originalName": "数学讲座录音.mp3",
      "materialType": "audio",
      "contentType": "audio/mpeg",
      "fileSize": 52428800,
      "title": "微积分专题讲座",
      "subject": "高等数学",
      "courseLevel": "undergraduate",
      "documentType": null,
      "audioType": "lecture",
      "createdAt": "2024-01-01T09:30:00"
    }
  ],
  "pagination": {
    "currentPage": 1,
    "pageSize": 10,
    "total": 25,
    "totalPages": 3
  }
}
```

### 3.4 获取教学素材详情

**接口描述：** 获取指定教学素材的详细信息，包括下载链接和转录文本

**请求信息：**
- **URL：** `GET /api/materials/{materialId}`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| materialId | String | 是 | 素材ID |

**请求示例：**
```
GET /api/materials/mat_123456
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "materialId": "mat_123456",
  "originalName": "微积分讲座录音.mp3",
  "materialType": "audio",
  "contentType": "audio/mpeg",
  "fileSize": 52428800,
  "downloadUrl": "https://tech-czq.oss-cn-beijing.aliyuncs.com/audio/...",
  "title": "微积分基本定理专题讲座",
  "description": "深入讲解微积分基本定理的数学表述、几何意义和实际应用",
  "subject": "高等数学",
  "courseLevel": "undergraduate",
  "documentType": null,
  "keywords": "微积分,基本定理,导数,积分",
  "duration": 3600,
  "language": "zh",
  "audioType": "lecture",
  "speaker": "张教授",
  "transcriptionText": "今天我们来深入探讨微积分基本定理，这是连接微分和积分的重要桥梁。首先，让我们回顾一下导数的定义...",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:30:00"
}
```

**错误响应：**
```json
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "素材不存在或无权访问",
  "path": "/api/materials/mat_123456"
}
```

### 3.5 删除教学素材

**接口描述：** 删除指定的教学素材，同时删除OSS中的文件和相关转录任务

**请求信息：**
- **URL：** `DELETE /api/materials/{materialId}`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| materialId | String | 是 | 素材ID |

**请求示例：**
```
DELETE /api/materials/mat_123456
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "素材删除成功",
  "materialId": "mat_123456"
}
```

### 3.6 获取素材下载链接

**接口描述：** 获取教学素材的临时下载链接，链接有效期1小时

**请求信息：**
- **URL：** `GET /api/materials/{materialId}/download`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| materialId | String | 是 | 素材ID |

**请求示例：**
```
GET /api/materials/mat_123456/download
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "materialId": "mat_123456",
  "downloadUrl": "https://tech-czq.oss-cn-beijing.aliyuncs.com/audio/...",
  "expiresIn": 3600
}
```

### 3.7 获取用户素材统计信息

**接口描述：** 获取当前用户的教学素材统计信息，包括总数、类型分布、学科分布等

**请求信息：**
- **URL：** `GET /api/materials/statistics`
- **认证：** 需要JWT Token

**请求示例：**
```
GET /api/materials/statistics
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "totalMaterials": 25,
  "typeStatistics": {
    "document": 15,
    "audio": 10
  },
  "subjectStatistics": [
    {
      "subject": "高等数学",
      "count": 12
    },
    {
      "subject": "线性代数",
      "count": 8
    },
    {
      "subject": "概率论",
      "count": 5
    }
  ],
  "todayMaterials": 3
}
```

### 3.8 语音转录任务状态查询

**接口描述：** 查询异步语音转录任务状态

**请求信息：**
- **URL：** `GET /api/tasks/{taskId}/status`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | String | 是 | 任务ID |

**处理中响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "taskId": "transcription_task_789012",
  "status": "processing",
  "progress": 45,
  "message": "正在处理语音转录",
  "startedAt": "2024-01-01T10:00:00"
}
```

**完成响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "taskId": "transcription_task_789012",
  "status": "completed",
  "progress": 100,
  "result": {
    "audioId": "audio_123456",
    "transcription": {
      "text": "完整的转录文本内容...",
      "confidence": 0.94,
      "duration": 3600
    }
  },
  "startedAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-01T10:02:00"
}
```
|--------|------|------|------|------|
| file | File | 是 | 语音文件 | 支持mp3/wav/m4a/flac，最大200MB |
| needTranscription | Boolean | 否 | 是否需要语音转文字 | 默认true |
| subject | String | 否 | 学科分类 | - |
| audioType | String | 否 | 音频类型 | lecture/seminar/discussion/interview |
| description | String | 否 | 语音描述 | 最大200字符 |
| speaker | String | 否 | 主讲人 | 教授姓名或职称 |
| language | String | 否 | 语言 | zh-CN/en-US |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "audio_123456",
  "filename": "calculus_lecture.mp3",
  "originalName": "微积分专题讲座.mp3",
  "subject": "高等数学",
  "audioType": "lecture",
  "description": "微积分基本定理专题讲座",
  "speaker": "张教授",
  "duration": 3600,
  "size": 25600000,
  "language": "zh-CN",
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "https://example.com/audio/audio_123456.mp3",
  "transcription": "今天我们来深入探讨微积分基本定理，这是连接微分和积分的重要桥梁...",
  "keyPoints": ["微积分基本定理", "牛顿-莱布尼茨公式", "定积分计算"]
}
```

## 4. AI资源自动制作模块

### 4.1 生成学术PPT课件

**接口描述：** 基于Spring AI Alibaba ChatClient和Function Calling，自动生成专业PPT课件

**请求信息：**
- **URL：** `POST /api/ai/generate/ppt`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 课件主题 | "微积分基本定理" |
| subject | String | 是 | 学科 | "高等数学" |
| courseLevel | String | 是 | 课程层次 | "undergraduate/graduate/doctoral" |
| slideCount | Integer | 否 | 幻灯片数量，默认20 | 25 |
| style | String | 否 | 课件风格 | "academic/professional/interactive" |
| includeFormulas | Boolean | 否 | 是否包含数学公式 | true |
| includeProofs | Boolean | 否 | 是否包含证明过程 | true |
| targetAudience | String | 否 | 目标受众 | "mathematics_major/engineering" |
| duration | Integer | 否 | 预期授课时长(分钟) | 90 |
| language | String | 否 | 语言 | "zh/en"，默认zh |

**请求示例：**
```json
{
  "topic": "微积分基本定理",
  "subject": "高等数学",
  "courseLevel": "undergraduate",
  "slideCount": 25,
  "style": "academic",
  "includeFormulas": true,
  "includeProofs": true,
  "targetAudience": "mathematics_major",
  "duration": 90,
  "language": "zh"
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "ppt_task_123456",
  "message": "学术PPT生成任务已启动",
  "estimatedTime": 300,
  "status": "processing",
  "progress": 0,
  "preview": {
    "outline": [
      "1. 引言与历史背景",
      "2. 微积分基本定理的表述",
      "3. 定理的几何意义",
      "4. 证明过程详解",
      "5. 应用实例与练习"
    ],
    "estimatedSlides": 25
  },
  "statusUrl": "/api/tasks/ppt_task_123456/status"
}
```

### 4.2 生成学术习题

**接口描述：** 基于Spring AI Alibaba，自动生成专业习题集

**请求信息：**
- **URL：** `POST /api/ai/generate/quiz`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 知识点主题 | "线性代数中的特征值" |
| subject | String | 是 | 学科 | "线性代数" |
| courseLevel | String | 是 | 课程层次 | "undergraduate/graduate" |
| difficulty | String | 否 | 难度等级 | "basic/intermediate/advanced" |
| questionCount | Integer | 否 | 题目数量，默认10 | 15 |
| questionTypes | String | 否 | 题型（逗号分隔） | "calculation,proof,application,conceptual" |
| includeSteps | Boolean | 否 | 是否包含解题步骤 | true |
| includeAnswers | Boolean | 否 | 是否包含答案 | true |
| timeLimit | Integer | 否 | 建议完成时间(分钟) | 120 |
| language | String | 否 | 语言 | "zh/en"，默认zh |

**请求示例：**
```json
{
  "topic": "线性代数中的特征值",
  "subject": "线性代数",
  "courseLevel": "undergraduate",
  "difficulty": "intermediate",
  "questionCount": 15,
  "questionTypes": "calculation,proof,application",
  "includeSteps": true,
  "includeAnswers": true,
  "timeLimit": 120,
  "language": "zh"
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "quiz_task_123456",
  "message": "学术习题生成任务已启动",
  "estimatedTime": 180,
  "status": "processing",
  "progress": 0,
  "preview": {
    "questionDistribution": {
      "calculation": 8,
      "proof": 4,
      "application": 3
    },
    "difficultyLevel": "intermediate",
    "estimatedTime": 120
  },
  "statusUrl": "/api/tasks/quiz_task_123456/status"
}
```

### 4.3 生成学术讲解文本

**接口描述：** 基于Spring AI Alibaba，生成详细的学术讲解文本

**请求信息：**
- **URL：** `POST /api/ai/generate/explanation`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 知识点主题 | "拉格朗日乘数法" |
| subject | String | 是 | 学科 | "高等数学" |
| courseLevel | String | 是 | 课程层次 | "undergraduate/graduate" |
| style | String | 否 | 讲解风格 | "rigorous/intuitive/applied" |
| length | String | 否 | 文本长度 | "concise/detailed/comprehensive" |
| includeExamples | Boolean | 否 | 是否包含实例 | true |
| includeProofs | Boolean | 否 | 是否包含证明 | true |
| includeApplications | Boolean | 否 | 是否包含应用场景 | true |
| targetAudience | String | 否 | 目标受众 | "mathematics_major/engineering" |
| language | String | 否 | 语言 | "zh/en"，默认zh |

**请求示例：**
```json
{
  "topic": "拉格朗日乘数法",
  "subject": "高等数学",
  "courseLevel": "undergraduate",
  "style": "rigorous",
  "length": "detailed",
  "includeExamples": true,
  "includeProofs": true,
  "includeApplications": true,
  "targetAudience": "mathematics_major",
  "language": "zh"
}
```

**同步响应模式（较短文本）：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": "# 拉格朗日乘数法详解\n\n## 理论基础\n拉格朗日乘数法是解决约束优化问题的重要数学方法...",
  "metadata": {
    "wordCount": 2500,
    "sections": [
      "理论基础与动机",
      "数学表述与定理",
      "几何直观理解",
      "算法步骤",
      "典型应用实例",
      "相关定理证明"
    ],
    "generatedAt": "2024-01-01T10:00:00",
    "estimatedReadingTime": 12
  }
}
```

**异步响应模式（较长文本）：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "explanation_task_123456",
  "message": "学术讲解文本生成任务已启动",
  "estimatedTime": 90,
  "status": "processing",
  "progress": 0,
  "preview": {
    "sections": [
      "理论基础与动机",
      "数学表述与定理",
      "几何直观理解",
      "算法步骤",
      "典型应用实例",
      "相关定理证明"
    ],
    "estimatedLength": "comprehensive"
  },
  "statusUrl": "/api/tasks/explanation_task_123456/status"
}
```
      "calculation": 8,
      "proof": 4,
      "application": 3
    },
    "estimatedDifficulty": "intermediate"
  }
}
```

### 4.3 生成学术讲解文本

**接口描述：** 基于高等教育知识点生成详细的学术讲解文本

**请求信息：**
- **URL：** `POST /api/ai/generate/explanation`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| topic | String | 是 | 知识点主题 | "拉格朗日乘数法" |
| subject | String | 是 | 学科 | "高等数学" |
| courseLevel | String | 是 | 课程层次 | "本科/研究生" |
| style | String | 否 | 讲解风格 | "rigorous/intuitive/applied" |
| length | String | 否 | 文本长度 | "concise/detailed/comprehensive" |
| includeExamples | Boolean | 否 | 是否包含实例 | true |
| includeProofs | Boolean | 否 | 是否包含证明 | true |
| targetAudience | String | 否 | 目标受众 | "mathematics_major/engineering" |

**请求示例：**
```json
{
  "topic": "拉格朗日乘数法",
  "subject": "高等数学",
  "courseLevel": "本科",
  "style": "rigorous",
  "length": "detailed",
  "includeExamples": true,
  "includeProofs": true,
  "targetAudience": "mathematics_major"
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "explanation_task_123456",
  "message": "学术讲解文本生成任务已启动",
  "estimatedTime": 90,
  "status": "processing",
  "preview": {
    "sections": [
      "理论基础与动机",
      "数学表述与定理",
      "几何直观理解",
      "算法步骤",
      "典型应用实例",
      "相关定理证明"
    ]
  }
}
```

## 5. 教学资源管理模块

### 5.1 获取学术资源列表

**接口描述：** 基于ResponseEntity标准的资源管理接口，支持分页和多维度筛选

**请求信息：**
- **URL：** `GET /api/resources`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，从1开始 | 1 |
| size | Integer | 否 | 每页数量，默认20 | 20 |
| subject | String | 否 | 学科筛选 | "高等数学" |
| courseLevel | String | 否 | 课程层次筛选 | "undergraduate/graduate/doctoral" |
| keyword | String | 否 | 关键词搜索 | "微积分" |
| resourceType | String | 否 | 资源类型 | "ppt/quiz/document/audio/video" |
| difficulty | String | 否 | 难度级别 | "basic/intermediate/advanced" |
| createdBy | String | 否 | 创建者筛选 | "user_123456" |
| startDate | String | 否 | 创建开始日期 | "2024-01-01" |
| endDate | String | 否 | 创建结束日期 | "2024-12-31" |
| sortBy | String | 否 | 排序字段 | "createdAt/downloadCount/rating" |
| sortDirection | String | 否 | 排序方向 | "asc/desc" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": [
    {
      "id": "res_123456",
      "title": "微积分基本定理详解PPT",
      "resourceType": "ppt",
      "subject": "高等数学",
      "courseLevel": "undergraduate",
      "difficulty": "intermediate",
      "description": "包含微积分基本定理的证明、几何意义和应用实例",
      "keywords": ["微积分", "基本定理", "牛顿-莱布尼茨公式"],
      "createdBy": {
        "id": "user_123",
        "username": "张教授",
        "institution": "清华大学数学系"
      },
      "downloadCount": 256,
      "viewCount": 1024,
      "rating": 4.9,
      "ratingCount": 45,
      "fileSize": 5120000,
      "language": "zh",
      "thumbnailUrl": "/api/files/thumbnail/res_123456",
      "downloadUrl": "/api/files/download/res_123456",
      "previewUrl": "/api/resources/res_123456/preview",
      "createdAt": "2024-01-01T10:00:00",
      "lastUpdated": "2024-01-15T14:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 350,
  "totalPages": 18,
  "first": true,
  "last": false,
  "numberOfElements": 20,
  "aggregations": {
    "subjects": {"高等数学": 120, "线性代数": 85, "概率论": 65},
    "courseLevels": {"undergraduate": 200, "graduate": 100, "doctoral": 50},
    "resourceTypes": {"ppt": 150, "document": 100, "quiz": 80, "audio": 30, "video": 20}
  }
}
```

### 5.2 资源智能分类

**接口描述：** 基于Spring AI Alibaba的智能分类功能，自动为资源分配学科和知识点标签

**请求信息：**
- **URL：** `POST /api/resources/{resourceId}/classify`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resourceId | String | 是 | 资源ID |

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| forceReclassify | Boolean | 否 | 是否强制重新分类 | false |
| includeSuggestions | Boolean | 否 | 是否包含改进建议 | true |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "resourceId": "res_123456",
  "classification": {
    "primarySubject": "高等数学",
    "secondarySubjects": ["数学分析", "微积分"],
    "knowledgePoints": [
      {
        "name": "微积分基本定理",
        "confidence": 0.96,
        "prerequisites": ["导数概念", "定积分定义"]
      },
      {
        "name": "牛顿-莱布尼茨公式",
        "confidence": 0.89,
        "prerequisites": ["原函数概念"]
      }
    ],
    "difficulty": "intermediate",
    "courseLevel": "undergraduate",
    "suggestedKeywords": ["微积分", "基本定理", "导数", "积分", "连续函数"],
    "topicHierarchy": [
      "数学 > 高等数学 > 微积分 > 积分学 > 微积分基本定理"
    ]
  },
  "suggestions": [
    {
      "type": "content_enhancement",
      "message": "建议增加几何意义的图示说明"
    },
    {
      "type": "prerequisite_clarification",
      "message": "可以添加导数和定积分概念的回顾"
    }
  ],
  "confidence": 0.94,
  "processedAt": "2024-01-01T10:00:00"
}
```

### 5.3 资源预览

**接口描述：** 获取资源的详细预览信息和内容摘要

**请求信息：**
- **URL：** `GET /api/resources/{resourceId}/preview`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resourceId | String | 是 | 资源ID |

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| includeContent | Boolean | 否 | 是否包含内容摘要 | true |
| generateSummary | Boolean | 否 | 是否生成AI摘要 | true |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "resourceId": "res_123456",
  "title": "微积分基本定理详解PPT",
  "resourceType": "ppt",
  "metadata": {
    "slideCount": 35,
    "estimatedDuration": 90,
    "fileSize": 5120000,
    "language": "zh",
    "lastModified": "2024-01-15T14:30:00"
  },
  "outline": [
    "1. 引言与历史背景",
    "2. 微积分基本定理的表述",
    "3. 定理的几何意义",
    "4. 严格数学证明",
    "5. 典型应用实例",
    "6. 相关定理与推广"
  ],
  "summary": {
    "content": "本PPT系统介绍微积分基本定理，从历史背景到严格证明，再到实际应用，为本科数学专业学生提供完整的理论框架...",
    "keyPoints": [
      "微积分基本定理连接了微分与积分",
      "牛顿-莱布尼茨公式的应用",
      "连续函数的原函数存在性"
    ],
    "generatedBy": "ai",
    "generatedAt": "2024-01-01T10:00:00"
  },
  "prerequisites": ["导数概念", "定积分定义", "连续函数性质"],
  "learningObjectives": [
    "理解微积分基本定理的数学表述",
    "掌握定理的几何意义",
    "能够应用定理计算定积分"
  ],
  "previewImages": [
    "/api/files/preview/res_123456/slide1",
    "/api/files/preview/res_123456/slide5",
    "/api/files/preview/res_123456/slide10"
  ],
  "relatedResources": [
    {
      "id": "res_789012",
      "title": "导数应用专题",
      "similarity": 0.75
    }
  ]
}
```

{
  "subjects": [
    {
      "code": "mathematics",
      "name": "数学类",
      "subcategories": [
        {"code": "calculus", "name": "高等数学"},
        {"code": "linear_algebra", "name": "线性代数"},
        {"code": "probability", "name": "概率论与数理统计"},
        {"code": "discrete_math", "name": "离散数学"}
      ]
    },
    {
      "code": "physics",
      "name": "物理学类",
      "subcategories": [
        {"code": "classical_mechanics", "name": "经典力学"},
        {"code": "electromagnetism", "name": "电磁学"},
        {"code": "quantum_physics", "name": "量子物理"},
        {"code": "thermodynamics", "name": "热力学"}
      ]
    },
    {
      "code": "computer_science",
      "name": "计算机科学",
      "subcategories": [
        {"code": "algorithms", "name": "算法与数据结构"},
        {"code": "programming", "name": "程序设计"},
        {"code": "database", "name": "数据库系统"},
        {"code": "ai", "name": "人工智能"}
      ]
    }
  ],
  "courseLevels": [
    {"code": "undergraduate", "name": "本科"},
    {"code": "graduate", "name": "研究生"},
    {"code": "doctoral", "name": "博士"}
  ],
  "resourceTypes": [
    {"code": "lecture_slides", "name": "课程PPT"},
    {"code": "academic_paper", "name": "学术论文"},
    {"code": "exercise_set", "name": "习题集"},
    {"code": "lab_manual", "name": "实验手册"},
    {"code": "textbook", "name": "教材"},
    {"code": "video_lecture", "name": "视频讲座"}
  ],
  "difficulties": [
    {"code": "basic", "name": "基础"},
    {"code": "intermediate", "name": "中等"},
    {"code": "advanced", "name": "高级"}
  ]
}
```

### 5.3 学术资源预览

**接口描述：** 获取学术资源的详细预览信息

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
  "title": "微积分基本定理详解PPT",
  "type": "lecture_slides",
  "subject": "高等数学",
  "courseLevel": "本科",
  "difficulty": "intermediate",
  "description": "深入讲解微积分基本定理的数学表述、几何意义和应用",
  "outline": [
    "1. 历史背景与动机",
    "2. 定理的数学表述",
    "3. 几何直观理解",
    "4. 严格数学证明",
    "5. 典型应用实例",
    "6. 相关定理与推广"
  ],
  "slideCount": 35,
  "estimatedDuration": 90,
  "prerequisites": ["导数概念", "定积分定义", "连续函数性质"],
  "learningObjectives": [
    "理解微积分基本定理的数学表述",
    "掌握定理的几何意义",
    "能够应用定理计算定积分"
  ],
  "previewImages": [
    "https://example.com/previews/res_123456_slide1.jpg",
    "https://example.com/previews/res_123456_slide5.jpg",
    "https://example.com/previews/res_123456_slide10.jpg"
  ],
  "downloadUrl": "https://example.com/resources/res_123456.pptx",
  "relatedResources": ["res_789012", "res_345678"]
}
```

## 6. RAG个性化知识库模块

### 6.1 构建学科知识库

**接口描述：** 基于Spring AI Alibaba VectorStore和QuestionAnswerAdvisor，构建专业RAG知识库

**请求信息：**
- **URL：** `POST /api/knowledge/build`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| materialIds | String | 是 | 学术资料文件ID列表（逗号分隔） | "doc_123,paper_456,textbook_789" |
| subject | String | 是 | 学科领域 | "高等数学" |
| courseLevel | String | 是 | 课程层次 | "undergraduate/graduate/doctoral" |
| name | String | 是 | 知识库名称 | "高等数学本科知识库" |
| description | String | 否 | 知识库描述 | "涵盖微积分、级数、多元函数等核心内容" |
| specialization | String | 否 | 专业方向 | "mathematics_major/engineering_math/economics_math" |
| language | String | 否 | 主要语言 | "zh/en" |
| includeProofs | Boolean | 否 | 是否包含定理证明 | true |
| chunkSize | Integer | 否 | 文档分块大小 | 1000 |
| chunkOverlap | Integer | 否 | 分块重叠大小 | 200 |
| enableSimilarityThreshold | Boolean | 否 | 是否启用相似度阈值 | true |
| similarityThreshold | Double | 否 | 相似度阈值 | 0.7 |

**请求示例：**
```json
{
  "materialIds": "textbook_123456,lecture_789012,paper_345678",
  "subject": "高等数学",
  "courseLevel": "undergraduate",
  "name": "高等数学本科知识库",
  "description": "涵盖极限、导数、积分、级数、多元函数微积分等核心内容",
  "specialization": "mathematics_major",
  "language": "zh",
  "includeProofs": true,
  "chunkSize": 1000,
  "chunkOverlap": 200,
  "enableSimilarityThreshold": true,
  "similarityThreshold": 0.75
}
```

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "taskId": "kb_build_123456",
  "knowledgeBaseId": "kb_123456",
  "message": "学科知识库构建任务已启动",
  "estimatedTime": 900,
  "status": "processing",
  "progress": 0,
  "preview": {
    "materialCount": 3,
    "estimatedChunks": 1500,
    "knowledgeAreas": ["极限理论", "微分学", "积分学", "级数理论"],
    "processingSteps": [
      "文档解析与预处理",
      "语义分块与向量化",
      "向量存储构建",
      "索引优化"
    ]
  },
  "statusUrl": "/api/tasks/kb_build_123456/status"
}
```

### 6.2 学术知识检索

**接口描述：** 基于Spring AI Alibaba VectorStore的语义搜索和知识检索

**请求信息：**
- **URL：** `GET /api/knowledge/search`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 查询内容 | "微积分基本定理的几何意义" |
| knowledgeBaseId | String | 否 | 知识库ID | "kb_123456" |
| subject | String | 否 | 学科范围 | "高等数学" |
| topK | Integer | 否 | 返回结果数量，默认5 | 10 |
| similarityThreshold | Double | 否 | 最小相关度阈值 | 0.7 |
| includeProofs | Boolean | 否 | 是否包含证明内容 | true |
| courseLevel | String | 否 | 课程层次筛选 | "undergraduate/graduate" |
| filterMetadata | String | 否 | 元数据过滤条件 | "difficulty:intermediate" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "results": [
    {
      "id": "chunk_123456",
      "content": "微积分基本定理揭示了微分与积分之间的根本联系。从几何角度看，该定理表明函数图像下方的面积（定积分）与该函数的原函数（不定积分）之间存在直接关系...",
      "metadata": {
        "source": "《数学分析》第四版 - 华东师范大学",
        "chapter": "第五章 定积分",
        "section": "5.3 微积分基本定理",
        "pageNumber": 156,
        "documentId": "textbook_123456",
        "knowledgePoints": ["微积分基本定理", "几何意义", "牛顿-莱布尼茨公式"],
        "difficulty": "intermediate",
        "courseLevel": "undergraduate",
        "hasProof": true
      },
      "similarity": 0.96,
      "distance": 0.04
    },
    {
      "id": "chunk_789012",
      "content": "从历史角度来看，牛顿和莱布尼茨独立发现了微积分基本定理，这一发现被认为是数学史上最重要的成就之一...",
      "metadata": {
        "source": "《微积分的历史》- 学术论文集",
        "documentId": "paper_345678",
        "knowledgePoints": ["数学史", "牛顿", "莱布尼茨"],
        "difficulty": "basic",
        "courseLevel": "undergraduate"
      },
      "similarity": 0.88,
      "distance": 0.12
    }
  ],
  "pagination": {
    "total": 8,
    "limit": 10,
    "offset": 0
  },
  "query": "微积分基本定理的几何意义",
  "knowledgeBaseId": "kb_123456",
  "searchTime": 0.23,
  "suggestions": [
    "微积分基本定理证明",
    "定积分的几何意义",
    "牛顿-莱布尼茨公式应用"
  ],
  "relatedConcepts": [
    "定积分与面积的关系",
    "原函数的存在性",
    "连续函数的性质"
  ]
}
```

### 6.3 基于RAG的智能问答

**接口描述：** 基于Spring AI Alibaba QuestionAnswerAdvisor的增强问答功能

**请求信息：**
- **URL：** `POST /api/knowledge/chat`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 用户问题 | "请详细解释微积分基本定理" |
| knowledgeBaseId | String | 否 | 知识库ID | "kb_123456" |
| conversationId | String | 否 | 会话ID，保持上下文连续性 | "conv_123456" |
| answerMode | String | 否 | 回答模式 | "detailed/concise/tutorial" |
| includeReferences | Boolean | 否 | 是否包含参考来源 | true |
| topK | Integer | 否 | 检索文档数量 | 5 |
| similarityThreshold | Double | 否 | 相似度阈值 | 0.7 |
| language | String | 否 | 回答语言 | "zh/en" |

**请求示例：**
```json
{
  "query": "请详细解释微积分基本定理，并给出几何直观理解",
  "knowledgeBaseId": "kb_123456",
  "conversationId": "conv_123456",
  "answerMode": "detailed",
  "includeReferences": true,
  "topK": 5,
  "similarityThreshold": 0.75,
  "language": "zh"
}
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "answer": "微积分基本定理是微积分学中最重要的定理之一，它建立了微分与积分之间的根本联系...\n\n从几何角度理解：\n1. 定积分∫[a,b]f(x)dx表示函数f(x)在区间[a,b]上与x轴围成的有向面积\n2. 如果F(x)是f(x)的原函数，那么这个面积等于F(b)-F(a)\n3. 这意味着求面积的问题转化为求原函数的问题...",
  "conversationId": "conv_123456",
  "messageId": "msg_789012",
  "metadata": {
    "answerMode": "detailed",
    "retrievedDocuments": 5,
    "averageSimilarity": 0.91,
    "processingTime": 1.2,
    "knowledgeBaseUsed": "kb_123456"
  },
  "references": [
    {
      "documentId": "textbook_123456",
      "title": "数学分析第四版",
      "chapter": "第五章 定积分",
      "section": "5.3 微积分基本定理",
      "pageNumber": 156,
      "relevanceScore": 0.96,
      "excerpt": "微积分基本定理揭示了微分与积分之间的根本联系..."
    },
    {
      "documentId": "lecture_789012",
      "title": "微积分基础讲义",
      "section": "定理的几何意义",
      "relevanceScore": 0.89,
      "excerpt": "从几何角度看，该定理表明函数图像下方的面积..."
    }
  ],
  "relatedQuestions": [
    "牛顿-莱布尼茨公式是如何推导的？",
    "微积分基本定理的第二形式是什么？",
    "如何应用基本定理计算定积分？"
  ],
  "keyPoints": [
    "建立了微分与积分的联系",
    "提供了计算定积分的有效方法",
    "具有重要的几何意义"
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 6.4 知识库管理

**接口描述：** 获取用户的知识库列表和管理信息

**请求信息：**
- **URL：** `GET /api/knowledge/bases`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，从0开始 | 0 |
| size | Integer | 否 | 每页数量 | 20 |
| subject | String | 否 | 学科筛选 | "高等数学" |
| courseLevel | String | 否 | 课程层次筛选 | "undergraduate" |
| status | String | 否 | 状态筛选 | "active/building/error" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": [
    {
      "id": "kb_123456",
      "name": "高等数学本科知识库",
      "description": "涵盖微积分、级数、多元函数等核心内容",
      "subject": "高等数学",
      "courseLevel": "undergraduate",
      "specialization": "mathematics_major",
      "status": "active",
      "documentCount": 15,
      "chunkCount": 1532,
      "totalTokens": 245600,
      "language": "zh",
      "createdAt": "2024-01-01T10:00:00",
      "lastUsed": "2024-01-10T15:30:00",
      "usageCount": 156,
      "averageResponseTime": 0.8
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 5,
  "totalPages": 1,
  "first": true,
  "last": true
}
```
  "status": "processing",
  "preview": {
    "materialCount": 3,
    "estimatedChunks": 1500,
    "subjects": ["极限理论", "微分学", "积分学", "级数理论"],
    "processingSteps": [
      "文档解析与预处理",
      "语义分块与向量化",
      "知识图谱构建",
      "索引优化"
    ]
  }
}
```

### 6.2 学术知识检索

**接口描述：** 在高等教育RAG知识库中进行语义搜索和知识检索

**请求信息：**
- **URL：** `GET /api/knowledge/search`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 查询内容 | "微积分基本定理的几何意义" |
| knowledgeBaseId | String | 否 | 知识库ID | "kb_123456" |
| subject | String | 否 | 学科范围 | "高等数学" |
| searchType | String | 否 | 搜索类型 | "semantic/keyword/hybrid" |
| limit | Integer | 否 | 返回结果数量 | 10 |
| minRelevance | Float | 否 | 最小相关度阈值 | 0.7 |
| includeProofs | Boolean | 否 | 是否包含证明内容 | true |
| courseLevel | String | 否 | 课程层次筛选 | "本科/研究生" |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "results": [
    {
      "id": "chunk_123456",
      "content": "微积分基本定理揭示了微分与积分之间的根本联系。从几何角度看，该定理表明函数图像下方的面积（定积分）与该函数的原函数（不定积分）之间存在直接关系...",
      "source": "《数学分析》第四版 - 华东师范大学",
      "chapter": "第五章 定积分",
      "section": "5.3 微积分基本定理",
      "pageNumber": 156,
      "relevanceScore": 0.96,
      "knowledgePoints": ["微积分基本定理", "几何意义", "牛顿-莱布尼茨公式"],
      "difficulty": "intermediate",
      "courseLevel": "本科",
      "relatedConcepts": ["定积分", "原函数", "连续函数"],
      "hasProof": true,
      "citations": 15
    },
    {
      "id": "chunk_789012",
      "content": "从历史角度来看，牛顿和莱布尼茨独立发现了微积分基本定理，这一发现被认为是数学史上最重要的成就之一...",
      "source": "《微积分的历史》- 学术论文集",
      "relevanceScore": 0.88,
      "knowledgePoints": ["数学史", "牛顿", "莱布尼茨"],
      "difficulty": "basic",
      "courseLevel": "本科"
    }
  ],
  "total": 8,
  "query": "微积分基本定理的几何意义",
  "searchTime": 0.23,
  "suggestions": [
    "微积分基本定理证明",
    "定积分的几何意义",
    "牛顿-莱布尼茨公式应用"
  ],
  "relatedQueries": [
    "定积分与面积的关系",
    "原函数的存在性",
    "连续函数的性质"
  ]
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
