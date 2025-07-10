
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { 
  Download, 
  Share2, 
  BarChart3, 
  FileText, 
  Presentation, 
  Image, 
  HelpCircle, 
  ArrowLeft,
  Eye,
  MoreHorizontal,
  Copy
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useToast } from '@/hooks/use-toast';

const ProjectDetail = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [selectedFile, setSelectedFile] = useState('lesson_plan');
  
  const projectFiles = [
    {
      id: 'lesson_plan',
      name: '教案.docx',
      type: 'document',
      icon: FileText,
      size: '2.4 MB',
      preview: {
        title: '光合作用教学设计',
        content: `
          一、教学目标
          1. 知识目标：理解光合作用的概念、过程和意义
          2. 能力目标：培养学生观察、分析和归纳的能力
          3. 情感目标：培养学生热爱自然、保护环境的意识
          
          二、教学重点
          - 光合作用的定义和化学方程式
          - 光合作用的两个阶段：光反应和暗反应
          
          三、教学难点
          - 光合作用过程中物质和能量的转化
          - 影响光合作用强度的因素
          
          四、教学过程
          1. 导入新课（5分钟）
             通过展示绿色植物图片，引导学生思考：为什么植物是绿色的？
          
          2. 新课教学（30分钟）
             (1) 光合作用的发现过程
             (2) 光合作用的定义和化学方程式
             (3) 光合作用的详细过程
          
          3. 巩固练习（8分钟）
             完成课堂练习，检验学习效果
          
          4. 课堂小结（2分钟）
             总结本节课的主要内容
        `
      }
    },
    {
      id: 'ppt_slides',
      name: '课件.pptx',
      type: 'presentation',
      icon: Presentation,
      size: '5.7 MB',
      preview: {
        title: 'PPT课件预览',
        slides: [
          { title: '光合作用', subtitle: '生物能量转换的奇迹' },
          { title: '学习目标', content: '理解光合作用的概念和过程' },
          { title: '光合作用的发现', content: '历史回顾与科学发展' },
          { title: '光合作用方程式', content: '6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂' },
          { title: '光反应阶段', content: '在叶绿体囊状结构中进行' }
        ]
      }
    },
    {
      id: 'images',
      name: '配图素材/',
      type: 'folder',
      icon: Image,
      size: '3.2 MB',
      preview: {
        title: '教学配图',
        images: [
          { name: '叶绿体结构图', url: 'https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=400&h=300&fit=crop' },
          { name: '光合作用过程图', url: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' },
          { name: '植物细胞图', url: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=300&fit=crop' },
          { name: '实验装置图', url: 'https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400&h=300&fit=crop' }
        ]
      }
    },
    {
      id: 'quiz',
      name: '测验题目.json',
      type: 'quiz',
      icon: HelpCircle,
      size: '0.8 MB',
      preview: {
        title: '随堂测验',
        questions: [
          {
            type: '选择题',
            question: '光合作用的场所是？',
            options: ['A. 线粒体', 'B. 叶绿体', 'C. 核糖体', 'D. 细胞核'],
            answer: 'B'
          },
          {
            type: '填空题',
            question: '光合作用的化学方程式为：_____ + _____ → _____ + _____',
            answer: '6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂'
          },
          {
            type: '简答题',
            question: '简述光合作用的生物学意义。',
            answer: '1. 制造有机物，为生物提供食物来源\n2. 释放氧气，维持大气中氧气含量\n3. 吸收二氧化碳，调节大气成分'
          }
        ]
      }
    }
  ];

  const selectedFileData = projectFiles.find(file => file.id === selectedFile);

  const handleFileSelect = (fileId: string) => {
    setSelectedFile(fileId);
  };

  const handleDownload = (fileName: string) => {
    toast({
      title: "下载开始",
      description: `正在下载 ${fileName}...`,
    });
  };

  const handleCopy = () => {
    toast({
      title: "复制成功",
      description: "内容已复制到剪贴板",
    });
  };

  const handleShare = () => {
    toast({
      title: "分享成功",
      description: "项目已分享到社区",
    });
  };

  const renderPreview = () => {
    if (!selectedFileData) return null;

    switch (selectedFileData.type) {
      case 'document':
        return (
          <div className="prose max-w-none">
            <h2 className="text-2xl font-bold mb-4">{selectedFileData.preview.title}</h2>
            <div className="whitespace-pre-line text-gray-700 leading-relaxed">
              {selectedFileData.preview.content}
            </div>
          </div>
        );

      case 'presentation':
        return (
          <div className="space-y-4">
            <h2 className="text-2xl font-bold mb-4">{selectedFileData.preview.title}</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {selectedFileData.preview.slides.map((slide, index) => (
                <Card key={index} className="p-4 bg-gradient-to-br from-blue-50 to-purple-50 cursor-pointer hover:shadow-md transition-shadow">
                  <div className="text-center">
                    <h3 className="font-bold text-lg mb-2">{slide.title}</h3>
                    {slide.subtitle && (
                      <p className="text-gray-600 mb-2">{slide.subtitle}</p>
                    )}
                    {slide.content && (
                      <p className="text-sm text-gray-700">{slide.content}</p>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          </div>
        );

      case 'folder':
        return (
          <div className="space-y-4">
            <h2 className="text-2xl font-bold mb-4">{selectedFileData.preview.title}</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {selectedFileData.preview.images.map((image, index) => (
                <Card key={index} className="overflow-hidden cursor-pointer hover:shadow-md transition-shadow">
                  <img 
                    src={image.url} 
                    alt={image.name}
                    className="w-full h-48 object-cover hover:scale-105 transition-transform"
                  />
                  <CardContent className="p-3">
                    <p className="font-medium text-center">{image.name}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        );

      case 'quiz':
        return (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold mb-4">{selectedFileData.preview.title}</h2>
            {selectedFileData.preview.questions.map((question, index) => (
              <Card key={index} className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <CardTitle className="text-lg">
                    题目 {index + 1} ({question.type})
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <p className="font-medium">{question.question}</p>
                  {question.options && (
                    <div className="space-y-1">
                      {question.options.map((option, optIndex) => (
                        <p key={optIndex} className="text-gray-700">{option}</p>
                      ))}
                    </div>
                  )}
                  <div className="bg-green-50 p-3 rounded-lg">
                    <p className="text-green-800"><strong>答案：</strong>{question.answer}</p>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        );

      default:
        return <div className="text-center text-gray-500 py-8">无法预览此文件类型</div>;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航 */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <Button 
                variant="ghost" 
                onClick={() => navigate('/dashboard')}
                className="text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="mr-2 h-4 w-4" />
                返回
              </Button>
              <div>
                <h1 className="text-xl font-bold text-gray-900">光合作用教学设计</h1>
                <p className="text-sm text-gray-600">高中一年级 • 生物</p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <Button 
                variant="outline" 
                size="sm"
                onClick={() => handleDownload('全部文件')}
              >
                <Download className="mr-2 h-4 w-4" />
                全部下载
              </Button>
              <Button 
                variant="outline" 
                size="sm"
                onClick={handleShare}
              >
                <Share2 className="mr-2 h-4 w-4" />
                分享到社区
              </Button>
              <Button 
                size="sm"
                className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700"
                onClick={() => navigate('/analytics')}
              >
                <BarChart3 className="mr-2 h-4 w-4" />
                分析教学效果
              </Button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-6 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* 左侧文件列表 */}
          <Card className="lg:col-span-1">
            <CardHeader>
              <CardTitle className="text-lg">项目文件</CardTitle>
              <CardDescription>点击文件名查看内容</CardDescription>
            </CardHeader>
            <CardContent className="space-y-2">
              {projectFiles.map((file) => (
                <div
                  key={file.id}
                  className={`flex items-center space-x-3 p-3 rounded-lg cursor-pointer transition-colors group ${
                    selectedFile === file.id
                      ? 'bg-purple-50 border border-purple-200'
                      : 'hover:bg-gray-50'
                  }`}
                  onClick={() => handleFileSelect(file.id)}
                >
                  <file.icon className={`h-5 w-5 ${
                    selectedFile === file.id ? 'text-purple-600' : 'text-gray-500'
                  }`} />
                  <div className="flex-1 min-w-0">
                    <p className={`font-medium truncate ${
                      selectedFile === file.id ? 'text-purple-900' : 'text-gray-900'
                    }`}>
                      {file.name}
                    </p>
                    <p className="text-xs text-gray-500">{file.size}</p>
                  </div>
                  <div className="opacity-0 group-hover:opacity-100 transition-opacity">
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      className="h-6 w-6 p-0"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDownload(file.name);
                      }}
                    >
                      <MoreHorizontal className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>

          {/* 右侧预览区域 */}
          <Card className="lg:col-span-3">
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  {selectedFileData && <selectedFileData.icon className="h-5 w-5 text-gray-600" />}
                  <CardTitle>{selectedFileData?.name}</CardTitle>
                </div>
                <div className="flex items-center space-x-2">
                  <Button variant="outline" size="sm">
                    <Eye className="mr-2 h-4 w-4" />
                    预览
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => selectedFileData && handleDownload(selectedFileData.name)}
                  >
                    <Download className="mr-2 h-4 w-4" />
                    下载
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={handleCopy}
                  >
                    <Copy className="mr-2 h-4 w-4" />
                    复制
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-6">
              <div className="max-h-96 overflow-y-auto">
                {renderPreview()}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 项目统计信息 */}
        <div className="mt-8">
          <Card>
            <CardHeader>
              <CardTitle>项目信息</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">4</div>
                  <p className="text-sm text-gray-600">生成文件</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">12.1 MB</div>
                  <p className="text-sm text-gray-600">总大小</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">2024-06-20</div>
                  <p className="text-sm text-gray-600">创建日期</p>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-orange-600">100%</div>
                  <p className="text-sm text-gray-600">完成度</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default ProjectDetail;
