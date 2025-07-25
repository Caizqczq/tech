
import { ApiResponse, User, LoginRequest, RegisterRequest, ConversationMessage, ConversationDetail, ConversationItem, TeachingResourceItem, KnowledgeBaseItem, AIGenerationTask, UsageStats, PaginationInfo } from '@/types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082/api';

class ApiService {
  private getToken(): string | null {
    return localStorage.getItem('token');
  }

  private getHeaders(): Record<string, string> {
    const token = this.getToken();
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = this.getToken();
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
      
      if (!response.ok) {
        // 简化401处理：只在明确的登出接口或用户主动登出时才清除token
        if (response.status === 401) {
          // 不自动跳转，让用户看到错误信息后自己决定
          console.warn('Authentication failed, JWT may be expired');
        }
        
        // 尝试解析错误响应
        let errorMessage = 'API request failed';
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = `HTTP ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }
      
      // 检查响应是否为空
      const text = await response.text();
      if (!text) {
        return {} as T;
      }
      
      return JSON.parse(text);
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  // 认证相关
  async login(credentials: LoginRequest): Promise<{ user: User; token: string }> {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  }

  async register(userData: RegisterRequest): Promise<User> {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  async logout(): Promise<void> {
    return this.request('/auth/logout', { method: 'POST' });
  }

  async getCurrentUser(): Promise<User> {
    return this.request('/auth/me');
  }

  // AI对话相关
  async simpleChat(query: string = '你好,能简单介绍一下自己吗', chatId: string = '1'): Promise<{
    content: string;
    conversationId: string;
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    const params = new URLSearchParams();
    params.append('query', query);
    params.append('chat-id', chatId);
    return this.request(`/simple/chat?${params.toString()}`);
  }

  async streamChat(query: string = '你好', chatId: string = '1'): Promise<Response> {
    const token = this.getToken();
    const params = new URLSearchParams();
    params.append('query', query);
    params.append('chat-id', chatId);
    
    return fetch(`${API_BASE_URL}/stream/chat?${params.toString()}`, {
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    });
  }

  async analyzeImageByUrl(imageUrl: string, prompt: string = '请分析这张图片的内容'): Promise<{
    content: string;
    imageUrl: string;
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    const formData = new FormData();
    formData.append('prompt', prompt);
    formData.append('imageUrl', imageUrl);
    
    return this.request('/image/analyze/url', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async analyzeImageByUpload(file: File, prompt: string = '请分析这张图片的内容'): Promise<{
    content: string;
    fileName: string;
    fileSize: number;
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('prompt', prompt);
    
    return this.request('/image/analyze/upload', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  // 教学AI对话相关
  async getTeachingAdvice(request: {
    subject: string;
    grade: string;
    topic: string;
    difficulty: string;
    requirements?: string;
  }): Promise<{
    advice: string;
    suggestions: string[];
    resources: string[];
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    return this.request('/chat/teaching-advice', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async analyzeContent(request: {
    content: string;
    analysisType: string;
    requirements?: string;
  }): Promise<{
    analysis: string;
    keyPoints: string[];
    improvements: string[];
    score: number;
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    return this.request('/chat/content-analysis', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async getWritingAssistance(request: {
    content: string;
    assistanceType: string;
    requirements?: string;
  }): Promise<{
    assistance: string;
    suggestions: string[];
    corrections: string[];
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    return this.request('/chat/writing-assistance', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async chatWithAssistant(request: {
    message: string;
    conversationId?: string;
    mode?: string;
    context?: {
      subject?: string;
      grade?: string;
      topic?: string;
    };
  }): Promise<{
    response: string;
    conversationId: string;
    mode: string;
    context?: {
      subject?: string;
      grade?: string;
      topic?: string;
    };
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    return this.request('/chat/assistant', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async streamChatWithAssistant(request: {
    message: string;
    conversationId?: string;
    mode?: string;
    context?: {
      subject?: string;
      grade?: string;
      topic?: string;
    };
  }): Promise<Response> {
    const token = this.getToken();
    
    return fetch(`${API_BASE_URL}/chat/assistant/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: JSON.stringify(request),
    });
  }

  // 对话管理相关
  async getConversations(page: number = 1, size: number = 10): Promise<{
    conversations: {
      conversationId: string;
      title: string;
      lastMessage: string;
      messageCount: number;
      createdAt: string;
      updatedAt: string;
    }[];
    pagination: {
      page: number;
      size: number;
      total: number;
      totalPages: number;
    };
  }> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    return this.request(`/chat/conversations?${params.toString()}`); // 添加 /chat 前缀
  }

  // 获取对话详情
  async getConversationDetail(conversationId: string): Promise<{
    conversation: any;
    messages: Array<{
      role: string;
      content: string;
      timestamp: string;
    }>;
  }> {
    return this.request(`/chat/conversations/${conversationId}`, {
      method: 'GET',
    });
  }

  async deleteConversation(conversationId: string): Promise<void> {
    return this.request(`/chat/conversations/${conversationId}`, { method: 'DELETE' }); // 添加 /chat 前缀
  }

  async clearAllConversations(): Promise<void> {
    return this.request('/chat/conversations', { method: 'DELETE' }); // 添加 /chat 前缀
  }

  async updateConversationTitle(conversationId: string, title: string): Promise<{
    conversationId: string;
    title: string;
    updatedAt: string;
  }> {
    return this.request(`/chat/conversations/${conversationId}/title`, {
      method: 'PUT',
      body: JSON.stringify({ title }),
    });
  }

  async getConversationStats(): Promise<{
    totalConversations: number;
    totalMessages: number;
    averageMessagesPerConversation: number;
    mostActiveDay: string;
    conversationsByDate: {
      date: string;
      count: number;
    }[];
  }> {
    return this.request('/chat/stats');
  }

  // 智能教学资源管理相关
  async uploadDocument(file: File, params: {
    subject: string;
    courseLevel: string;
    resourceType: string;
    title?: string;
    description?: string;
    keywords?: string;
    autoVectorize?: boolean;
    autoExtractKeywords?: boolean;
  }) {
    const token = this.getToken();
    if (!token) {
      throw new Error('请先登录');
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('subject', params.subject);
    formData.append('courseLevel', params.courseLevel);
    formData.append('resourceType', params.resourceType);
    
    if (params.title) formData.append('title', params.title);
    if (params.description) formData.append('description', params.description);
    if (params.keywords) formData.append('keywords', params.keywords);
    formData.append('autoVectorize', String(params.autoVectorize ?? true));
    formData.append('autoExtractKeywords', String(params.autoExtractKeywords ?? true));

    try {
      const response = await fetch(`${API_BASE_URL}/resources/upload/document`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          // 不要设置Content-Type，让浏览器自动设置multipart/form-data
        },
        body: formData,
      });

      if (!response.ok) {
        if (response.status === 401) {
          console.warn('Upload authentication failed, JWT may be expired');
        }
        
        let errorMessage = 'Upload failed';
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = `HTTP ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      const text = await response.text();
      return text ? JSON.parse(text) : {};
    } catch (error) {
      console.error('Upload Error:', error);
      throw error;
    }
  }

  async uploadAudio(file: File, params: {
    transcriptionMode?: string;
    needTranscription?: boolean;
    subject?: string;
    resourceType?: string;
    description?: string;
    speaker?: string;
    language?: string;
    autoVectorize?: boolean;
  }): Promise<{
    resourceId: string;
    fileName: string;
    fileSize: number;
    transcriptionText: string;
    transcriptionMode: string;
    language: string;
    duration: number;
    uploadTime: string;
  }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('transcriptionMode', params.transcriptionMode || 'sync');
    formData.append('needTranscription', String(params.needTranscription ?? true));
    
    if (params.subject) formData.append('subject', params.subject);
    if (params.resourceType) formData.append('resourceType', params.resourceType);
    if (params.description) formData.append('description', params.description);
    if (params.speaker) formData.append('speaker', params.speaker);
    formData.append('language', params.language || 'zh');
    formData.append('autoVectorize', String(params.autoVectorize ?? true));

    return this.request('/resources/upload/audio', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async uploadBatchResources(files: File[], params: {
    subject: string;
    courseLevel: string;
    autoVectorize?: boolean;
  }): Promise<{
    successCount: number;
    failureCount: number;
    results: {
      fileName: string;
      status: string;
      resourceId?: string;
      error?: string;
    }[];
    uploadTime: string;
  }> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    formData.append('subject', params.subject);
    formData.append('courseLevel', params.courseLevel);
    formData.append('autoVectorize', String(params.autoVectorize ?? true));

    return this.request('/resources/upload/batch', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async uploadBatch(files: File[], params: {
    subject: string;
    courseLevel: string;
    autoVectorize?: boolean;
  }): Promise<{
    successCount: number;
    failedCount: number;
    results: {
      fileName: string;
      status: string;
      resourceId?: string;
      error?: string;
    }[];
    uploadTime: string;
  }> {
    return this.uploadBatchResources(files, params);
  }

  async getResources(params: {
    page?: number;
    size?: number;
    resourceType?: string;
    keywords?: string;
  } = {}): Promise<{
    content: any[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  }> {
    const searchParams = new URLSearchParams();
    searchParams.append('page', (params.page || 0).toString());
    searchParams.append('size', (params.size || 20).toString());
    if (params.resourceType && params.resourceType !== 'all') {
      searchParams.append('resourceType', params.resourceType);
    }
    if (params.keywords) {
      searchParams.append('keywords', params.keywords);
    }
    
    return this.request(`/resources?${searchParams.toString()}`);
  }

  async searchResourcesSemantic(params: {
    query: string;
    topK?: number;
    threshold?: number;
  }): Promise<any[]> {
    const searchParams = new URLSearchParams();
    searchParams.append('query', params.query);
    searchParams.append('topK', (params.topK || 10).toString());
    searchParams.append('threshold', (params.threshold || 0.7).toString());
    
    return this.request(`/resources/search/semantic?${searchParams.toString()}`);
  }

  async getResourceDetail(resourceId: string): Promise<{
    id: string;
    title: string;
    description: string;
    subject: string;
    courseLevel: string;
    resourceType: string;
    originalName: string;
    fileSize: number;
    contentType: string;
    keywords: string[];
    downloadUrl: string;
    transcriptionText?: string;
    isVectorized: boolean;
    knowledgeBaseIds: string[];
    createdAt: string;
    updatedAt: string;
  }> {
    return this.request(`/resources/${resourceId}`);
  }

  async deleteResource(resourceId: string): Promise<void> {
    return this.request(`/resources/${resourceId}`, { method: 'DELETE' });
  }

  async getResourceDownloadUrl(resourceId: string): Promise<{
    downloadUrl: string;
    fileName: string;
    expiresAt: string;
  }> {
    return this.request(`/resources/${resourceId}/download`);
  }

  async getResourcePreview(resourceId: string): Promise<{
    previewType: string;
    previewUrl: string;
    originalUrl: string;
    previewable: boolean;
    previewImages?: string[];
    textContent?: string;
    metadata?: Record<string, any>;
    errorMessage?: string;
  }> {
    return this.request(`/resources/${resourceId}/preview`);
  }

  async getResourceStats(): Promise<{
    totalResources: number;
    totalSize: number;
    categoryStats: {
      category: string;
      count: number;
      totalSize: number;
    }[];
    recentUploads: {
      date: string;
      count: number;
    }[];
  }> {
    return this.request('/resources/stats');
  }

  // 知识库RAG功能相关
  async createKnowledgeBase(params: {
    name: string;
    description: string;
    subject: string;
    courseLevel: string;
    resourceIds: string[];
  }): Promise<{
    knowledgeBaseId: string;
    name: string;
    description?: string;
    resourceCount: number;
    status: string;
    createdAt: string;
  }> {
    return this.request('/resources/knowledge-base', {
      method: 'POST',
      body: JSON.stringify(params),
    });
  }

  async buildKnowledgeBase(resourceIds: string[], name: string, description?: string): Promise<{
    knowledgeBaseId: string;
    name: string;
    description?: string;
    resourceCount: number;
    status: string;
    createdAt: string;
  }> {
    return this.request('/resources/knowledge-base', {
      method: 'POST',
      body: JSON.stringify({ resourceIds, name, description }),
    });
  }

  async getKnowledgeBaseBuildStatus(knowledgeBaseId: string): Promise<{
    knowledgeBaseId: string;
    status: string;
    progress: number;
    message?: string;
    completedAt?: string;
  }> {
    return this.request(`/resources/knowledge-base/${knowledgeBaseId}/status`);
  }

  async getKnowledgeBases(page: number = 0, size: number = 10): Promise<{
    knowledgeBases: {
      knowledgeBaseId: string;
      name: string;
      description?: string;
      resourceCount: number;
      status: string;
      createdAt: string;
      updatedAt: string;
    }[];
    pagination: {
      page: number;
      size: number;
      total: number;
      totalPages: number;
    };
  }> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    const response = await this.request<{
      content: any[];
      totalElements: number;
    }>(`/resources/knowledge-base?${params.toString()}`);
    
    // 转换后端返回的数据结构为前端期望的格式
    return {
      knowledgeBases: response.content || [],
      pagination: {
        page: page,
        size: size,
        total: response.totalElements || 0,
        totalPages: Math.ceil((response.totalElements || 0) / size)
      }
    };
  }

  async ragQuery(knowledgeBaseId: string, query: string, topK: number = 5): Promise<{
    answer: string;
    sources: {
      resourceId: string;
      fileName: string;
      relevantContent: string;
      similarity: number;
    }[];
    query: string;
    knowledgeBaseId: string;
    usage: {
      promptTokens: number;
      completionTokens: number;
      totalTokens: number;
    };
    responseTime: string;
  }> {
    return this.request('/resources/qa', {
      method: 'POST',
      body: JSON.stringify({ 
        knowledgeBaseId, 
        query, 
        topK,
        answerMode: 'detailed',
        includeReferences: true
      }),
    });
  }

  async ragStreamQuery(knowledgeBaseId: string, query: string, topK: number = 5): Promise<Response> {
    const token = this.getToken();
    return fetch(`${API_BASE_URL}/resources/qa/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: JSON.stringify({ 
        knowledgeBaseId, 
        query, 
        topK,
        answerMode: 'detailed',
        includeReferences: true
      }),
    });
  }

  // AI资源自动制作模块相关
  async generatePPT(request: {
    topic: string;
    subject: string;
    courseLevel: string;
    slideCount?: number;
    style?: string;
    includeFormulas?: boolean;
    includeProofs?: boolean;
    targetAudience?: string;
    duration?: number;
    language?: string;
  }): Promise<{
    taskId: string;
    status: string;
    message: string;
    createdAt: string;
  }> {
    return this.request('/ai/generate/ppt', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async generateQuiz(request: {
    topic: string;
    subject: string;
    courseLevel: string;
    difficulty?: string;
    questionCount?: number;
    questionTypes?: string;
    includeSteps?: boolean;
    includeAnswers?: boolean;
    timeLimit?: number;
    language?: string;
  }): Promise<{
    taskId: string;
    status: string;
    message: string;
    createdAt: string;
  }> {
    return this.request('/ai/generate/quiz', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async generateExplanation(request: {
    topic: string;
    subject: string;
    courseLevel: string;
    style?: string;
    length?: string;
    includeExamples?: boolean;
    includeProofs?: boolean;
    includeApplications?: boolean;
    targetAudience?: string;
    language?: string;
  }): Promise<{
    taskId: string;
    status: string;
    message: string;
    createdAt: string;
  }> {
    return this.request('/ai/generate/explanation', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async getTaskStatus(taskId: string): Promise<{
    taskId: string;
    status: string;
    progress: number;
    result?: {
      downloadUrl?: string;
      content?: string;
      metadata?: any;
      fileData?: any;
      fileName?: string;
      fileSize?: number;
    };
    error?: string;
    createdAt: string;
    completedAt?: string;
  }> {
    return this.request(`/tasks/${taskId}/status`);
  }

  async downloadFile(taskId: string): Promise<Blob> {
    console.log('downloadFile - 开始下载，taskId:', taskId);
    console.log('downloadFile - API_BASE_URL:', API_BASE_URL);
    console.log('downloadFile - token:', this.getToken());

    const response = await fetch(`${API_BASE_URL}/download/${taskId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${this.getToken()}`
      }
    });

    console.log('downloadFile - 响应状态:', response.status, response.statusText);
    console.log('downloadFile - 响应头:', Object.fromEntries(response.headers.entries()));

    if (!response.ok) {
      const errorText = await response.text();
      console.error('downloadFile - 错误响应:', errorText);
      throw new Error(`下载失败: ${response.status} ${response.statusText} - ${errorText}`);
    }

    const blob = await response.blob();
    console.log('downloadFile - blob信息:', blob.size, blob.type);
    return blob;
  }

  async previewFile(taskId: string): Promise<{
    taskId: string;
    content: string;
    fileName: string;
    fileSize: number;
    topic: string;
    generatedAt: string;
  }> {
    return this.request(`/preview/${taskId}`);
  }

  async regeneratePPT(taskId: string, data: {
    content: string;
    slides: any[];
  }): Promise<{
    success: boolean;
    message: string;
    newTaskId?: string;
  }> {
    return this.request(`/regenerate-ppt/${taskId}`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // 测试接口
  async testHello(): Promise<{
    message: string;
    timestamp: string;
    user?: {
      userId: string;
      username: string;
      email: string;
    };
  }> {
    return this.request('/auth/hello');
  }
}

export const apiService = new ApiService();
