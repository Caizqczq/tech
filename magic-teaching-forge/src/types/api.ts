
// API响应类型定义
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  code?: number;
}

// 用户相关类型
export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  role: 'teacher' | 'admin';
  subject?: string;
  institution?: string;
  verified: boolean;
  createdAt: string;
  updatedAt: string;
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

// AI对话类型
export interface ChatMessage {
  id: string;
  content: string;
  role: 'user' | 'assistant';
  timestamp: string;
  conversationId?: string;
  imageUrl?: string;
  usage?: {
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
  };
}

export interface ChatConversation {
  id: string;
  title: string;
  messages: ChatMessage[];
  createdAt: string;
  updatedAt: string;
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

// 图片分析结果类型
export interface ImageAnalysisResult {
  content: string;
  imageUrl?: string;
  fileName?: string;
  fileSize?: number;
  usage?: {
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
  };
  responseTime: string;
}

// 教学建议响应类型
export interface TeachingAdviceResponse {
  response: string;
  suggestions: string[];
  timestamp: string;
}

// 内容分析响应类型
export interface ContentAnalysisResponse {
  contentSummary: string;
  keyPoints: string[];
  difficultyLevel: string;
  suggestedTeachingTime: string;
  prerequisites: string[];
  teachingSuggestions: string[];
  timestamp: string;
}

// 写作辅助响应类型
export interface WritingAssistanceResponse {
  improvedContent: string;
  improvements: Array<{
    area: string;
    suggestion: string;
  }>;
  additionalSuggestions: string[];
  timestamp: string;
}

// 智能助手响应类型
export interface AssistantResponse {
  response: string;
  conversationId: string;
  messageId: string;
  suggestions: string[];
  relatedTopics: string[];
  assistantMode: string;
  timestamp: string;
}
