import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
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
  Share2
} from 'lucide-react';

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

const ResourceCenter: React.FC = () => {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [activeTab, setActiveTab] = useState('resources');

  const resourceCategories = [
    { id: 'all', label: '全部资源', icon: Database, count: 156, color: 'from-blue-500 to-purple-600' },
    { id: 'lesson_plan', label: '教案课件', icon: FileText, count: 45, color: 'from-green-500 to-teal-600' },
    { id: 'paper', label: '学术论文', icon: BookOpen, count: 32, color: 'from-pink-500 to-rose-600' },
    { id: 'textbook', label: '教材资料', icon: Archive, count: 28, color: 'from-purple-500 to-indigo-600' },
    { id: 'lecture', label: '音频讲座', icon: Music, count: 23, color: 'from-orange-500 to-red-600' },
    { id: 'exercise', label: '练习题库', icon: PenTool, count: 28, color: 'from-cyan-500 to-blue-600' }
  ];

  const mockResources: ResourceItem[] = [
    {
      id: 'res_001',
      title: '高等数学微积分基础教案',
      type: 'lesson_plan',
      size: '2.4 MB',
      uploadDate: '2024-01-15',
      author: '张教授',
      tags: ['微积分', '高等数学', '本科', '基础'],
      downloads: 156,
      rating: 4.8,
      description: '详细的微积分基础知识教案，包含导数、积分的概念和应用。',
      status: 'vectorized'
    },
    {
      id: 'res_002',
      title: '线性代数矩阵运算研究论文',
      type: 'paper',
      size: '1.8 MB',
      uploadDate: '2024-01-14',
      author: '李教授',
      tags: ['线性代数', '矩阵', '研究', '论文'],
      downloads: 89,
      rating: 4.6,
      description: '关于矩阵运算优化算法的最新研究成果。',
      status: 'ready'
    },
    {
      id: 'res_003',
      title: '概率论与数理统计音频讲座',
      type: 'lecture',
      size: '45.2 MB',
      uploadDate: '2024-01-13',
      author: '王教授',
      tags: ['概率论', '统计', '音频', '讲座'],
      downloads: 234,
      rating: 4.9,
      description: '概率论基础概念的详细音频讲解。',
      status: 'processing'
    }
  ];

  const mockKnowledgeBases: KnowledgeBase[] = [
    {
      id: 'kb_001',
      name: '高等数学知识库',
      description: '包含微积分、线性代数、概率论等核心内容',
      resourceCount: 25,
      status: 'ready',
      createdAt: '2024-01-10',
      lastUpdated: '2024-01-15'
    },
    {
      id: 'kb_002',
      name: '数学分析专题库',
      description: '深入的数学分析理论和应用案例',
      resourceCount: 18,
      status: 'building',
      createdAt: '2024-01-12',
      lastUpdated: '2024-01-15'
    }
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

  const filteredResources = mockResources.filter(resource => {
    const matchesSearch = resource.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         resource.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesCategory = selectedCategory === 'all' || resource.type === selectedCategory;
    return matchesSearch && matchesCategory;
  });

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
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
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
                  <Button className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl">
                    <Upload className="h-4 w-4 mr-2" />
                    上传资源
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Resources List */}
          <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6' : 'space-y-4'}>
            {filteredResources.map((resource) => {
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
                        {resource.tags.slice(0, 3).map((tag) => (
                          <Badge key={tag} variant="secondary" className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 border-indigo-200">
                            {tag}
                          </Badge>
                        ))}
                        {resource.tags.length > 3 && (
                          <Badge variant="secondary" className="text-xs px-2 py-1 bg-gray-100 text-gray-600">
                            +{resource.tags.length - 3}
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
                          <Button size="sm" variant="ghost" className="h-8 w-8 p-0 hover:bg-green-100 dark:hover:bg-green-900/20">
                            <Download className="h-4 w-4" />
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
        </TabsContent>

        <TabsContent value="knowledge" className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {mockKnowledgeBases.map((kb) => (
              <Card key={kb.id} className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
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
                          {kb.description}
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
                      <span className="font-medium text-gray-900 dark:text-white">{kb.resourceCount} 个</span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600 dark:text-gray-400">最后更新</span>
                      <span className="text-gray-900 dark:text-white">{kb.lastUpdated}</span>
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
                <Button className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl">
                  <Plus className="h-4 w-4 mr-2" />
                  创建知识库
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="qa" className="space-y-6">
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
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center space-x-4">
                  <div className="flex-1">
                    <Input
                      placeholder="请输入您的问题..."
                      className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30 rounded-xl"
                    />
                  </div>
                  <Button className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl">
                    <MessageSquare className="h-4 w-4 mr-2" />
                    提问
                  </Button>
                </div>
                
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-xl p-6 text-center">
                  <div className="w-16 h-16 bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-900 dark:to-purple-900 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <Brain className="h-8 w-8 text-indigo-600 dark:text-indigo-400" />
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">智能问答助手</h3>
                  <p className="text-gray-600 dark:text-gray-400">基于您的知识库内容，为您提供准确、详细的答案</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default ResourceCenter;