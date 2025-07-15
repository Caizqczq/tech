
import { ApiResponse, User, LoginRequest, RegisterRequest, ConversationMessage, ConversationDetail, ConversationItem, TeachingResourceItem, KnowledgeBaseItem, AIGenerationTask, UsageStats, PaginationInfo } from '@/types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082/api';

class ApiService {
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = localStorage.getItem('token'); // 改为 'token'
    
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
        const errorData = await response.json();
        throw new Error(errorData.message || 'API request failed');
      }
      
      const data = await response.json();
      return data;
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
    const token = localStorage.getItem('token'); // 改为 'token'
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
    const token = localStorage.getItem('token'); // 改为 'token'
    
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
    return this.request('/chat/conversations/stats');
  }

  // 智能教学资源管理相关
  async uploadDocument(file: File, category: string = 'document', description?: string): Promise<{
    resourceId: string;
    fileName: string;
    fileSize: number;
    category: string;
    description?: string;
    extractedContent: string;
    metadata: {
      pageCount?: number;
      wordCount?: number;
      language?: string;
      keywords?: string[];
    };
    uploadTime: string;
  }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', category);
    if (description) formData.append('description', description);
    
    return this.request('/resources/upload/document', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async uploadAudio(file: File, transcriptionMode: string = 'auto', language: string = 'zh-CN'): Promise<{
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
    formData.append('transcriptionMode', transcriptionMode);
    formData.append('language', language);
    
    return this.request('/resources/upload/audio', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async uploadBatchResources(files: File[], category: string = 'document'): Promise<{
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
    formData.append('category', category);
    
    return this.request('/resources/upload/batch', {
      method: 'POST',
      body: formData,
      headers: {}, // 让浏览器自动设置Content-Type
    });
  }

  async getResources(page: number = 1, size: number = 10, category?: string, keyword?: string): Promise<{
    resources: {
      resourceId: string;
      fileName: string;
      category: string;
      description?: string;
      fileSize: number;
      uploadTime: string;
      downloadCount: number;
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
    if (category) params.append('category', category);
    if (keyword) params.append('keyword', keyword);
    
    return this.request(`/resources?${params.toString()}`);
  }

  async searchResourcesSemantic(query: string, limit: number = 10, threshold: number = 0.7): Promise<{
    results: {
      resourceId: string;
      fileName: string;
      category: string;
      relevantContent: string;
      similarity: number;
      uploadTime: string;
    }[];
    query: string;
    searchTime: string;
  }> {
    const params = new URLSearchParams();
    params.append('query', query);
    params.append('limit', limit.toString());
    params.append('threshold', threshold.toString());
    
    return this.request(`/resources/search/semantic?${params.toString()}`);
  }

  async getResourceDetail(resourceId: string): Promise<{
    resourceId: string;
    fileName: string;
    category: string;
    description?: string;
    fileSize: number;
    extractedContent: string;
    metadata: {
      pageCount?: number;
      wordCount?: number;
      language?: string;
      keywords?: string[];
    };
    uploadTime: string;
    downloadCount: number;
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
    return this.request(`/knowledge-base/${knowledgeBaseId}/status`);
  }

  async getKnowledgeBases(page: number = 1, size: number = 10): Promise<{
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
    return this.request(`/knowledge-base?${params.toString()}`);
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
    return this.request('/rag/query', {
      method: 'POST',
      body: JSON.stringify({ knowledgeBaseId, query, topK }),
    });
  }

  async ragStreamQuery(knowledgeBaseId: string, query: string, topK: number = 5): Promise<Response> {
    const token = localStorage.getItem('auth_token');
    return fetch(`${API_BASE_URL}/rag/query/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: JSON.stringify({ knowledgeBaseId, query, topK }),
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
    };
    error?: string;
    createdAt: string;
    completedAt?: string;
  }> {
    return this.request(`/tasks/${taskId}/status`);
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
