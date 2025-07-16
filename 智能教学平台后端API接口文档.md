# 高等教育智能教学平台API接口文档

## 接口规范

### 基础信息
- **接口协议：** HTTP/HTTPS
- **数据格式：** JSON
- **字符编码：** UTF-8
- **接口版本：** v1.0
- **基础URL：** `http://localhost:8082`
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

## 2. 基础AI对话模块

### 2.1 简单对话接口

**接口描述：** 基于Spring AI Alibaba ChatClient的简单对话接口，支持会话记忆功能

**请求信息：**
- **URL：** `GET /api/simple/chat`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 否 | 用户问题，默认"你好,能简单介绍一下自己吗" | "请解释微积分中导数的几何意义" |
| chat-id | String | 否 | 会话ID，保持对话连续性，默认"1" | "chat_123456" |

**请求示例：**
```
GET /api/simple/chat?query=请解释微积分中导数的几何意义&chat-id=chat_123456
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": "导数的几何意义是函数图像在某一点处的切线斜率。具体来说：\n1. 对于函数f(x)在点x₀处的导数f'(x₀)，表示函数图像在点(x₀, f(x₀))处的切线斜率\n2. 这个斜率反映了函数在该点的瞬时变化率\n3. 如果导数为正，函数在该点递增；如果导数为负，函数在该点递减\n4. 导数的绝对值越大，函数变化越剧烈",
  "conversationId": "chat_123456",
  "usage": {
    "promptTokens": 25,
    "completionTokens": 156,
    "totalTokens": 181
  },
  "responseTime": "2024-01-01T10:00:00"
}
```

### 2.2 流式对话接口

**接口描述：** 支持流式响应的对话接口，实时返回生成内容

**请求信息：**
- **URL：** `GET /api/stream/chat`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 否 | 用户问题，默认"你好" | "请解释微积分基本定理" |
| chat-id | String | 否 | 会话ID，默认"1" | "chat_123456" |

**成功响应：** (流式文本响应)
```
HTTP/1.1 200 OK
Content-Type: text/event-stream; charset=utf-8
Cache-Control: no-cache
Connection: keep-alive

导数的几何意义是函数图像在某一点处的切线斜率。具体来说...
```

### 2.3 图片分析接口 - URL方式

**接口描述：** 基于Spring AI Alibaba多模态模型，通过图片URL进行图像内容分析

**请求信息：**
- **URL：** `POST /api/image/analyze/url`
- **Content-Type：** `application/x-www-form-urlencoded`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| prompt | String | 否 | 分析提示词，默认"请分析这张图片的内容" | "请分析这张数学图表的内容" |
| imageUrl | String | 是 | 图片URL地址 | "https://example.com/chart.jpg" |

**请求示例：**
```
POST /api/image/analyze/url
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

prompt=请分析这张数学图表的内容&imageUrl=https://example.com/chart.jpg
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": "这是一张关于函数y=x²的图像，显示了抛物线的特征：\n1. 开口向上的抛物线\n2. 顶点在原点(0,0)\n3. 关于y轴对称\n4. 函数值在x=0处达到最小值0",
  "imageUrl": "https://example.com/chart.jpg",
  "usage": {
    "promptTokens": 15,
    "completionTokens": 89,
    "totalTokens": 104
  },
  "responseTime": "2024-01-01T10:00:00"
}
```

**错误响应：**
```json
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "图片分析失败: 无法访问指定的图片URL",
  "path": "/api/image/analyze/url"
}
```

### 2.4 图片分析接口 - 文件上传方式

**接口描述：** 基于Spring AI Alibaba多模态模型，通过文件上传进行图像内容分析

**请求信息：**
- **URL：** `POST /api/image/analyze/upload`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 图片文件 | 支持图片格式，最大10MB |
| prompt | String | 否 | 分析提示词，默认"请分析这张图片的内容" | "请详细分析这张数学图表" |

**请求示例：**
```
POST /api/image/analyze/upload
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
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": "这是一张数学函数图像，包含以下要素：\n1. 坐标系：标准的笛卡尔坐标系，有x轴和y轴\n2. 函数曲线：红色的抛物线，方程为y=x²\n3. 特征点：顶点在原点(0,0)，开口向上\n4. 对称性：关于y轴对称\n5. 适用于高等数学函数课程教学",
  "fileName": "math_chart.jpg",
  "fileSize": 2048576,
  "usage": {
    "promptTokens": 18,
    "completionTokens": 124,
    "totalTokens": 142
  },
  "responseTime": "2024-01-01T10:00:00"
}
```

**错误响应：**
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "timestamp": "2024-01-01T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "请上传图片文件",
  "path": "/api/image/analyze/upload"
}
```

## 3. 教学AI对话模块

### 3.1 教学建议接口

**接口描述：** 基于TeachingChatController的教学建议功能，为教师提供专业的教学方法建议

**请求信息：**
- **URL：** `POST /api/chat/teaching-advice`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 教学问题或主题 | "如何更好地教授线性代数中的矩阵运算" |
| subject | String | 否 | 学科 | "数学" |
| courseLevel | String | 否 | 课程层次 | "本科" |
| teachingType | String | 否 | 教学类型 | "理论/实践/混合" |
| currentContext | String | 否 | 当前教学背景 | "学生基础薄弱" |
| mode | String | 否 | 模式 | "详细/简洁" |

**请求示例：**
```json
{
  "query": "如何更好地教授线性代数中的矩阵运算，学生总是觉得很抽象",
  "subject": "数学",
  "courseLevel": "本科",
  "teachingType": "理论",
  "currentContext": "学生基础薄弱",
  "mode": "详细"
}
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "response": "理解您的困扰！矩阵运算确实是线性代数中学生感到抽象的部分。我建议采用以下几种教学策略：\n\n1. **几何可视化**：使用图形展示矩阵变换的几何意义\n2. **实际应用导入**：从图像处理、经济模型等实际问题引入\n3. **分步骤教学**：先教2×2矩阵，再逐步扩展\n4. **动手计算**：让学生多做手工计算，建立直觉",
  "suggestions": [
    "详细了解几何可视化方法",
    "获取矩阵运算的实际应用案例",
    "查看相关教学资源推荐"
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

### 3.2 课程内容分析接口

**接口描述：** 基于TeachingChatController，分析教学内容并提供建议

**请求信息：**
- **URL：** `POST /api/chat/content-analysis`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| content | String | 是 | 需要分析的教学内容 | "微积分基本定理的教学内容..." |
| analysisType | String | 是 | 分析类型 | "summary/difficulty/structure" |
| subject | String | 否 | 学科领域 | "数学" |
| courseLevel | String | 否 | 课程层次 | "本科" |
| analysisScope | String | 否 | 分析范围 | "全面分析/重点分析/概要分析" |
| targetAudience | String | 否 | 目标受众 | "本科生/研究生/教师" |

**请求示例：**
```json
{
  "content": "微积分基本定理是连接微分和积分的重要定理...",
  "analysisType": "summary",
  "subject": "数学",
  "courseLevel": "本科",
  "analysisScope": "全面分析",
  "targetAudience": "本科生"
}
```

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

### 3.3 学术写作辅助接口

**接口描述：** 为教师提供学术论文、教案等写作建议

**请求信息：**
- **URL：** `POST /api/chat/writing-assistance`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| content | String | 是 | 需要改进的文本内容 | "本课程旨在介绍微积分的基本概念..." |
| assistanceType | String | 是 | 辅助类型 | "grammar/style/structure/academic_tone" |
| writingType | String | 否 | 写作类型 | "lesson_plan/paper/syllabus" |
| subject | String | 否 | 学科领域 | "数学" |
| targetAudience | String | 否 | 目标受众 | "undergraduate/graduate" |
| language | String | 否 | 语言 | "zh/en" |
| additionalRequirements | String | 否 | 额外要求 | "增强学术性/提高可读性" |

**请求示例：**
```json
{
  "content": "本课程旨在介绍微积分的基本概念，包括极限、导数和积分。学生将学习这些概念的定义和应用。",
  "assistanceType": "structure",
  "writingType": "lesson_plan",
  "subject": "数学",
  "targetAudience": "undergraduate",
  "language": "zh",
  "additionalRequirements": "增强学术性和逻辑性"
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

### 3.4 智能对话助手接口

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
| subject | String | 否 | 当前讨论的学科 | "线性代数" |
| courseLevel | String | 否 | 课程层次 | "本科" |
| streamMode | String | 否 | 流式模式 | "normal/detailed" |
| contextInfo | String | 否 | 上下文信息 | "当前正在讨论矩阵运算教学方法" |

**请求示例：**
```json
{
  "message": "我想了解如何更好地教授线性代数中的矩阵运算，学生总是觉得很抽象",
  "conversationId": "conv_123456",
  "assistantMode": "teaching",
  "subject": "线性代数",
  "courseLevel": "本科",
  "streamMode": "detailed",
  "contextInfo": "当前正在讨论如何提高学生对抽象概念的理解"
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

### 3.5 流式智能对话助手接口

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
| subject | String | 否 | 当前讨论的学科 | "高等数学" |
| courseLevel | String | 否 | 课程层次 | "本科" |
| streamMode | String | 否 | 流式模式 | "normal/detailed" |
| contextInfo | String | 否 | 上下文信息 | "正在学习优化理论相关内容" |

**请求示例：**
```json
{
  "message": "请解释一下拉格朗日乘数法的原理，并给出一个具体例子",
  "conversationId": "conv_123456",
  "assistantMode": "teaching",
  "subject": "高等数学",
  "courseLevel": "本科",
  "streamMode": "detailed",
  "contextInfo": "正在学习多元函数优化理论"
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

## 4. 对话管理模块

### 4.1 获取对话列表

**接口描述：** 获取用户的对话列表，支持分页和多维度筛选

**请求信息：**
- **URL：** `GET /api/chat/conversations`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| limit | Integer | 否 | 每页数量，默认10，最大100 | 10 |
| scenario | String | 否 | 场景筛选 | "teaching/general" |
| startDate | String | 否 | 开始日期 | "2024-01-01T00:00:00" |
| endDate | String | 否 | 结束日期 | "2024-01-31T23:59:59" |

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

### 4.2 获取对话详情

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

### 4.3 删除对话

**接口描述：** 删除指定的对话记录

**请求信息：**
- **URL：** `DELETE /api/chat/conversations/{conversationId}`
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
  "message": "对话删除成功",
  "conversationId": "conv_123456"
}
```

### 4.4 清空所有对话

**接口描述：** 清空用户的所有对话记录

**请求信息：**
- **URL：** `DELETE /api/chat/conversations`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "所有对话已清空"
}
```

### 4.5 更新对话标题

**接口描述：** 更新指定对话的标题

**请求信息：**
- **URL：** `PUT /api/chat/conversations/{conversationId}/title`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | String | 是 | 对话ID |

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| title | String | 是 | 新标题 | "线性代数教学讨论" |

**请求示例：**
```json
{
  "title": "线性代数教学讨论"
}
```

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "对话标题更新成功",
  "conversationId": "conv_123456",
  "newTitle": "线性代数教学讨论"
}
```

### 4.6 获取对话统计信息

**接口描述：** 获取用户的对话统计信息

**请求信息：**
- **URL：** `GET /api/chat/conversations/statistics`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "totalConversations": 25,
  "activeConversations": 15,
  "totalMessages": 350,
  "todayMessages": 12,
  "averageMessagesPerConversation": 14.0
}
```

## 5. 智能教学资源管理模块

### 5.1 教学资源上传

#### 5.1.1 学术文档上传

**接口描述：** 基于Spring Boot和Spring AI Alibaba的智能文档上传接口，支持教案、论文、课件等学术资料的上传和智能解析

**请求信息：**
- **URL：** `POST /api/resources/upload/document`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 文档文件 | 支持doc/docx/pdf/pptx/md/txt，最大50MB |
| subject | String | 是 | 学科分类 | 如：高等数学、线性代数、概率论等 |
| courseLevel | String | 是 | 课程层次 | undergraduate/graduate/doctoral |
| resourceType | String | 是 | 资源类型 | lesson_plan/syllabus/paper/textbook/exercise |
| title | String | 否 | 资源标题 | 最大100字符 |
| description | String | 否 | 资源描述 | 最大500字符 |
| keywords | String | 否 | 关键词标签（逗号分隔） | 如："导数,微积分,极限" |
| autoVectorize | Boolean | 否 | 是否自动向量化 | 默认true |
| autoExtractKeywords | Boolean | 否 | 是否自动提取关键词 | 默认true |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "res_123456",
  "filename": "高等数学导数教案.docx",
  "originalName": "calculus_derivative_lesson.docx",
  "subject": "高等数学",
  "courseLevel": "undergraduate",
  "resourceType": "lesson_plan",
  "size": 2048000,
  "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "keywords": ["导数", "微积分", "极限"],
  "extractedKeywords": ["函数", "连续性", "可导性"],
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "/api/resources/res_123456/download",
  "isVectorized": true,
  "processingStatus": "completed"
}
```

#### 5.1.2 学术语音上传及转文字

**接口描述：** 基于Spring AI Alibaba AudioTranscriptionModel的语音上传和转文字功能

**请求信息：**
- **URL：** `POST /api/resources/upload/audio`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| file | File | 是 | 语音文件 | 支持mp3/wav/m4a/flac，最大100MB |
| transcriptionMode | String | 否 | 转录模式 | sync/async/stream，默认sync |
| needTranscription | Boolean | 否 | 是否需要语音转文字 | 默认true |
| subject | String | 否 | 学科分类 | - |
| resourceType | String | 否 | 资源类型 | lecture/seminar/discussion/interview |
| description | String | 否 | 语音描述 | 最大200字符 |
| speaker | String | 否 | 主讲人 | 教授姓名或职称 |
| language | String | 否 | 语言 | zh/en，默认zh |
| autoVectorize | Boolean | 否 | 是否自动向量化转录文本 | 默认true |

**同步转录成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "res_123456",
  "filename": "calculus_lecture.mp3",
  "originalName": "微积分专题讲座.mp3",
  "subject": "高等数学",
  "resourceType": "lecture",
  "description": "微积分基本定理专题讲座",
  "speaker": "张教授",
  "duration": 3600,
  "size": 25600000,
  "language": "zh",
  "uploadedAt": "2024-01-01T10:00:00",
  "downloadUrl": "/api/resources/res_123456/download",
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
  "keyPoints": ["微积分基本定理", "牛顿-莱布尼茨公式", "定积分计算"],
  "isVectorized": true,
  "processingStatus": "completed"
}
```

#### 5.1.3 批量资源上传

**接口描述：** 批量上传多个教学资源文件

**请求信息：**
- **URL：** `POST /api/resources/upload/batch`
- **Content-Type：** `multipart/form-data`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 限制 |
|--------|------|------|------|------|
| files | File[] | 是 | 资源文件列表 | 最多10个文件 |
| subject | String | 是 | 学科分类 | 应用于所有文件 |
| courseLevel | String | 是 | 课程层次 | 应用于所有文件 |
| autoVectorize | Boolean | 否 | 是否自动向量化 | 默认true |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "batchId": "batch_123456",
  "totalFiles": 5,
  "successCount": 4,
  "failedCount": 1,
  "results": [
    {
      "filename": "lesson1.pdf",
      "status": "success",
      "resourceId": "res_123456"
    },
    {
      "filename": "lesson2.docx",
      "status": "failed",
      "error": "文件格式不支持"
    }
  ],
  "processingStatus": "completed"
}
```

### 5.2 资源管理与检索

#### 5.2.1 分页查询教学资源

**接口描述：** 分页查询用户的教学资源，支持多维度筛选和排序

**请求信息：**
- **URL：** `GET /api/resources`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| resourceType | String | 否 | 资源类型筛选 | lesson_plan/paper/textbook/lecture |
| subject | String | 否 | 学科分类筛选 | "高等数学" |
| courseLevel | String | 否 | 课程层次筛选 | undergraduate/graduate/doctoral |
| keywords | String | 否 | 关键词搜索 | "微积分" |
| page | Integer | 否 | 页码，从0开始 | 0 |
| size | Integer | 否 | 每页数量，默认20 | 20 |
| sort | String | 否 | 排序字段 | createdAt,desc |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": [
    {
      "id": "res_123456",
      "title": "微积分基础教案",
      "subject": "高等数学",
      "courseLevel": "undergraduate",
      "resourceType": "lesson_plan",
      "fileSize": 2048576,
      "keywords": ["导数", "微积分", "极限"],
      "isVectorized": true,
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

#### 5.2.2 语义搜索资源

**接口描述：** 基于Spring AI Alibaba VectorStore的语义搜索功能

**请求信息：**
- **URL：** `GET /api/resources/search/semantic`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 搜索查询 | "微积分基本定理的几何意义" |
| subject | String | 否 | 学科范围 | "高等数学" |
| courseLevel | String | 否 | 课程层次 | "undergraduate" |
| topK | Integer | 否 | 返回结果数量 | 10 |
| threshold | Double | 否 | 相似度阈值 | 0.7 |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "results": [
    {
      "resource": {
        "id": "res_123456",
        "title": "微积分基本定理详解",
        "subject": "高等数学",
        "resourceType": "lesson_plan"
      },
      "similarity": 0.96,
      "relevantContent": "微积分基本定理揭示了微分与积分之间的根本联系..."
    }
  ],
  "query": "微积分基本定理的几何意义",
  "totalResults": 5,
  "searchTime": 0.23
}
```

#### 5.2.3 获取资源详情

**接口描述：** 获取指定教学资源的详细信息

**请求信息：**
- **URL：** `GET /api/resources/{resourceId}`
- **认证：** 需要JWT Token

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resourceId | String | 是 | 资源ID |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "res_123456",
  "title": "微积分基本定理专题讲座",
  "description": "深入讲解微积分基本定理的数学表述、几何意义和实际应用",
  "subject": "高等数学",
  "courseLevel": "undergraduate",
  "resourceType": "lecture",
  "originalName": "微积分讲座录音.mp3",
  "fileSize": 52428800,
  "contentType": "audio/mpeg",
  "keywords": ["微积分", "基本定理", "导数", "积分"],
  "extractedKeywords": ["函数", "连续性", "可导性"],
  "downloadUrl": "/api/resources/res_123456/download",
  "transcriptionText": "今天我们来深入探讨微积分基本定理...",
  "isVectorized": true,
  "knowledgeBaseIds": ["kb_123456"],
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:30:00"
}
```

### 5.3 智能知识库管理

#### 5.3.1 构建知识库

**接口描述：** 基于Spring AI Alibaba VectorStore构建专业知识库

**请求信息：**
- **URL：** `POST /api/resources/knowledge-base`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 知识库名称 | "高等数学本科知识库" |
| description | String | 否 | 知识库描述 | "涵盖微积分、级数、多元函数等核心内容" |
| resourceIds | String[] | 是 | 资源ID列表 | ["res_123", "res_456"] |
| subject | String | 是 | 学科领域 | "高等数学" |
| courseLevel | String | 是 | 课程层次 | "undergraduate" |
| vectorStore | String | 否 | 向量存储类型 | "elasticsearch/milvus/redis" |
| chunkSize | Integer | 否 | 分块大小 | 1000 |
| chunkOverlap | Integer | 否 | 分块重叠 | 200 |

**成功响应：**
```json
HTTP/1.1 202 Accepted
Content-Type: application/json

{
  "knowledgeBaseId": "kb_123456",
  "taskId": "task_123456",
  "message": "知识库构建任务已启动",
  "estimatedTime": 300,
  "status": "processing",
  "resourceCount": 15,
  "statusUrl": "/api/resources/knowledge-base/kb_123456/status"
}
```

#### 5.3.2 知识库状态查询

**接口描述：** 查询知识库构建状态

**请求信息：**
- **URL：** `GET /api/resources/knowledge-base/{knowledgeBaseId}/status`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "knowledgeBaseId": "kb_123456",
  "status": "completed",
  "progress": 100,
  "resourceCount": 15,
  "chunkCount": 1532,
  "message": "知识库构建完成",
  "createdAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-01T10:05:00"
}
```

#### 5.3.3 知识库列表

**接口描述：** 获取用户的知识库列表

**请求信息：**
- **URL：** `GET /api/resources/knowledge-base`
- **认证：** 需要JWT Token

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
      "status": "active",
      "resourceCount": 15,
      "chunkCount": 1532,
      "createdAt": "2024-01-01T10:00:00",
      "lastUsed": "2024-01-10T15:30:00"
    }
  ],
  "totalElements": 5
}
```

### 5.4 智能问答服务

#### 5.4.1 基于RAG的智能问答

**接口描述：** 基于Spring AI Alibaba QuestionAnswerAdvisor的增强问答功能

**请求信息：**
- **URL：** `POST /api/resources/qa`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 用户问题 | "请详细解释微积分基本定理" |
| knowledgeBaseId | String | 否 | 知识库ID | "kb_123456" |
| conversationId | String | 否 | 会话ID | "conv_123456" |
| answerMode | String | 否 | 回答模式 | "detailed/concise/tutorial" |
| includeReferences | Boolean | 否 | 是否包含参考来源 | true |
| topK | Integer | 否 | 检索文档数量 | 5 |

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "answer": "微积分基本定理是微积分学中最重要的定理之一，它建立了微分与积分之间的根本联系...",
  "conversationId": "conv_123456",
  "messageId": "msg_789012",
  "references": [
    {
      "resourceId": "res_123456",
      "title": "数学分析第四版",
      "relevanceScore": 0.96,
      "excerpt": "微积分基本定理揭示了微分与积分之间的根本联系..."
    }
  ],
  "relatedQuestions": [
    "牛顿-莱布尼茨公式是如何推导的？",
    "微积分基本定理的第二形式是什么？"
  ],
  "processingTime": 1.2,
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 5.4.2 流式智能问答

**接口描述：** 流式RAG问答接口，实时返回生成内容

**请求信息：**
- **URL：** `POST /api/resources/qa/stream`
- **Content-Type：** `application/json`
- **认证：** 需要JWT Token

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| query | String | 是 | 用户问题 | "请解释拉格朗日乘数法的原理" |
| knowledgeBaseId | String | 否 | 知识库ID | "kb_123456" |
| conversationId | String | 否 | 会话ID | "conv_123456" |

**成功响应：** (Server-Sent Events格式)
```
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive

data: {"type": "start", "conversationId": "conv_123456"}

data: {"type": "content", "content": "拉格朗日乘数法是"}

data: {"type": "content", "content": "解决约束优化问题的重要方法..."}

data: {"type": "references", "references": [...]}

data: {"type": "end", "timestamp": "2024-01-01T10:00:00"}
```

### 5.5 资源操作管理

#### 5.5.1 删除教学资源

**接口描述：** 删除指定的教学资源

**请求信息：**
- **URL：** `DELETE /api/resources/{resourceId}`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "资源删除成功",
  "resourceId": "res_123456"
}
```

#### 5.5.2 获取资源下载链接

**接口描述：** 获取教学资源的临时下载链接

**请求信息：**
- **URL：** `GET /api/resources/{resourceId}/download`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "resourceId": "res_123456",
  "downloadUrl": "https://tech-czq.oss-cn-beijing.aliyuncs.com/...",
  "expiresIn": 3600
}
```

#### 5.5.3 资源统计信息

**接口描述：** 获取用户的教学资源统计信息

**请求信息：**
- **URL：** `GET /api/resources/statistics`
- **认证：** 需要JWT Token

**成功响应：**
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "totalResources": 25,
  "typeStatistics": {
    "lesson_plan": 10,
    "paper": 8,
    "lecture": 7
  },
  "subjectStatistics": [
    {
      "subject": "高等数学",
      "count": 12
    },
    {
      "subject": "线性代数",
      "count": 8
    }
  ],
  "knowledgeBaseCount": 3,
  "vectorizedResources": 20,
  "todayUploads": 3
}
```

## 6. [规划中] AI资源自动制作模块

### 6.1 生成学术PPT课件

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

### 6.2 生成学术习题

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

### 6.3 生成学术讲解文本

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
## 7. 任务管理模块

### 7.1 查询任务状态

**接口描述：** 查询异步任务的执行状态

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
  "taskId": "task_123456",
  "status": "completed",
  "result": "任务执行结果数据",
  "message": "任务执行成功",
  "timestamp": "2024-01-01T10:00:00"
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
  "message": "任务不存在",
  "path": "/api/tasks/task_123456/status"
}
```

## 接口实现技术说明

### Spring AI Alibaba 核心组件

本接口文档基于Spring AI Alibaba框架设计，充分利用其丰富的AI能力组件：

#### 1. 音频处理组件
- **AudioTranscriptionModel**: 语音转文字核心模型，支持同步/异步/流式转录
- **AudioSpeechModel**: 语音合成模型，支持多种语音风格和语言
- **实现示例**:
```java
// 语音转文字
AudioTranscriptionResponse response = transcriptionModel.call(
    new AudioTranscriptionPrompt(
        new FileSystemResource("audio.mp3"),
        DashScopeAudioTranscriptionOptions.builder()
            .withModel("paraformer-realtime-v2")
            .withLanguage("zh")
            .build()
    )
);
```

#### 2. 多模态处理组件
- **Multi-modal ChatModel**: 支持图像、视频、音频等多模态输入
- **Media处理**: 统一的媒体文件处理接口
- **实现示例**:
```java
// 图像分析
List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_PNG, imageResource));
UserMessage message = UserMessage.builder()
    .text("请分析这张图片的内容")
    .media(mediaList)
    .build();
```

#### 3. 向量存储组件
- **VectorStore**: 支持多种向量数据库（Elasticsearch、Milvus、Redis等）
- **DocumentReader**: 支持多种文档格式解析（PDF、DOC、HTML、Markdown等）
- **TokenTextSplitter**: 智能文档分块处理
- **实现示例**:
```java
// 构建向量存储
List<Document> documents = documentReader.read();
List<Document> splitDocuments = tokenTextSplitter.apply(documents);
vectorStore.add(splitDocuments);
```

#### 4. RAG增强组件
- **QuestionAnswerAdvisor**: RAG问答增强器
- **RetrievalRerankAdvisor**: 检索重排序增强器
- **ChatMemoryAdvisor**: 对话记忆增强器
- **实现示例**:
```java
// RAG增强的ChatClient
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(
        QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(SearchRequest.builder().topK(5).build())
            .build()
    )
    .build();
```

### 技术架构优势

#### 1. 统一的API设计
- 所有AI功能通过统一的Spring Boot接口暴露
- 标准的HTTP RESTful API设计
- 完善的错误处理和状态码管理

#### 2. 异步处理能力
- 支持同步、异步、流式三种处理模式
- 完善的任务状态跟踪和进度管理
- 适应不同场景的性能需求

#### 3. 可扩展的存储方案
- 支持多种向量数据库后端
- 灵活的文档处理管道
- 可插拔的AI模型配置

#### 4. 智能化的内容处理
- 自动关键词提取和分类
- 智能的文档向量化
- 语义搜索和相似度计算

### 部署和配置

#### 1. 依赖配置
```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter</artifactId>
    <version>1.0.0.2</version>
</dependency>
```

#### 2. 应用配置
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        model: qwen-turbo
      audio:
        transcription:
          model: paraformer-realtime-v2
      image:
        model: qwen-vl-plus
```

#### 3. 向量存储配置
```yaml
spring:
  ai:
    vectorstore:
      elasticsearch:
        url: http://localhost:9200
        index-name: teaching-resources
      milvus:
        host: localhost
        port: 19530
        database-name: teaching_db
```

### 最佳实践建议

#### 1. 性能优化
- 合理配置文档分块大小（推荐1000-2000字符）
- 使用适当的向量存储后端（Elasticsearch适合混合搜索，Milvus适合高性能向量搜索）
- 启用缓存机制减少重复计算

#### 2. 安全考虑
- 实施严格的JWT认证和授权
- 文件上传大小和类型限制
- 敏感信息脱敏处理

#### 3. 监控和日志
- 完整的API调用日志记录
- 关键指标监控（响应时间、成功率、资源使用率）
- 异常处理和告警机制

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
