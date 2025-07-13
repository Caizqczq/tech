import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { 
  Search, 
  Filter, 
  Grid, 
  List, 
  Eye, 
  Download, 
  Share2, 
  Tag, 
  Calendar, 
  User, 
  FileText, 
  Image, 
  Presentation, 
  FileQuestion,
  BookOpen,
  Folder,
  Star,
  MoreVertical
} from 'lucide-react';
import { apiService } from '@/services/api';
import { toast } from '@/hooks/use-toast';
import DashboardHeader from '@/components/dashboard/DashboardHeader';

interface Resource {
  id: string;
  title: string;
  type: 'document' | 'image' | 'ppt' | 'quiz' | 'explanation';
  subject: string;
  grade: string;
  knowledgePoints: string[];
  tags: string[];
  author: string;
  createdAt: string;
  updatedAt: string;
  fileUrl?: string;
  thumbnail?: string;
  description?: string;
  downloads: number;
  rating: number;
  size: string;
}

interface Category {
  id: string;
  name: string;
  subject: string;
  grade: string;
  knowledgePoints: string[];
  resourceCount: number;
}

const ResourceManagement = () => {
  const [resources, setResources] = useState<Resource[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSubject, setSelectedSubject] = useState('all');
  const [selectedGrade, setSelectedGrade] = useState('all');
  const [selectedType, setSelectedType] = useState('all');
  const [sortBy, setSortBy] = useState('createdAt');
  const [selectedResource, setSelectedResource] = useState<Resource | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadResources();
    loadCategories();
  }, []);

  const loadResources = async () => {
    try {
      setLoading(true);
      // 模拟数据，实际应该调用API
      const mockResources: Resource[] = [
        {
          id: '1',
          title: '线性代数基础概念PPT',
          type: 'ppt',
          subject: '数学',
          grade: '大一',
          knowledgePoints: ['矩阵', '向量', '线性变换'],
          tags: ['基础', '概念', '入门'],
          author: '张教授',
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-15T10:00:00Z',
          downloads: 156,
          rating: 4.8,
          size: '2.5MB',
          description: '详细介绍线性代数的基础概念，包括矩阵运算、向量空间等内容'
        },
        {
          id: '2',
          title: '微积分练习题集',
          type: 'quiz',
          subject: '数学',
          grade: '大一',
          knowledgePoints: ['导数', '积分', '极限'],
          tags: ['练习', '习题', '基础'],
          author: '李教授',
          createdAt: '2024-01-14T14:30:00Z',
          updatedAt: '2024-01-14T14:30:00Z',
          downloads: 89,
          rating: 4.6,
          size: '1.2MB',
          description: '包含50道微积分练习题，涵盖导数和积分的基本计算'
        },
        {
          id: '3',
          title: '函数图像分析',
          type: 'image',
          subject: '数学',
          grade: '大一',
          knowledgePoints: ['函数', '图像', '性质'],
          tags: ['图像', '分析', '可视化'],
          author: '王教授',
          createdAt: '2024-01-13T09:15:00Z',
          updatedAt: '2024-01-13T09:15:00Z',
          downloads: 234,
          rating: 4.9,
          size: '3.8MB',
          description: '各种函数的图像及其性质分析图表'
        }
      ];
      setResources(mockResources);
    } catch (error) {
      toast({
        title: "加载失败",
        description: "无法加载教学资源",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const mockCategories: Category[] = [
        {
          id: '1',
          name: '线性代数',
          subject: '数学',
          grade: '大一',
          knowledgePoints: ['矩阵', '向量', '线性变换', '特征值'],
          resourceCount: 45
        },
        {
          id: '2',
          name: '微积分',
          subject: '数学',
          grade: '大一',
          knowledgePoints: ['导数', '积分', '极限', '连续性'],
          resourceCount: 67
        },
        {
          id: '3',
          name: '概率统计',
          subject: '数学',
          grade: '大二',
          knowledgePoints: ['概率', '分布', '统计推断', '假设检验'],
          resourceCount: 32
        }
      ];
      setCategories(mockCategories);
    } catch (error) {
      console.error('加载分类失败:', error);
    }
  };

  const filteredResources = resources.filter(resource => {
    const matchesSearch = resource.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         resource.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         resource.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesSubject = selectedSubject === 'all' || resource.subject === selectedSubject;
    const matchesGrade = selectedGrade === 'all' || resource.grade === selectedGrade;
    const matchesType = selectedType === 'all' || resource.type === selectedType;
    
    return matchesSearch && matchesSubject && matchesGrade && matchesType;
  });

  const sortedResources = [...filteredResources].sort((a, b) => {
    switch (sortBy) {
      case 'title':
        return a.title.localeCompare(b.title);
      case 'downloads':
        return b.downloads - a.downloads;
      case 'rating':
        return b.rating - a.rating;
      case 'createdAt':
      default:
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    }
  });

  const getResourceIcon = (type: string) => {
    switch (type) {
      case 'document': return <FileText className="h-5 w-5" />;
      case 'image': return <Image className="h-5 w-5" />;
      case 'ppt': return <Presentation className="h-5 w-5" />;
      case 'quiz': return <FileQuestion className="h-5 w-5" />;
      case 'explanation': return <BookOpen className="h-5 w-5" />;
      default: return <FileText className="h-5 w-5" />;
    }
  };

  const getResourceTypeLabel = (type: string) => {
    switch (type) {
      case 'document': return '文档';
      case 'image': return '图片';
      case 'ppt': return 'PPT';
      case 'quiz': return '习题';
      case 'explanation': return '讲解';
      default: return '未知';
    }
  };

  const handleDownload = async (resource: Resource) => {
    try {
      // 实际应该调用下载API
      toast({
        title: "下载开始",
        description: `正在下载 ${resource.title}`,
      });
    } catch (error) {
      toast({
        title: "下载失败",
        description: "无法下载该资源",
        variant: "destructive",
      });
    }
  };

  const ResourceCard = ({ resource }: { resource: Resource }) => (
    <Card className="group hover:shadow-lg transition-all duration-300 cursor-pointer">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-2">
            <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
              {getResourceIcon(resource.type)}
            </div>
            <div className="flex-1">
              <CardTitle className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors line-clamp-2">
                {resource.title}
              </CardTitle>
              <div className="flex items-center space-x-2 mt-1">
                <Badge variant="secondary" className="text-xs">
                  {getResourceTypeLabel(resource.type)}
                </Badge>
                <Badge variant="outline" className="text-xs">
                  {resource.subject}
                </Badge>
                <Badge variant="outline" className="text-xs">
                  {resource.grade}
                </Badge>
              </div>
            </div>
          </div>
          <Button variant="ghost" size="sm">
            <MoreVertical className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      
      <CardContent className="pt-0">
        <CardDescription className="text-sm text-gray-600 mb-3 line-clamp-2">
          {resource.description}
        </CardDescription>
        
        <div className="flex flex-wrap gap-1 mb-3">
          {resource.knowledgePoints.slice(0, 3).map((point, index) => (
            <Badge key={index} variant="secondary" className="text-xs">
              {point}
            </Badge>
          ))}
          {resource.knowledgePoints.length > 3 && (
            <Badge variant="secondary" className="text-xs">
              +{resource.knowledgePoints.length - 3}
            </Badge>
          )}
        </div>
        
        <div className="flex items-center justify-between text-sm text-gray-500 mb-3">
          <div className="flex items-center space-x-1">
            <User className="h-3 w-3" />
            <span>{resource.author}</span>
          </div>
          <div className="flex items-center space-x-1">
            <Calendar className="h-3 w-3" />
            <span>{new Date(resource.createdAt).toLocaleDateString()}</span>
          </div>
        </div>
        
        <div className="flex items-center justify-between text-sm text-gray-500 mb-4">
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-1">
              <Download className="h-3 w-3" />
              <span>{resource.downloads}</span>
            </div>
            <div className="flex items-center space-x-1">
              <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
              <span>{resource.rating}</span>
            </div>
          </div>
          <span>{resource.size}</span>
        </div>
        
        <div className="flex space-x-2">
          <Button 
            size="sm" 
            variant="outline" 
            className="flex-1"
            onClick={() => setSelectedResource(resource)}
          >
            <Eye className="h-3 w-3 mr-1" />
            预览
          </Button>
          <Button 
            size="sm" 
            className="flex-1"
            onClick={() => handleDownload(resource)}
          >
            <Download className="h-3 w-3 mr-1" />
            下载
          </Button>
        </div>
      </CardContent>
    </Card>
  );

  const ResourceListItem = ({ resource }: { resource: Resource }) => (
    <Card className="mb-3">
      <CardContent className="p-4">
        <div className="flex items-center space-x-4">
          <div className="flex-shrink-0">
            <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
              {getResourceIcon(resource.type)}
            </div>
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex items-center space-x-2 mb-1">
              <h3 className="text-lg font-semibold text-gray-900 truncate">{resource.title}</h3>
              <Badge variant="secondary" className="text-xs">
                {getResourceTypeLabel(resource.type)}
              </Badge>
            </div>
            
            <p className="text-sm text-gray-600 mb-2 line-clamp-1">{resource.description}</p>
            
            <div className="flex items-center space-x-4 text-sm text-gray-500">
              <span>{resource.subject} • {resource.grade}</span>
              <span>{resource.author}</span>
              <span>{new Date(resource.createdAt).toLocaleDateString()}</span>
              <div className="flex items-center space-x-1">
                <Download className="h-3 w-3" />
                <span>{resource.downloads}</span>
              </div>
              <div className="flex items-center space-x-1">
                <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                <span>{resource.rating}</span>
              </div>
            </div>
          </div>
          
          <div className="flex-shrink-0 flex space-x-2">
            <Button size="sm" variant="outline" onClick={() => setSelectedResource(resource)}>
              <Eye className="h-3 w-3 mr-1" />
              预览
            </Button>
            <Button size="sm" onClick={() => handleDownload(resource)}>
              <Download className="h-3 w-3 mr-1" />
              下载
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      <DashboardHeader />
      
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">教学资源管理</h1>
          <p className="text-gray-600">按学科知识点自动分类，快速检索和预览教学资源</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* 侧边栏 - 分类和筛选 */}
          <div className="lg:col-span-1">
            <Card className="mb-6">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Folder className="h-5 w-5" />
                  <span>资源分类</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {categories.map((category) => (
                    <div key={category.id} className="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer">
                      <div className="flex items-center justify-between mb-1">
                        <h4 className="font-medium text-gray-900">{category.name}</h4>
                        <Badge variant="secondary" className="text-xs">
                          {category.resourceCount}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600">{category.subject} • {category.grade}</p>
                      <div className="flex flex-wrap gap-1 mt-2">
                        {category.knowledgePoints.slice(0, 2).map((point, index) => (
                          <Badge key={index} variant="outline" className="text-xs">
                            {point}
                          </Badge>
                        ))}
                        {category.knowledgePoints.length > 2 && (
                          <Badge variant="outline" className="text-xs">
                            +{category.knowledgePoints.length - 2}
                          </Badge>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
            
            {/* 筛选器 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Filter className="h-5 w-5" />
                  <span>筛选条件</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="subject-filter">学科</Label>
                  <Select value={selectedSubject} onValueChange={setSelectedSubject}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">全部学科</SelectItem>
                      <SelectItem value="数学">数学</SelectItem>
                      <SelectItem value="物理">物理</SelectItem>
                      <SelectItem value="化学">化学</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div>
                  <Label htmlFor="grade-filter">年级</Label>
                  <Select value={selectedGrade} onValueChange={setSelectedGrade}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">全部年级</SelectItem>
                      <SelectItem value="大一">大一</SelectItem>
                      <SelectItem value="大二">大二</SelectItem>
                      <SelectItem value="大三">大三</SelectItem>
                      <SelectItem value="大四">大四</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div>
                  <Label htmlFor="type-filter">资源类型</Label>
                  <Select value={selectedType} onValueChange={setSelectedType}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">全部类型</SelectItem>
                      <SelectItem value="document">文档</SelectItem>
                      <SelectItem value="image">图片</SelectItem>
                      <SelectItem value="ppt">PPT</SelectItem>
                      <SelectItem value="quiz">习题</SelectItem>
                      <SelectItem value="explanation">讲解</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </CardContent>
            </Card>
          </div>
          
          {/* 主内容区 */}
          <div className="lg:col-span-3">
            {/* 搜索和工具栏 */}
            <div className="flex items-center space-x-4 mb-6">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  placeholder="搜索资源标题、描述或标签..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              
              <Select value={sortBy} onValueChange={setSortBy}>
                <SelectTrigger className="w-40">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="createdAt">最新创建</SelectItem>
                  <SelectItem value="title">标题排序</SelectItem>
                  <SelectItem value="downloads">下载量</SelectItem>
                  <SelectItem value="rating">评分</SelectItem>
                </SelectContent>
              </Select>
              
              <div className="flex border rounded-lg">
                <Button
                  variant={viewMode === 'grid' ? 'default' : 'ghost'}
                  size="sm"
                  onClick={() => setViewMode('grid')}
                  className="rounded-r-none"
                >
                  <Grid className="h-4 w-4" />
                </Button>
                <Button
                  variant={viewMode === 'list' ? 'default' : 'ghost'}
                  size="sm"
                  onClick={() => setViewMode('list')}
                  className="rounded-l-none"
                >
                  <List className="h-4 w-4" />
                </Button>
              </div>
            </div>
            
            {/* 资源列表 */}
            {loading ? (
              <div className="text-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-4 text-gray-600">加载中...</p>
              </div>
            ) : sortedResources.length === 0 ? (
              <div className="text-center py-12">
                <FileText className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                <p className="text-gray-600">没有找到匹配的资源</p>
                <p className="text-sm text-gray-500">尝试调整搜索条件或筛选器</p>
              </div>
            ) : (
              <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6' : 'space-y-3'}>
                {sortedResources.map((resource) => 
                  viewMode === 'grid' ? (
                    <ResourceCard key={resource.id} resource={resource} />
                  ) : (
                    <ResourceListItem key={resource.id} resource={resource} />
                  )
                )}
              </div>
            )}
          </div>
        </div>
        
        {/* 资源预览对话框 */}
        <Dialog open={!!selectedResource} onOpenChange={() => setSelectedResource(null)}>
          <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
            {selectedResource && (
              <>
                <DialogHeader>
                  <DialogTitle className="flex items-center space-x-2">
                    {getResourceIcon(selectedResource.type)}
                    <span>{selectedResource.title}</span>
                  </DialogTitle>
                  <DialogDescription>
                    {selectedResource.description}
                  </DialogDescription>
                </DialogHeader>
                
                <div className="space-y-4">
                  <div className="flex flex-wrap gap-2">
                    <Badge>{getResourceTypeLabel(selectedResource.type)}</Badge>
                    <Badge variant="outline">{selectedResource.subject}</Badge>
                    <Badge variant="outline">{selectedResource.grade}</Badge>
                    {selectedResource.tags.map((tag, index) => (
                      <Badge key={index} variant="secondary">{tag}</Badge>
                    ))}
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <strong>作者：</strong>{selectedResource.author}
                    </div>
                    <div>
                      <strong>文件大小：</strong>{selectedResource.size}
                    </div>
                    <div>
                      <strong>下载次数：</strong>{selectedResource.downloads}
                    </div>
                    <div>
                      <strong>评分：</strong>{selectedResource.rating}/5.0
                    </div>
                  </div>
                  
                  <div>
                    <strong>知识点：</strong>
                    <div className="flex flex-wrap gap-1 mt-1">
                      {selectedResource.knowledgePoints.map((point, index) => (
                        <Badge key={index} variant="outline" className="text-xs">
                          {point}
                        </Badge>
                      ))}
                    </div>
                  </div>
                  
                  <div className="flex space-x-2 pt-4">
                    <Button onClick={() => handleDownload(selectedResource)} className="flex-1">
                      <Download className="h-4 w-4 mr-2" />
                      下载资源
                    </Button>
                    <Button variant="outline" className="flex-1">
                      <Share2 className="h-4 w-4 mr-2" />
                      分享资源
                    </Button>
                  </div>
                </div>
              </>
            )}
          </DialogContent>
        </Dialog>
      </main>
    </div>
  );
};

export default ResourceManagement;