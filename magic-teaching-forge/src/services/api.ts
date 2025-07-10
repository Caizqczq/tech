
import { ApiResponse, User, LoginRequest, RegisterRequest, Project, CreateProjectRequest, TeachingResource, DashboardStats, AnalyticsData, KnowledgeItem, CommunityPost, Comment } from '@/types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api';

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

  // 社区
  async getCommunityPosts(category?: string, page = 1): Promise<ApiResponse<{ posts: CommunityPost[]; total: number }>> {
    const params = new URLSearchParams({ page: page.toString() });
    if (category) params.append('category', category);
    return this.request(`/community/posts?${params.toString()}`);
  }

  async getCommunityPost(id: string): Promise<ApiResponse<CommunityPost>> {
    return this.request(`/community/posts/${id}`);
  }

  async createCommunityPost(postData: { title: string; content: string; category: string; tags: string[] }): Promise<ApiResponse<CommunityPost>> {
    return this.request('/community/posts', {
      method: 'POST',
      body: JSON.stringify(postData),
    });
  }

  async likeCommunityPost(id: string): Promise<ApiResponse<null>> {
    return this.request(`/community/posts/${id}/like`, { method: 'POST' });
  }

  async getPostComments(postId: string): Promise<ApiResponse<Comment[]>> {
    return this.request(`/community/posts/${postId}/comments`);
  }

  async createComment(postId: string, content: string): Promise<ApiResponse<Comment>> {
    return this.request(`/community/posts/${postId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    });
  }

  // 文件上传
  async uploadFile(file: File, type: 'project' | 'knowledge' | 'avatar'): Promise<ApiResponse<{ url: string }>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    return this.request('/upload', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }
}

export const apiService = new ApiService();
