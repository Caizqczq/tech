
import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Home, ArrowLeft, Search, HelpCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const NotFound = () => {
  const navigate = useNavigate();

  const quickLinks = [
    { title: '返回首页', path: '/dashboard', icon: Home, description: '回到主控制面板' },
    { title: '创建项目', path: '/create', icon: Search, description: '开始创建新的教学设计' },
    { title: '查看项目', path: '/project/demo', icon: HelpCircle, description: '查看示例项目' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 flex items-center justify-center px-6">
      <div className="max-w-2xl w-full text-center">
        {/* 404 图标和文字 */}
        <div className="mb-8">
          <div className="text-8xl font-bold text-gray-200 mb-4">404</div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">页面未找到</h1>
          <p className="text-lg text-gray-600 mb-8">
            抱歉，您访问的页面不存在或已被移动。
          </p>
        </div>

        {/* 快速导航 */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          {quickLinks.map((link, index) => (
            <Card 
              key={index} 
              className="cursor-pointer hover:shadow-lg transition-all duration-300 hover:-translate-y-1"
              onClick={() => navigate(link.path)}
            >
              <CardContent className="p-6 text-center">
                <div className="flex justify-center mb-4">
                  <div className="p-3 bg-gradient-to-r from-purple-100 to-blue-100 rounded-full">
                    <link.icon className="h-6 w-6 text-purple-600" />
                  </div>
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{link.title}</h3>
                <p className="text-sm text-gray-600">{link.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* 操作按钮 */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button 
            onClick={() => navigate(-1)}
            variant="outline"
            className="px-6"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            返回上一页
          </Button>
          <Button 
            onClick={() => navigate('/dashboard')}
            className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 px-6"
          >
            <Home className="mr-2 h-4 w-4" />
            回到首页
          </Button>
        </div>

        {/* 帮助信息 */}
        <div className="mt-12 p-6 bg-white rounded-lg shadow-sm border">
          <h3 className="font-semibold text-gray-900 mb-2">需要帮助？</h3>
          <p className="text-gray-600 text-sm mb-4">
            如果您认为这是一个错误，或者需要帮助找到您要找的内容，请联系我们的支持团队。
          </p>
          <div className="flex flex-wrap gap-2 justify-center text-sm">
            <span className="text-gray-500">常见问题：</span>
            <button 
              onClick={() => navigate('/create')}
              className="text-purple-600 hover:text-purple-800 underline"
            >
              如何创建项目
            </button>
            <span className="text-gray-300">|</span>
            <button 
              onClick={() => navigate('/dashboard')}
              className="text-purple-600 hover:text-purple-800 underline"
            >
              项目管理
            </button>
            <span className="text-gray-300">|</span>
            <button className="text-purple-600 hover:text-purple-800 underline">
              联系支持
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
