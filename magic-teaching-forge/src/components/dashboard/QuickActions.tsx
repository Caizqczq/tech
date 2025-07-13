import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Upload, Bot, MessageSquare, FolderOpen, Sparkles, FileText } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const QuickActions = () => {
  const navigate = useNavigate();

  const actions = [
    {
      icon: Upload,
      title: '多模态素材上传',
      description: '上传文档、图片、音频等教学素材',
      color: 'bg-blue-500',
      path: '/upload',
      features: ['Word/PDF文档', '图片素材', '语音转文字']
    },
    {
      icon: Sparkles,
      title: 'AI资源自动制作',
      description: '智能生成PPT、习题和讲解文本',
      color: 'bg-purple-500',
      path: '/ai-generation',
      features: ['生成PPT', '智能习题', '讲解文本']
    },
    {
      icon: FolderOpen,
      title: '教学资源管理',
      description: '按学科知识点分类管理教学资源',
      color: 'bg-green-500',
      path: '/resources',
      features: ['自动分类', '资源检索', '预览功能']
    },
    {
      icon: MessageSquare,
      title: '智能对话助手',
      description: '与AI对话，快速咨询教学知识点',
      color: 'bg-orange-500',
      path: '/chat',
      features: ['教学建议', '内容分析', '写作辅助']
    }
  ];

  return (
    <div className="mt-8">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-900">快速功能</h2>
        <p className="text-gray-600">选择功能开始您的智能教学之旅</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {actions.map((action, index) => {
          const IconComponent = action.icon;
          return (
            <Card key={index} className="group hover:shadow-lg transition-all duration-300 cursor-pointer border-0 bg-white/80 backdrop-blur-sm">
              <CardHeader className="pb-3">
                <div className="flex items-center space-x-3">
                  <div className={`p-2 rounded-lg ${action.color} text-white group-hover:scale-110 transition-transform`}>
                    <IconComponent className="h-5 w-5" />
                  </div>
                  <div className="flex-1">
                    <CardTitle className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
                      {action.title}
                    </CardTitle>
                  </div>
                </div>
                <CardDescription className="text-sm text-gray-600 mt-2">
                  {action.description}
                </CardDescription>
              </CardHeader>
              
              <CardContent className="pt-0">
                <div className="space-y-3">
                  <div className="flex flex-wrap gap-1">
                    {action.features.map((feature, featureIndex) => (
                      <Badge key={featureIndex} variant="secondary" className="text-xs">
                        {feature}
                      </Badge>
                    ))}
                  </div>
                  
                  <Button 
                    onClick={() => navigate(action.path)}
                    className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white border-0"
                    size="sm"
                  >
                    立即使用
                  </Button>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>
      
      <div className="mt-8 p-6 bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl border border-blue-100">
        <div className="flex items-center space-x-3 mb-3">
          <Bot className="h-6 w-6 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900">RAG个性化知识库</h3>
        </div>
        <p className="text-gray-600 mb-4">
          上传教材教案时自动构建RAG知识库，为您提供个性化的教学支持和智能问答服务。
        </p>
        <div className="flex space-x-2">
          <Button 
            onClick={() => navigate('/knowledge')}
            variant="outline" 
            size="sm"
            className="border-blue-200 text-blue-600 hover:bg-blue-50"
          >
            <FileText className="h-4 w-4 mr-2" />
            查看知识库
          </Button>
          <Button 
            onClick={() => navigate('/upload')}
            size="sm"
            className="bg-blue-600 hover:bg-blue-700 text-white"
          >
            <Upload className="h-4 w-4 mr-2" />
            上传教材
          </Button>
        </div>
      </div>
    </div>
  );
};

export default QuickActions;