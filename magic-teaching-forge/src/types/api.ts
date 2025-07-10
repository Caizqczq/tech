
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

// 社区类型
export interface CommunityPost {
  id: string;
  title: string;
  content: string;
  author: User;
  category: string;
  tags: string[];
  likes: number;
  comments: number;
  isLiked: boolean;
  createdAt: string;
}

export interface Comment {
  id: string;
  postId: string;
  content: string;
  author: User;
  likes: number;
  isLiked: boolean;
  createdAt: string;
}
