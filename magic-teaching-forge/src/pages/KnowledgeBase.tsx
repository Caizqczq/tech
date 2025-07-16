
import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Search, BookOpen, Download, Star, Filter, ArrowLeft, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';
import { apiService } from '@/services/api';

const KnowledgeBase = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [resources, setResources] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  });

  // 获取资源列表
  const fetchResources = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        resourceType: selectedCategory === 'all' ? undefined : selectedCategory,
        keywords: searchTerm || undefined
      };
      
      const response = await apiService.getResources(params);
      
      setResources(response?.content || []);
      setPagination({
        page: response?.number || 0,
        size: response?.size || 20,
        totalElements: response?.totalElements || 0,
        totalPages: response?.totalPages || 0
      });
    } catch (error) {
      console.error('获取资源失败:', error);
      toast({
        title: "获取资源失败",
        description: "请稍后重试",
        variant: "destructive",
      });
      setResources([]);
    } finally {
      setLoading(false);
    }
  };

  // 语义搜索
  const handleSemanticSearch = async () => {
    if (!searchTerm.trim()) return;
    
    setLoading(true);
    try {
      const response = await apiService.searchResourcesSemantic({
        query: searchTerm,
        topK: 10,
        threshold: 0.7
      });
      setResources(response || []);
    } catch (error) {
      console.error('语义搜索失败:', error);
      toast({
        title: "搜索失败",
        description: "请稍后重试",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  // 组件加载时获取资源
  useEffect(() => {
    fetchResources();
  }, [selectedCategory, pagination.page]);

  // 搜索时的防抖处理
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm) {
        handleSemanticSearch();
      } else {
        fetchResources();
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const categories = [
    { id: 'all', name: '全部', count: 156 },
    { id: 'math', name: '数学', count: 45 },
    { id: 'chinese', name: '语文', count: 38 },
    { id: 'english', name: '英语', count: 32 },
    { id: 'science', name: '科学', count: 28 },
    { id: 'history', name: '历史', count: 13 }
  ];

  const handleDownload = async (resourceId: string, title: string) => {
    try {
      const response = await apiService.getResourceDownloadUrl(resourceId);
      window.open(response.downloadUrl, '_blank');
      toast({
        title: "开始下载",
        description: `"${title}" 下载已开始`,
      });
    } catch (error) {
      console.error('下载失败:', error);
      toast({
        title: "下载失败",
        description: "请稍后重试",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      {/* 顶部导航栏 */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <Button
                variant="ghost"
                onClick={() => navigate('/dashboard')}
                className="flex items-center space-x-2"
              >
                <ArrowLeft className="h-4 w-4" />
                <span>返回</span>
              </Button>
              <h1 className="text-2xl font-bold text-gray-900">知识库</h1>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-6 py-8">
        {/* 搜索和筛选 */}
        <div className="mb-8">
          <div className="flex flex-col md:flex-row gap-4 mb-6">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                type="text"
                placeholder="搜索教学资源..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
                onKeyPress={(e) => e.key === 'Enter' && handleSemanticSearch()}
              />
            </div>
            <Button onClick={handleSemanticSearch} className="px-8" disabled={loading}>
              {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
              搜索
            </Button>
          </div>

          {/* 分类筛选 */}
          <div className="flex flex-wrap gap-2">
            {categories.map((category) => (
              <Button
                key={category.id}
                variant={selectedCategory === category.id ? "default" : "outline"}
                onClick={() => setSelectedCategory(category.id)}
                className="text-sm"
              >
                {category.name} ({category.count})
              </Button>
            ))}
          </div>
        </div>

        {/* 资源列表 */}
        {loading ? (
          <div className="flex justify-center items-center py-12">
            <Loader2 className="h-8 w-8 animate-spin text-purple-600" />
            <span className="ml-2 text-gray-600">加载中...</span>
          </div>
        ) : resources.length === 0 ? (
          <div className="text-center py-12">
            <BookOpen className="h-16 w-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-600 mb-2">暂无资源</h3>
            <p className="text-gray-500">尝试搜索其他关键词或选择不同的分类</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {resources.map((resource) => (
                <Card key={resource.id || resource.resourceId} className="group hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                  <div className="relative overflow-hidden rounded-t-lg">
                    <div className="w-full h-48 bg-gradient-to-br from-purple-500 to-blue-600 flex items-center justify-center">
                      <BookOpen className="h-16 w-16 text-white opacity-60" />
                    </div>
                    <div className="absolute inset-0 bg-black opacity-0 group-hover:opacity-20 transition-opacity"></div>
                  </div>
                  <CardHeader className="pb-2">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                        {resource.category || resource.resourceType || '文档'}
                      </span>
                      <div className="flex items-center space-x-1">
                        <Star className="h-4 w-4 text-yellow-400 fill-current" />
                        <span className="text-sm text-gray-600">{resource.rating || '4.5'}</span>
                      </div>
                    </div>
                    <CardTitle className="text-lg font-semibold text-gray-900 group-hover:text-purple-600 transition-colors">
                      {resource.title || resource.fileName || '未命名资源'}
                    </CardTitle>
                    <CardDescription className="text-sm text-gray-600">
                      {resource.description || '暂无描述'}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center justify-between text-sm text-gray-500 mb-4">
                      <span>
                        {resource.uploadTime ? `上传时间: ${new Date(resource.uploadTime).toLocaleDateString()}` 
                         : resource.author ? `作者: ${resource.author}` 
                         : ''}
                      </span>
                      <div className="flex items-center space-x-1">
                        <Download className="h-4 w-4" />
                        <span>{resource.downloads || resource.downloadCount || 0}</span>
                      </div>
                    </div>
                    <Button 
                      className="w-full"
                      onClick={() => handleDownload(resource.id || resource.resourceId, resource.title || resource.fileName)}
                    >
                      <Download className="h-4 w-4 mr-2" />
                      下载资源
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
            
            {/* 分页控件 */}
            {pagination.totalPages > 1 && (
              <div className="flex justify-center items-center space-x-4 mt-8">
                <Button
                  variant="outline"
                  disabled={pagination.page === 0}
                  onClick={() => setPagination(prev => ({ ...prev, page: prev.page - 1 }))}
                >
                  上一页
                </Button>
                <span className="text-sm text-gray-600">
                  第 {pagination.page + 1} 页，共 {pagination.totalPages} 页
                </span>
                <Button
                  variant="outline"
                  disabled={pagination.page >= pagination.totalPages - 1}
                  onClick={() => setPagination(prev => ({ ...prev, page: prev.page + 1 }))}
                >
                  下一页
                </Button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default KnowledgeBase;
