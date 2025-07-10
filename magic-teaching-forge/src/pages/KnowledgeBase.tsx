
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Search, BookOpen, Download, Star, Filter, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';

const KnowledgeBase = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');

  const categories = [
    { id: 'all', name: '全部', count: 156 },
    { id: 'math', name: '数学', count: 45 },
    { id: 'chinese', name: '语文', count: 38 },
    { id: 'english', name: '英语', count: 32 },
    { id: 'science', name: '科学', count: 28 },
    { id: 'history', name: '历史', count: 13 }
  ];

  const resources = [
    {
      id: 1,
      title: '高中数学函数专题复习',
      description: '包含二次函数、指数函数、对数函数等重点内容',
      category: '数学',
      author: '李老师',
      downloads: 1245,
      rating: 4.8,
      thumbnail: 'https://images.unsplash.com/photo-1509228468518-180dd4864904?w=300&h=200&fit=crop'
    },
    {
      id: 2,
      title: '古诗词情感分析教学法',
      description: '创新的语文教学方法，提高学生文学素养',
      category: '语文',
      author: '王老师',
      downloads: 956,
      rating: 4.9,
      thumbnail: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300&h=200&fit=crop'
    },
    {
      id: 3,
      title: '化学实验安全操作指南',
      description: '全面的化学实验安全知识和操作规范',
      category: '科学',
      author: '张老师',
      downloads: 834,
      rating: 4.7,
      thumbnail: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=300&h=200&fit=crop'
    },
    {
      id: 4,
      title: '英语口语交际训练',
      description: '提高学生英语口语表达能力的训练方法',
      category: '英语',
      author: '陈老师',
      downloads: 723,
      rating: 4.6,
      thumbnail: 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=300&h=200&fit=crop'
    }
  ];

  const handleSearch = () => {
    toast({
      title: "搜索功能",
      description: `正在搜索 "${searchTerm}"...`,
    });
  };

  const handleDownload = (resourceId: number, title: string) => {
    toast({
      title: "下载资源",
      description: `正在下载 "${title}"...`,
    });
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
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <Button onClick={handleSearch} className="px-8">
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
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {resources.map((resource) => (
            <Card key={resource.id} className="group hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
              <div className="relative overflow-hidden rounded-t-lg">
                <img 
                  src={resource.thumbnail} 
                  alt={resource.title}
                  className="w-full h-48 object-cover group-hover:scale-110 transition-transform duration-300"
                />
                <div className="absolute inset-0 bg-black opacity-0 group-hover:opacity-20 transition-opacity"></div>
              </div>
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">{resource.category}</span>
                  <div className="flex items-center space-x-1">
                    <Star className="h-4 w-4 text-yellow-400 fill-current" />
                    <span className="text-sm text-gray-600">{resource.rating}</span>
                  </div>
                </div>
                <CardTitle className="text-lg font-semibold text-gray-900 group-hover:text-purple-600 transition-colors">
                  {resource.title}
                </CardTitle>
                <CardDescription className="text-sm text-gray-600">
                  {resource.description}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between text-sm text-gray-500 mb-4">
                  <span>作者: {resource.author}</span>
                  <div className="flex items-center space-x-1">
                    <Download className="h-4 w-4" />
                    <span>{resource.downloads}</span>
                  </div>
                </div>
                <Button 
                  className="w-full"
                  onClick={() => handleDownload(resource.id, resource.title)}
                >
                  <Download className="h-4 w-4 mr-2" />
                  下载资源
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* 分页 */}
        <div className="flex justify-center mt-12">
          <div className="flex space-x-2">
            <Button variant="outline" disabled>上一页</Button>
            <Button variant="default">1</Button>
            <Button variant="outline">2</Button>
            <Button variant="outline">3</Button>
            <Button variant="outline">下一页</Button>
          </div>
        </div>
      </main>
    </div>
  );
};

export default KnowledgeBase;
