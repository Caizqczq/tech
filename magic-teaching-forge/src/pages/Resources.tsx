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
  Zap
} from 'lucide-react';

interface ResourceItem {
  id: string;
  name: string;
  type: 'document' | 'image' | 'video' | 'audio' | 'template' | 'question';
  size: string;
  uploadDate: string;
  author: string;
  tags: string[];
  downloads: number;
  rating: number;
  thumbnail?: string;
  description: string;
}

const Resources: React.FC = () => {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');

  const resourceCategories = [
    { id: 'all', label: '全部资源', icon: Database, count: 1247, color: 'from-blue-500 to-purple-600' },
    { id: 'documents', label: '文档资料', icon: FileText, count: 342, color: 'from-green-500 to-teal-600' },
    { id: 'images', label: '图片素材', icon: Image, count: 456, color: 'from-pink-500 to-rose-600' },
    { id: 'videos', label: '视频资源', icon: Video, count: 123, color: 'from-purple-500 to-indigo-600' },
    { id: 'templates', label: '模板库', icon: Layers, count: 89, color: 'from-orange-500 to-red-600' },
    { id: 'questions', label: '题库', icon: BookOpen, count: 237, color: 'from-cyan-500 to-blue-600' }
  ];

  const mockResources: ResourceItem[] = [
    {
      id: '1',
      name: '高中数学函数专题课件',
      type: 'document',
      size: '2.4 MB',
      uploadDate: '2024-01-15',
      author: '张老师',
      tags: ['数学', '高中', '函数', 'PPT'],
      downloads: 156,
      rating: 4.8,
      description: '详细讲解高中数学函数的概念、性质和应用，包含丰富的例题和练习。'
    },
    {
      id: '2',
      name: '化学实验安全图解',
      type: 'image',
      size: '1.8 MB',
      uploadDate: '2024-01-14',
      author: '李老师',
      tags: ['化学', '实验', '安全', '图解'],
      downloads: 89,
      rating: 4.6,
      description: '化学实验室安全操作规范图解，包含常见实验器材使用方法。'
    },
    {
      id: '3',
      name: '英语口语练习视频',
      type: 'video',
      size: '45.2 MB',
      uploadDate: '2024-01-13',
      author: '王老师',
      tags: ['英语', '口语', '练习', '视频'],
      downloads: 234,
      rating: 4.9,
      description: '日常英语口语练习视频，包含发音技巧和对话练习。'
    },
    {
      id: '4',
      name: '物理实验报告模板',
      type: 'template',
      size: '156 KB',
      uploadDate: '2024-01-12',
      author: '赵老师',
      tags: ['物理', '实验', '报告', '模板'],
      downloads: 67,
      rating: 4.5,
      description: '标准化的物理实验报告模板，包含实验目的、步骤、数据记录等部分。'
    },
    {
      id: '5',
      name: '历史选择题题库',
      type: 'question',
      size: '890 KB',
      uploadDate: '2024-01-11',
      author: '陈老师',
      tags: ['历史', '选择题', '题库', '考试'],
      downloads: 178,
      rating: 4.7,
      description: '涵盖中国古代史、近现代史的选择题题库，适合高中历史教学。'
    },
    {
      id: '6',
      name: '生物细胞结构图',
      type: 'image',
      size: '3.2 MB',
      uploadDate: '2024-01-10',
      author: '刘老师',
      tags: ['生物', '细胞', '结构', '图解'],
      downloads: 145,
      rating: 4.8,
      description: '高清生物细胞结构示意图，包含动物细胞和植物细胞的详细标注。'
    }
  ];

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'document': return FileText;
      case 'image': return Image;
      case 'video': return Video;
      case 'audio': return Music;
      case 'template': return Layers;
      case 'question': return BookOpen;
      default: return FileText;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'document': return 'text-blue-600 bg-blue-100';
      case 'image': return 'text-pink-600 bg-pink-100';
      case 'video': return 'text-purple-600 bg-purple-100';
      case 'audio': return 'text-green-600 bg-green-100';
      case 'template': return 'text-orange-600 bg-orange-100';
      case 'question': return 'text-cyan-600 bg-cyan-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const filteredResources = mockResources.filter(resource => {
    const matchesSearch = resource.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         resource.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesCategory = selectedCategory === 'all' || 
                           (selectedCategory === 'documents' && resource.type === 'document') ||
                           (selectedCategory === 'images' && resource.type === 'image') ||
                           (selectedCategory === 'videos' && resource.type === 'video') ||
                           (selectedCategory === 'templates' && resource.type === 'template') ||
                           (selectedCategory === 'questions' && resource.type === 'question');
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-gray-900 dark:via-blue-900 dark:to-indigo-900 p-6">
      <div className="max-w-7xl mx-auto space-y-8">
        {/* Header */}
        <div className="text-center space-y-4">
          <div className="inline-flex items-center space-x-3 px-6 py-3 bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl rounded-2xl border border-white/30 dark:border-gray-700/30">
            <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-red-600 rounded-xl flex items-center justify-center">
              <Database className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-white">资源中心</h1>
              <p className="text-gray-600 dark:text-gray-300">管理和分享您的教学资源</p>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
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
                      <p className="text-2xl font-bold text-gray-900 dark:text-white">
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
                  placeholder="搜索资源名称、标签..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-12 pr-4 py-3 bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30 rounded-xl focus:ring-2 focus:ring-indigo-500/50"
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

        {/* Resources Grid/List */}
        <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6' : 'space-y-4'}>
          {filteredResources.map((resource) => {
            const TypeIcon = getTypeIcon(resource.type);
            const typeColor = getTypeColor(resource.type);
            
            if (viewMode === 'grid') {
              return (
                <Card key={resource.id} className="group hover:shadow-xl transition-all duration-300 hover:scale-105 bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${typeColor}`}>
                        <TypeIcon className="h-6 w-6" />
                      </div>
                      <div className="flex items-center space-x-1">
                        <Star className="h-4 w-4 text-yellow-500 fill-current" />
                        <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{resource.rating}</span>
                      </div>
                    </div>
                    <div>
                      <CardTitle className="text-lg font-semibold text-gray-900 dark:text-white line-clamp-2">
                        {resource.name}
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
                            <Edit className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            } else {
              return (
                <Card key={resource.id} className="group hover:shadow-lg transition-all duration-300 bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
                  <CardContent className="p-4">
                    <div className="flex items-center space-x-4">
                      <div className={`w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 ${typeColor}`}>
                        <TypeIcon className="h-6 w-6" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between">
                          <div className="flex-1 min-w-0">
                            <h3 className="text-lg font-semibold text-gray-900 dark:text-white truncate">
                              {resource.name}
                            </h3>
                            <p className="text-gray-600 dark:text-gray-400 text-sm line-clamp-1 mt-1">
                              {resource.description}
                            </p>
                            <div className="flex items-center space-x-4 mt-2 text-sm text-gray-500 dark:text-gray-400">
                              <div className="flex items-center space-x-1">
                                <User className="h-4 w-4" />
                                <span>{resource.author}</span>
                              </div>
                              <div className="flex items-center space-x-1">
                                <Clock className="h-4 w-4" />
                                <span>{resource.uploadDate}</span>
                              </div>
                              <div className="flex items-center space-x-1">
                                <Download className="h-4 w-4" />
                                <span>{resource.downloads}</span>
                              </div>
                              <span>{resource.size}</span>
                            </div>
                          </div>
                          <div className="flex items-center space-x-3 ml-4">
                            <div className="flex items-center space-x-1">
                              <Star className="h-4 w-4 text-yellow-500 fill-current" />
                              <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{resource.rating}</span>
                            </div>
                            <div className="flex items-center space-x-2">
                              <Button size="sm" variant="ghost" className="h-8 w-8 p-0 hover:bg-indigo-100 dark:hover:bg-indigo-900/20">
                                <Eye className="h-4 w-4" />
                              </Button>
                              <Button size="sm" variant="ghost" className="h-8 w-8 p-0 hover:bg-green-100 dark:hover:bg-green-900/20">
                                <Download className="h-4 w-4" />
                              </Button>
                              <Button size="sm" variant="ghost" className="h-8 w-8 p-0 hover:bg-blue-100 dark:hover:bg-blue-900/20">
                                <Edit className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        </div>
                        <div className="flex flex-wrap gap-1 mt-3">
                          {resource.tags.slice(0, 4).map((tag) => (
                            <Badge key={tag} variant="secondary" className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 border-indigo-200">
                              {tag}
                            </Badge>
                          ))}
                          {resource.tags.length > 4 && (
                            <Badge variant="secondary" className="text-xs px-2 py-1 bg-gray-100 text-gray-600">
                              +{resource.tags.length - 4}
                            </Badge>
                          )}
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            }
          })}
        </div>

        {/* Empty State */}
        {filteredResources.length === 0 && (
          <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
            <CardContent className="p-12 text-center">
              <div className="w-24 h-24 bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <FolderOpen className="h-12 w-12 text-gray-400" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">暂无资源</h3>
              <p className="text-gray-600 dark:text-gray-400 mb-6">没有找到符合条件的资源，请尝试调整搜索条件或上传新资源。</p>
              <Button className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl">
                <Plus className="h-4 w-4 mr-2" />
                上传第一个资源
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
};

export default Resources;