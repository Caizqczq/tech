
// API响应类型定义 - 根据接口文档标准格式
export interface ApiResponse<T> {
  data?: T;
  message?: string;
  timestamp?: string;
  status?: number;
  error?: string;
  path?: string;
}

// 用户相关类型
export interface User {
  id: number;
  username: string;
  email: string;
  avatar?: string;
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: 'teacher' | 'admin';
  subject?: string;
  institution?: string;
}

// 项目相关类型
export interface Project {
  id: string;
  title: string;
  description?: string;
  subject: string;
  grade: string;
  style: 'professional' | 'interactive' | 'inspiring';
  sourceType: 'topic' | 'text' | 'url' | 'file';
  sourceContent: string;
  outputs: string[];
  status: 'draft' | 'generating' | 'completed' | 'failed';
  progress: number;
  userId: string;
  createdAt: string;
  updatedAt: string;
  thumbnail?: string;
}

export interface CreateProjectRequest {
  title: string;
  description?: string;
  subject: string;
  grade: string;
  style: string;
  sourceType: string;
  sourceContent: string;
  outputs: string[];
}

// 教学资源类型
export interface TeachingResource {
  id: string;
  projectId: string;
  type: 'lesson_plan' | 'ppt' | 'images' | 'quiz';
  title: string;
  content: any;
  fileUrl?: string;
  createdAt: string;
}

// 统计数据类型
export interface DashboardStats {
  totalProjects: number;
  monthlyCreated: number;
  activeStudents: number;
  teachingEffectiveness: number;
}

// 分析数据类型
export interface AnalyticsData {
  overview: {
    totalProjects: number;
    completionRate: number;
    averageScore: number;
    activeUsers: number;
  };
  trends: Array<{
    date: string;
    projects: number;
    completions: number;
  }>;
  engagement: Array<{
    level: string;
    count: number;
  }>;
  subjects: Array<{
    subject: string;
    score: number;
  }>;
  recentActivities: Array<{
    id: string;
    type: string;
    description: string;
    timestamp: string;
    user: string;
  }>;
}

// 知识库类型
export interface KnowledgeItem {
  id: string;
  title: string;
  content: string;
  category: string;
  tags: string[];
  author: string;
  downloads: number;
  rating: number;
  createdAt: string;
}



// AI生成任务类型
export interface AIGenerationTask {
  id: string;
  type: 'ppt' | 'quiz' | 'explanation';
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  input: any;
  output?: any;
  error?: string;
  createdAt: string;
  completedAt?: string;
}

// 多模态素材上传类型
export interface MaterialUpload {
  id: string;
  fileName: string;
  fileType: 'document' | 'image' | 'audio';
  fileSize: number;
  fileUrl: string;
  processedContent?: string; // 处理后的文本内容
  metadata?: {
    subject?: string;
    grade?: string;
    tags?: string[];
    extractedText?: string; // OCR或语音识别结果
  };
  status: 'uploaded' | 'processing' | 'processed' | 'failed';
  createdAt: string;
}

// RAG知识库类型
export interface KnowledgeBaseItem {
  id: string;
  title: string;
  content: string;
  source: string; // 来源文件
  subject: string;
  grade: string;
  tags: string[];
  embedding?: number[]; // 向量嵌入
  similarity?: number; // 相似度分数
  createdAt: string;
}

// 教学资源分类类型
export interface ResourceCategory {
  id: string;
  name: string;
  subject: string;
  grade: string;
  knowledgePoints: string[];
  resourceCount: number;
}

// 对话消息
export interface ConversationMessage {
  messageId: string;
  role: string;
  content: string;
  timestamp: string;
}

// 对话详情
export interface ConversationDetail {
  conversationId: string;
  title: string;
  messages: ConversationMessage[];
  createdAt: string;
  updatedAt: string;
}

// 对话列表项
export interface ConversationItem {
  conversationId: string;
  title: string;
  lastMessage: string;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
}

// 教学资源
export interface TeachingResourceItem {
  resourceId: string;
  fileName: string;
  category: string;
  description?: string;
  fileSize: number;
  uploadTime: string;
  downloadCount: number;
}

// 知识库管理项
export interface KnowledgeBaseManagementItem {
  knowledgeBaseId: string;
  name: string;
  description?: string;
  resourceCount: number;
  status: 'active' | 'inactive' | 'pending';
  createdAt: string;
  updatedAt: string;
}

// AI生成任务响应
export interface AIGenerationTaskResponse {
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

// 使用统计
export interface UsageStats {
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
}

// 分页信息
export interface PaginationInfo {
  page: number;
  size: number;
  total: number;
  totalPages: number;
}
