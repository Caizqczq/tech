import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { apiService } from '@/services/api';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Checkbox } from '@/components/ui/checkbox';
import { toast } from '@/hooks/use-toast';
import {
  Database,
  FileText,
  Image,
  Video,
  Music,
  Archive,
  Search,
  Filter,
  Upload,
  Download,
  Eye,
  Edit,
  Trash2,
  Star,
  Clock,
  User,
  Tag,
  Grid3X3,
  List,
  Plus,
  FolderOpen,
  BookOpen,
  PenTool,
  Layers,
  Zap,
  BarChart3,
  TrendingUp,
  FileCheck,
  Brain,
  MessageSquare,
  Share2,
  Loader2
} from 'lucide-react';
import { UploadDialog } from './UploadDialog';

interface ResourceItem {
  id: string;
  title: string;
  type: 'lesson_plan' | 'paper' | 'textbook' | 'lecture' | 'exercise';
  size: string;
  uploadDate: string;
  author: string;
  tags: string[];
  downloads: number;
  rating: number;
  description: string;
  status: 'processing' | 'ready' | 'vectorized';
}

interface KnowledgeBase {
  id: string;
  name: string;
  description: string;
  resourceCount: number;
  status: 'building' | 'ready' | 'error';
  createdAt: string;
  lastUpdated: string;
}

interface QAMessage {
  id: string;
  type: 'user' | 'assistant';
  content: string;
  timestamp: string;
  sources?: {
    resourceId: string;
    fileName: string;
    relevantContent: string;
    similarity: number;
  }[];
}

interface QAInterfaceProps {
  knowledgeBases: KnowledgeBase[];
}

const QAInterface: React.FC<QAInterfaceProps> = ({ knowledgeBases }) => {
  const [messages, setMessages] = useState<QAMessage[]>([]);
  const [currentQuestion, setCurrentQuestion] = useState('');
  const [selectedKnowledgeBase, setSelectedKnowledgeBase] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSubmitQuestion = async () => {
    if (!currentQuestion.trim() || !selectedKnowledgeBase) {
      toast({
        title: "请填写必要信息",
        description: "请选择知识库并输入问题",
        variant: "destructive",
      });
      return;
    }

    const userMessage: QAMessage = {
      id: Date.now().toString(),
      type: 'user',
      content: currentQuestion,
      timestamp: new Date().toISOString(),
    };

    setMessages(prev => [...prev, userMessage]);
    setCurrentQuestion('');
    setIsLoading(true);

    try {
      const response = await apiService.ragQuery(selectedKnowledgeBase, currentQuestion, 5);
      
      const assistantMessage: QAMessage = {
        id: (Date.now() + 1).toString(),
        type: 'assistant',
        content: response.answer,
        timestamp: new Date().toISOString(),
        sources: response.sources,
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (error: unknown) {
      const errorMessage: QAMessage = {
        id: (Date.now() + 1).toString(),
        type: 'assistant',
        content: '抱歉，我无法回答这个问题。请稍后再试。',
        timestamp: new Date().toISOString(),
      };

      setMessages(prev => [...prev, errorMessage]);
      
      toast({
        title: "查询失败",
        description: (error instanceof Error ? error.message : String(error)) || "智能问答服务暂时不可用",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const quickQuestions = [
    "这个主题的核心概念是什么？",
    "有哪些相关的实例和应用？",
    "这个知识点有什么难点和重点？",
    "有什么学习建议和方法？"
  ];

  return (
    <Card className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <MessageSquare className="h-5 w-5" />
          <span>智能问答</span>
        </CardTitle>
        <CardDescription>
          基于知识库的智能问答系统，快速获取准确答案
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* 知识库选择 */}
        <div className="flex items-center space-x-4">
          <Label htmlFor="kb-select" className="text-sm font-medium whitespace-nowrap">
            选择知识库:
          </Label>
          <Select value={selectedKnowledgeBase} onValueChange={setSelectedKnowledgeBase}>
            <SelectTrigger className="flex-1">
              <SelectValue placeholder="请选择知识库" />
            </SelectTrigger>
            <SelectContent>
              {Array.isArray(knowledgeBases) && knowledgeBases.filter(kb => kb.status === 'ready').map((kb) => (
                <SelectItem key={kb.id || kb.knowledgeBaseId} value={kb.id || kb.knowledgeBaseId}>
                  {kb.name} ({kb.resourceCount || 0} 个资源)
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* 消息历史 */}
        <div className="bg-gray-50 dark:bg-gray-700/50 rounded-xl p-4 h-64 overflow-y-auto">
          {messages.length === 0 ? (
            <div className="text-center py-8">
              <div className="w-16 h-16 bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-900 dark:to-purple-900 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <Brain className="h-8 w-8 text-indigo-600 dark:text-indigo-400" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">智能问答助手</h3>
              <p className="text-gray-600 dark:text-gray-400">基于您的知识库内容，为您提供准确、详细的答案</p>
            </div>
          ) : (
            <div className="space-y-4">
              {messages.map((message) => (
                <div key={message.id} className={`flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[80%] rounded-lg px-4 py-2 ${
                    message.type === 'user' 
                      ? 'bg-indigo-600 text-white' 
                      : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100'
                  }`}>
                    <div className="whitespace-pre-wrap">{message.content}</div>
                    {message.sources && message.sources.length > 0 && (
                      <div className="mt-2 pt-2 border-t border-gray-200 dark:border-gray-600">
                        <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">参考来源:</div>
                        <div className="space-y-1">
                          {message.sources.map((source, index) => (
                            <div key={index} className="text-xs bg-gray-100 dark:bg-gray-700 rounded px-2 py-1">
                              <div className="font-medium">{source.fileName}</div>
                              <div className="text-gray-600 dark:text-gray-400 truncate">
                                {source.relevantContent}
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
              {isLoading && (
                <div className="flex justify-start">
                  <div className="bg-white dark:bg-gray-800 rounded-lg px-4 py-2 flex items-center space-x-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    <span className="text-sm text-gray-600 dark:text-gray-400">正在思考...</span>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* 快速问题 */}
        <div className="flex flex-wrap gap-2">
          {quickQuestions.map((question, index) => (
            <Button
              key={index}
              variant="outline"
              size="sm"
              onClick={() => setCurrentQuestion(question)}
              className="text-xs"
            >
              {question}
            </Button>
          ))}
        </div>

        {/* 问题输入 */}
        <div className="flex items-center space-x-2">
          <Input
            placeholder="请输入您的问题..."
            value={currentQuestion}
            onChange={(e) => setCurrentQuestion(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSubmitQuestion();
              }
            }}
            className="flex-1 bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30 rounded-xl"
            disabled={isLoading}
          />
          <Button 
            onClick={handleSubmitQuestion}
            disabled={!currentQuestion.trim() || !selectedKnowledgeBase || isLoading}
            className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl"
          >
            <MessageSquare className="h-4 w-4 mr-2" />
            提问
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

const ResourceCenter: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // 从URL参数获取初始tab值
  const getInitialTab = () => {
    const params = new URLSearchParams(location.search);
    return params.get('tab') || 'resources';
  };
  
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [activeTab, setActiveTab] = useState(getInitialTab());
  const [resources, setResources] = useState<ResourceItem[]>([]);
  const [knowledgeBases, setKnowledgeBases] = useState<KnowledgeBase[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  });

  // 创建知识库对话框状态
  const [createKbDialogOpen, setCreateKbDialogOpen] = useState(false);
  const [selectedResources, setSelectedResources] = useState<string[]>([]);
  const [newKbForm, setNewKbForm] = useState({
    name: '',
    description: '',
    subject: '',
    courseLevel: ''
  });

  // 当tab改变时更新URL
  const handleTabChange = (newTab: string) => {
    setActiveTab(newTab);
    const params = new URLSearchParams(location.search);
    params.set('tab', newTab);
    navigate(`${location.pathname}?${params.toString()}`, { replace: true });
  };

  // 获取资源列表
  const fetchResources = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        resourceType: selectedCategory === 'all' ? undefined : selectedCategory,
        keywords: searchQuery || undefined
      };
      
      const response = await apiService.getResources(params);
      
      // 确保响应数据的安全处理
      setResources(response?.content || []);
      setPagination({
        page: response?.number || 0,
        size: response?.size || 20,
        totalElements: response?.totalElements || 0,
        totalPages: response?.totalPages || 0
      });
    } catch (error) {
      console.error('获取资源失败:', error);
      // 设置空数组而不是保持 undefined
      setResources([]);
    } finally {
      setLoading(false);
    }
  };

  // 获取知识库列表
  const fetchKnowledgeBases = async () => {
    setLoading(true);
    try {
      const response = await apiService.getKnowledgeBases();
      // 修复：正确提取knowledgeBases数组
      setKnowledgeBases(response?.knowledgeBases || []);
    } catch (error) {
      console.error('获取知识库失败:', error);
      // 修复：错误情况下设置为空数组
      setKnowledgeBases([]);
    } finally {
      setLoading(false);
    }
  };

  // 语义搜索
  const handleSemanticSearch = async (query: string) => {
    if (!query.trim()) return;
    
    setLoading(true);
    try {
      const response = await apiService.searchResourcesSemantic({
        query,
        topK: 10,
        threshold: 0.7
      });
      setResources(response || []);
    } catch (error) {
      console.error('语义搜索失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 创建知识库
  const handleCreateKnowledgeBase = async () => {
    if (!newKbForm.name.trim()) {
      toast({
        title: "请填写知识库名称",
        variant: "destructive",
      });
      return;
    }

    if (!newKbForm.subject.trim()) {
      toast({
        title: "请选择学科领域",
        variant: "destructive",
      });
      return;
    }

    if (!newKbForm.courseLevel.trim()) {
      toast({
        title: "请选择课程层次",
        variant: "destructive",
      });
      return;
    }

    if (selectedResources.length === 0) {
      toast({
        title: "请选择至少一个资源",
        variant: "destructive",
      });
      return;
    }

    try {
      setLoading(true);
      await apiService.createKnowledgeBase({
        name: newKbForm.name,
        description: newKbForm.description,
        subject: newKbForm.subject,
        courseLevel: newKbForm.courseLevel,
        resourceIds: selectedResources
      });
      
      toast({
        title: "创建成功",
        description: `知识库 "${newKbForm.name}" 已创建`,
      });

      // 重置表单和关闭对话框
      setNewKbForm({ name: '', description: '', subject: '', courseLevel: '' });
      setSelectedResources([]);
      setCreateKbDialogOpen(false);
      
      // 刷新知识库列表
      await fetchKnowledgeBases();
    } catch (error: unknown) {
      console.error('创建知识库失败:', error);
      toast({
        title: "创建失败",
        description: (error instanceof Error ? error.message : String(error)) || "创建知识库时发生错误",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  // 处理资源选择
  const handleResourceSelect = (resourceId: string, checked: boolean) => {
    if (checked) {
      setSelectedResources(prev => [...prev, resourceId]);
    } else {
      setSelectedResources(prev => prev.filter(id => id !== resourceId));
    }
  };

  // 下载资源
  const handleDownloadResource = async (resourceId: string) => {
    try {
      const response = await apiService.getResourceDownloadUrl(resourceId);
      window.open(response.downloadUrl, '_blank');
    } catch (error) {
      console.error('下载资源失败:', error);
    }
  };

  // 监听URL变化，更新activeTab
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tabFromUrl = params.get('tab') || 'resources';
    if (tabFromUrl !== activeTab) {
      setActiveTab(tabFromUrl);
    }
  }, [location.search]);

  useEffect(() => {
    fetchResources();
  }, [selectedCategory, searchQuery, pagination.page]);

  useEffect(() => {
    if (activeTab === 'knowledge') {
      fetchKnowledgeBases();
    }
  }, [activeTab]);

  const resourceCategories = [
    { id: 'all', label: '全部资源', icon: Database, count: 156, color: 'from-blue-500 to-purple-600' },
    { id: 'lesson_plan', label: '教案课件', icon: FileText, count: 45, color: 'from-green-500 to-teal-600' },
    { id: 'paper', label: '学术论文', icon: BookOpen, count: 32, color: 'from-pink-500 to-rose-600' },
    { id: 'textbook', label: '教材资料', icon: Archive, count: 28, color: 'from-purple-500 to-indigo-600' },
    { id: 'lecture', label: '音频讲座', icon: Music, count: 23, color: 'from-orange-500 to-red-600' },
    { id: 'exercise', label: '练习题库', icon: PenTool, count: 28, color: 'from-cyan-500 to-blue-600' }
  ];

  const resourceStats = {
    totalResources: 156,
    totalSize: '2.3 GB',
    monthlyUploads: 23,
    totalDownloads: 1247,
    vectorizedResources: 142,
    knowledgeBases: 8
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'lesson_plan': return FileText;
      case 'paper': return BookOpen;
      case 'textbook': return Archive;
      case 'lecture': return Music;
      case 'exercise': return PenTool;
      default: return FileText;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'lesson_plan': return 'text-green-600 bg-green-100';
      case 'paper': return 'text-pink-600 bg-pink-100';
      case 'textbook': return 'text-purple-600 bg-purple-100';
      case 'lecture': return 'text-orange-600 bg-orange-100';
      case 'exercise': return 'text-cyan-600 bg-cyan-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'processing': return <Badge className="bg-blue-100 text-blue-700 border-blue-200">处理中</Badge>;
      case 'ready': return <Badge className="bg-green-100 text-green-700 border-green-200">就绪</Badge>;
      case 'vectorized': return <Badge className="bg-purple-100 text-purple-700 border-purple-200">已向量化</Badge>;
      default: return <Badge className="bg-gray-100 text-gray-700 border-gray-200">未知</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center space-y-4">
        <div className="inline-flex items-center space-x-3 px-6 py-3 bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl rounded-2xl border border-white/30 dark:border-gray-700/30">
          <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-red-600 rounded-xl flex items-center justify-center">
            <Database className="h-6 w-6 text-white" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">资源中心</h2>
            <p className="text-gray-600 dark:text-gray-300">智能管理您的教学资源</p>
          </div>
        </div>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardContent className="p-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center">
                <Database className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">总资源</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{resourceStats.totalResources}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardContent className="p-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-teal-600 rounded-xl flex items-center justify-center">
                <TrendingUp className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">本月上传</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{resourceStats.monthlyUploads}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardContent className="p-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-pink-500 to-rose-600 rounded-xl flex items-center justify-center">
                <Download className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">总下载</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{resourceStats.totalDownloads}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardContent className="p-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center">
                <Brain className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">已向量化</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{resourceStats.vectorizedResources}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardContent className="p-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-red-600 rounded-xl flex items-center justify-center">
                <Archive className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">存储空间</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{resourceStats.totalSize}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardContent className="p-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-cyan-500 to-blue-600 rounded-xl flex items-center justify-center">
                <Layers className="h-5 w-5 text-white" />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">知识库</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{resourceStats.knowledgeBases}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Content */}
      <Tabs value={activeTab} onValueChange={handleTabChange} className="space-y-6">
        <TabsList className="grid w-full grid-cols-3 bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border border-white/30 dark:border-gray-700/30">
          <TabsTrigger value="resources" className="data-[state=active]:bg-white dark:data-[state=active]:bg-gray-700">
            <FileText className="h-4 w-4 mr-2" />
            资源管理
          </TabsTrigger>
          <TabsTrigger value="knowledge" className="data-[state=active]:bg-white dark:data-[state=active]:bg-gray-700">
            <Brain className="h-4 w-4 mr-2" />
            知识库
          </TabsTrigger>
          <TabsTrigger value="qa" className="data-[state=active]:bg-white dark:data-[state=active]:bg-gray-700">
            <MessageSquare className="h-4 w-4 mr-2" />
            智能问答
          </TabsTrigger>
        </TabsList>

        <TabsContent value="resources" className="space-y-6">
          {/* Category Filter */}
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {resourceCategories.map((category) => {
              const IconComponent = category.icon;
              return (
                <Card 
                  key={category.id}
                  className={`cursor-pointer transition-all duration-300 hover:scale-105 hover:shadow-xl ${
                    selectedCategory === category.id 
                      ? 'ring-2 ring-indigo-500 bg-gradient-to-br from-white to-indigo-50 dark:from-gray-800 dark:to-indigo-900/20' 
                      : 'hover:bg-white/80 dark:hover:bg-gray-800/80'
                  } bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30`}
                  onClick={() => setSelectedCategory(category.id)}
                >
                  <CardContent className="p-4">
                    <div className="flex items-center space-x-3">
                      <div className={`w-10 h-10 bg-gradient-to-br ${category.color} rounded-xl flex items-center justify-center`}>
                        <IconComponent className="h-5 w-5 text-white" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                          {category.label}
                        </p>
                        <p className="text-lg font-bold text-gray-900 dark:text-white">
                          {category.count}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          {/* Search and Controls */}
          <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
            <CardContent className="p-6">
              <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
                <div className="flex-1 relative">
                  <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <Input
                    placeholder="搜索资源标题、标签..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        handleSemanticSearch(searchQuery);
                      }
                    }}
                    className="pl-12 pr-4 py-3 bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30 rounded-xl"
                  />
                </div>
                <div className="flex items-center space-x-3">
                  <Button
                    variant={viewMode === 'grid' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setViewMode('grid')}
                    className="rounded-xl"
                  >
                    <Grid3X3 className="h-4 w-4" />
                  </Button>
                  <Button
                    variant={viewMode === 'list' ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setViewMode('list')}
                    className="rounded-xl"
                  >
                    <List className="h-4 w-4" />
                  </Button>
                  <UploadDialog onUploadSuccess={fetchResources} />
                  <Button 
                    onClick={() => handleSemanticSearch(searchQuery)}
                    className="bg-gradient-to-r from-purple-500 to-pink-600 hover:from-purple-600 hover:to-pink-700 text-white rounded-xl"
                  >
                    <Brain className="h-4 w-4 mr-2" />
                    智能搜索
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Resources List */}
          {loading && (
            <div className="flex justify-center items-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
            </div>
          )}
          <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6' : 'space-y-4'}>
            {resources.map((resource) => {
              const TypeIcon = getTypeIcon(resource.type);
              const typeColor = getTypeColor(resource.type);
              
              return (
                <Card key={resource.id} className="group hover:shadow-xl transition-all duration-300 hover:scale-105 bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${typeColor}`}>
                        <TypeIcon className="h-6 w-6" />
                      </div>
                      <div className="flex items-center space-x-2">
                        {getStatusBadge(resource.status)}
                        <div className="flex items-center space-x-1">
                          <Star className="h-4 w-4 text-yellow-500 fill-current" />
                          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{resource.rating}</span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <CardTitle className="text-lg font-semibold text-gray-900 dark:text-white line-clamp-2">
                        {resource.title}
                      </CardTitle>
                      <CardDescription className="text-gray-600 dark:text-gray-400 line-clamp-2 mt-2">
                        {resource.description}
                      </CardDescription>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="space-y-4">
                      <div className="flex flex-wrap gap-1">
                        {(resource.tags || []).slice(0, 3).map((tag) => (
                          <Badge key={tag} variant="secondary" className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 border-indigo-200">
                            {tag}
                          </Badge>
                        ))}
                        {(resource.tags || []).length > 3 && (
                          <Badge variant="secondary" className="text-xs px-2 py-1 bg-gray-100 text-gray-600">
                            +{(resource.tags || []).length - 3}
                          </Badge>
                        )}
                      </div>
                      <div className="flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
                        <div className="flex items-center space-x-4">
                          <div className="flex items-center space-x-1">
                            <User className="h-4 w-4" />
                            <span>{resource.author}</span>
                          </div>
                          <div className="flex items-center space-x-1">
                            <Download className="h-4 w-4" />
                            <span>{resource.downloads}</span>
                          </div>
                        </div>
                        <span>{resource.size}</span>
                      </div>
                      <div className="flex items-center justify-between pt-2 border-t border-gray-200 dark:border-gray-700">
                        <div className="flex items-center space-x-1 text-sm text-gray-500 dark:text-gray-400">
                          <Clock className="h-4 w-4" />
                          <span>{resource.uploadDate}</span>
                        </div>
                        <div className="flex items-center space-x-2">
                          <Button size="sm" variant="ghost" className="h-8 w-8 p-0 hover:bg-indigo-100 dark:hover:bg-indigo-900/20">
                            <Eye className="h-4 w-4" />
                          </Button>
                          <Button 
                            size="sm" 
                            variant="outline"
                            onClick={() => handleDownloadResource(resource.id)}
                            className="rounded-xl"
                          >
                            <Download className="h-4 w-4 mr-1" />
                            下载
                          </Button>
                          <Button size="sm" variant="ghost" className="h-8 w-8 p-0 hover:bg-blue-100 dark:hover:bg-blue-900/20">
                            <Share2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
          {pagination.totalPages > 1 && (
            <div className="flex justify-center items-center space-x-2 mt-6">
              <Button
                variant="outline"
                size="sm"
                disabled={pagination.page === 0}
                onClick={() => setPagination(prev => ({ ...prev, page: prev.page - 1 }))}
              >
                上一页
              </Button>
              <span className="text-sm text-gray-600 dark:text-gray-400">
                第 {pagination.page + 1} 页，共 {pagination.totalPages} 页
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={pagination.page >= pagination.totalPages - 1}
                onClick={() => setPagination(prev => ({ ...prev, page: prev.page + 1 }))}
              >
                下一页
              </Button>
            </div>
          )}
        </TabsContent>

        <TabsContent value="knowledge" className="space-y-6">
          {loading && (
            <div className="flex justify-center items-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
            </div>
          )}
          
          {!loading && (!Array.isArray(knowledgeBases) || knowledgeBases.length === 0) && (
            <div className="text-center py-8">
              <div className="w-16 h-16 bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <Brain className="h-8 w-8 text-gray-400" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">暂无知识库</h3>
              <p className="text-gray-600 dark:text-gray-400">创建您的第一个知识库开始使用智能问答功能</p>
            </div>
          )}
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {Array.isArray(knowledgeBases) && knowledgeBases.map((kb) => (
              <Card key={kb.id || kb.knowledgeBaseId} className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-indigo-600 rounded-xl flex items-center justify-center">
                        <Brain className="h-6 w-6 text-white" />
                      </div>
                      <div>
                        <CardTitle className="text-lg font-semibold text-gray-900 dark:text-white">
                          {kb.name}
                        </CardTitle>
                        <CardDescription className="text-gray-600 dark:text-gray-400">
                          {kb.description || '暂无描述'}
                        </CardDescription>
                      </div>
                    </div>
                    <Badge className={kb.status === 'ready' ? 'bg-green-100 text-green-700 border-green-200' : 'bg-blue-100 text-blue-700 border-blue-200'}>
                      {kb.status === 'ready' ? '就绪' : '构建中'}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600 dark:text-gray-400">包含资源</span>
                      <span className="font-medium text-gray-900 dark:text-white">{kb.resourceCount || 0} 个</span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600 dark:text-gray-400">最后更新</span>
                      <span className="text-gray-900 dark:text-white">
                        {kb.lastUpdated || kb.updatedAt ? new Date(kb.lastUpdated || kb.updatedAt).toLocaleDateString() : '未知'}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 pt-2">
                      <Button size="sm" className="flex-1">
                        <MessageSquare className="h-4 w-4 mr-2" />
                        智能问答
                      </Button>
                      <Button size="sm" variant="outline">
                        <Edit className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
            
            {/* Add New Knowledge Base Card */}
            <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30 border-dashed">
              <CardContent className="p-8 text-center">
                <div className="w-16 h-16 bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 rounded-2xl flex items-center justify-center mx-auto mb-4">
                  <Plus className="h-8 w-8 text-gray-400" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">创建知识库</h3>
                <p className="text-gray-600 dark:text-gray-400 mb-4">将相关资源组织成知识库，支持智能问答</p>
                
                <Dialog open={createKbDialogOpen} onOpenChange={setCreateKbDialogOpen}>
                  <DialogTrigger asChild>
                    <Button 
                      className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl"
                      onClick={() => {
                        setCreateKbDialogOpen(true);
                        // 确保有资源数据可供选择
                        if (resources.length === 0) {
                          fetchResources();
                        }
                      }}
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      创建知识库
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
                    <DialogHeader>
                      <DialogTitle>创建知识库</DialogTitle>
                      <DialogDescription>
                        选择资源创建知识库，支持智能问答功能
                      </DialogDescription>
                    </DialogHeader>
                    
                    <div className="space-y-6">
                      {/* 知识库基本信息 */}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor="kb-name">知识库名称 *</Label>
                          <Input
                            id="kb-name"
                            placeholder="请输入知识库名称"
                            value={newKbForm.name}
                            onChange={(e) => setNewKbForm(prev => ({ ...prev, name: e.target.value }))}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="kb-desc">描述信息</Label>
                          <Input
                            id="kb-desc"
                            placeholder="请输入描述信息（可选）"
                            value={newKbForm.description}
                            onChange={(e) => setNewKbForm(prev => ({ ...prev, description: e.target.value }))}
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="kb-subject">学科领域 *</Label>
                          <Select value={newKbForm.subject} onValueChange={(value) => setNewKbForm(prev => ({ ...prev, subject: value }))}>
                            <SelectTrigger>
                              <SelectValue placeholder="请选择学科领域" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="mathematics">数学</SelectItem>
                              <SelectItem value="physics">物理</SelectItem>
                              <SelectItem value="chemistry">化学</SelectItem>
                              <SelectItem value="biology">生物</SelectItem>
                              <SelectItem value="chinese">语文</SelectItem>
                              <SelectItem value="english">英语</SelectItem>
                              <SelectItem value="history">历史</SelectItem>
                              <SelectItem value="geography">地理</SelectItem>
                              <SelectItem value="politics">政治</SelectItem>
                              <SelectItem value="computer">计算机</SelectItem>
                              <SelectItem value="other">其他</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="kb-level">课程层次 *</Label>
                          <Select value={newKbForm.courseLevel} onValueChange={(value) => setNewKbForm(prev => ({ ...prev, courseLevel: value }))}>
                            <SelectTrigger>
                              <SelectValue placeholder="请选择课程层次" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="undergraduate">本科</SelectItem>
                              <SelectItem value="graduate">研究生</SelectItem>
                              <SelectItem value="doctoral">博士</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>

                      {/* 资源选择 */}
                      <div className="space-y-4">
                        <div className="flex items-center justify-between">
                          <Label className="text-base font-medium">选择资源</Label>
                          <Badge variant="secondary">
                            已选择 {selectedResources.length} 个资源
                          </Badge>
                        </div>
                        
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 max-h-60 overflow-y-auto border rounded-lg p-4">
                          {Array.isArray(resources) && resources.map((resource) => (
                            <div key={resource.id} className="flex items-start space-x-3 p-3 border rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800">
                              <Checkbox
                                id={`resource-${resource.id}`}
                                checked={selectedResources.includes(resource.id)}
                                onCheckedChange={(checked) => handleResourceSelect(resource.id, !!checked)}
                                className="mt-1"
                              />
                              <div className="flex-1 min-w-0">
                                <label
                                  htmlFor={`resource-${resource.id}`}
                                  className="text-sm font-medium text-gray-900 dark:text-white cursor-pointer line-clamp-2"
                                >
                                  {resource.title}
                                </label>
                                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                                  {resource.size} • {resource.uploadDate}
                                </p>
                              </div>
                            </div>
                          ))}
                          
                          {(!Array.isArray(resources) || resources.length === 0) && (
                            <div className="col-span-full text-center py-8 text-gray-500 dark:text-gray-400">
                              <Database className="h-8 w-8 mx-auto mb-2 opacity-50" />
                              <p>暂无可用资源，请先上传资源</p>
                            </div>
                          )}
                        </div>
                      </div>

                      {/* 操作按钮 */}
                      <div className="flex justify-end space-x-3 pt-4 border-t">
                        <Button 
                          variant="outline" 
                          onClick={() => {
                            setCreateKbDialogOpen(false);
                            setNewKbForm({ name: '', description: '', subject: '', courseLevel: '' });
                            setSelectedResources([]);
                          }}
                        >
                          取消
                        </Button>
                        <Button 
                          onClick={handleCreateKnowledgeBase}
                          disabled={loading || !newKbForm.name.trim() || !newKbForm.subject.trim() || !newKbForm.courseLevel.trim() || selectedResources.length === 0}
                          className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700"
                        >
                          {loading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                          创建知识库
                        </Button>
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="qa" className="space-y-6">
          <QAInterface knowledgeBases={knowledgeBases} />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default ResourceCenter;










