
import { ApiResponse, User, LoginRequest, RegisterRequest, Project, CreateProjectRequest, TeachingResource, DashboardStats, AnalyticsData, KnowledgeItem, ChatMessage, AIGenerationTask, MaterialUpload } from '@/types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082/api';

class ApiService {
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const token = localStorage.getItem('auth_token');
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'API request failed');
      }
      
      return data;
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  // 认证相关
  async login(credentials: LoginRequest): Promise<ApiResponse<{ user: User; token: string }>> {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  }

  async register(userData: RegisterRequest): Promise<ApiResponse<{ user: User; token: string }>> {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  async logout(): Promise<ApiResponse<null>> {
    return this.request('/auth/logout', { method: 'POST' });
  }

  async getCurrentUser(): Promise<ApiResponse<User>> {
    return this.request('/auth/me');
  }

  // AI对话相关
  async simpleChat(query: string, chatId?: string): Promise<ApiResponse<any>> {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (chatId) params.append('chat-id', chatId);
    return this.request(`/simple/chat?${params.toString()}`);
  }

  async streamChat(query: string, chatId?: string): Promise<Response> {
    const token = localStorage.getItem('auth_token');
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (chatId) params.append('chat-id', chatId);
    
    return fetch(`${API_BASE_URL}/stream/chat?${params.toString()}`, {
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    });
  }

  async analyzeImageByUrl(imageUrl: string, prompt?: string): Promise<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('imageUrl', imageUrl);
    if (prompt) formData.append('prompt', prompt);
    
    return this.request('/image/analyze/url', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async analyzeImageByUpload(file: File, prompt?: string): Promise<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('file', file);
    if (prompt) formData.append('prompt', prompt);
    
    return this.request('/image/analyze/upload', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  // 教学AI对话相关
  async getTeachingAdvice(data: {
    query: string;
    subject?: string;
    courseLevel?: string;
    teachingType?: string;
    currentContext?: string;
    mode?: string;
  }): Promise<ApiResponse<any>> {
    return this.request('/chat/teaching-advice', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async analyzeContent(data: {
    content: string;
    analysisType: string;
    subject?: string;
    courseLevel?: string;
    analysisScope?: string;
    targetAudience?: string;
  }): Promise<ApiResponse<any>> {
    return this.request('/chat/content-analysis', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getWritingAssistance(data: {
    content: string;
    assistanceType: string;
    writingType?: string;
    subject?: string;
    targetAudience?: string;
    language?: string;
    additionalRequirements?: string;
  }): Promise<ApiResponse<any>> {
    return this.request('/chat/writing-assistance', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async chatWithAssistant(data: {
    message: string;
    conversationId?: string;
    assistantMode?: string;
    subject?: string;
    courseLevel?: string;
    streamMode?: string;
    contextInfo?: string;
  }): Promise<ApiResponse<any>> {
    return this.request('/chat/assistant', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async streamChatWithAssistant(data: {
    message: string;
    conversationId?: string;
    assistantMode?: string;
    subject?: string;
    courseLevel?: string;
    streamMode?: string;
    contextInfo?: string;
  }): Promise<Response> {
    const token = localStorage.getItem('auth_token');
    
    return fetch(`${API_BASE_URL}/chat/assistant/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: JSON.stringify(data),
    });
  }

  // 项目相关
  async getProjects(page = 1, limit = 10): Promise<ApiResponse<{ projects: Project[]; total: number }>> {
    return this.request(`/projects?page=${page}&limit=${limit}`);
  }

  async getProject(id: string): Promise<ApiResponse<Project>> {
    return this.request(`/projects/${id}`);
  }

  async createProject(projectData: CreateProjectRequest): Promise<ApiResponse<Project>> {
    return this.request('/projects', {
      method: 'POST',
      body: JSON.stringify(projectData),
    });
  }

  async updateProject(id: string, projectData: Partial<Project>): Promise<ApiResponse<Project>> {
    return this.request(`/projects/${id}`, {
      method: 'PUT',
      body: JSON.stringify(projectData),
    });
  }

  async deleteProject(id: string): Promise<ApiResponse<null>> {
    return this.request(`/projects/${id}`, { method: 'DELETE' });
  }

  async generateProject(id: string): Promise<ApiResponse<{ taskId: string }>> {
    return this.request(`/projects/${id}/generate`, { method: 'POST' });
  }

  async getGenerationStatus(taskId: string): Promise<ApiResponse<{ status: string; progress: number }>> {
    return this.request(`/tasks/${taskId}/status`);
  }

  // 教学资源相关
  async getProjectResources(projectId: string): Promise<ApiResponse<TeachingResource[]>> {
    return this.request(`/projects/${projectId}/resources`);
  }

  async downloadResource(resourceId: string): Promise<Blob> {
    const response = await fetch(`${API_BASE_URL}/resources/${resourceId}/download`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
      },
    });
    return response.blob();
  }

  // 仪表盘统计
  async getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
    return this.request('/dashboard/stats');
  }

  async getRecentProjects(limit = 6): Promise<ApiResponse<Project[]>> {
    return this.request(`/dashboard/recent-projects?limit=${limit}`);
  }

  // 分析数据
  async getAnalyticsData(): Promise<ApiResponse<AnalyticsData>> {
    return this.request('/analytics');
  }

  // 知识库
  async getKnowledgeItems(category?: string, search?: string): Promise<ApiResponse<KnowledgeItem[]>> {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    if (search) params.append('search', search);
    return this.request(`/knowledge?${params.toString()}`);
  }

  async getKnowledgeItem(id: string): Promise<ApiResponse<KnowledgeItem>> {
    return this.request(`/knowledge/${id}`);
  }

  async downloadKnowledgeItem(id: string): Promise<Blob> {
    const response = await fetch(`${API_BASE_URL}/knowledge/${id}/download`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
      },
    });
    return response.blob();
  }



  // 多模态文件上传
  async uploadFile(file: File, type: 'document' | 'image' | 'audio' | 'avatar'): Promise<ApiResponse<{ url: string; fileId?: string }>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    return this.request('/upload', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  // 语音转文字
  async transcribeAudio(file: File): Promise<ApiResponse<{ text: string }>> {
    const formData = new FormData();
    formData.append('audio', file);

    return this.request('/audio/transcribe', {
      method: 'POST',
      body: formData,
      headers: {},
    });
  }

  // AI资源生成
  async generatePPT(data: {
    topic: string;
    content: string;
    slides?: number;
    style?: string;
  }): Promise<ApiResponse<{ taskId: string }>> {
    return this.request('/ai/generate/ppt', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async generateQuiz(data: {
    topic: string;
    content: string;
    questionCount?: number;
    difficulty?: string;
    questionTypes?: string[];
  }): Promise<ApiResponse<{ taskId: string }>> {
    return this.request('/ai/generate/quiz', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async generateExplanation(data: {
    topic: string;
    content: string;
    level?: string;
    style?: string;
  }): Promise<ApiResponse<{ taskId: string }>> {
    return this.request('/ai/generate/explanation', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // RAG知识库
  async uploadToKnowledgeBase(file: File, metadata?: {
    subject?: string;
    grade?: string;
    tags?: string[];
  }): Promise<ApiResponse<{ knowledgeBaseId: string }>> {
    const formData = new FormData();
    formData.append('file', file);
    if (metadata) {
      formData.append('metadata', JSON.stringify(metadata));
    }

    return this.request('/knowledge-base/upload', {
      method: 'POST',
      body: formData,
      headers: {},
    });
  }

  async searchKnowledgeBase(query: string, filters?: {
    subject?: string;
    grade?: string;
    tags?: string[];
  }): Promise<ApiResponse<any[]>> {
    const params = new URLSearchParams({ query });
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          value.forEach(v => params.append(key, v));
        } else if (value) {
          params.append(key, value);
        }
      });
    }
    return this.request(`/knowledge-base/search?${params.toString()}`);
  }
}

export const apiService = new ApiService();
