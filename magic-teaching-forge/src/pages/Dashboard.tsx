
import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { 
  Plus, 
  BookOpen, 
  Users, 
  TrendingUp, 
  Clock,
  Star,
  Play,
  MoreHorizontal,
  Sparkles,
  Brain,
  Zap,
  Target,
  Award,
  Calendar,
  Wand2,
  Database,
  BarChart3,
  ArrowRight,
  Palette,
  Cpu,
  MessageSquare,
  Upload,
  Download,
  Eye,
  Heart,
  Share2
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import StatsCards from '@/components/dashboard/StatsCards';
import CreateProjectButton from '@/components/dashboard/CreateProjectButton';
import RecentProjects from '@/components/dashboard/RecentProjects';
import QuickActions from '@/components/dashboard/QuickActions';

const Dashboard = () => {
  const navigate = useNavigate();

  const featureModules = [
    {
      title: 'AI智能创作',
      description: '利用人工智能快速生成教学内容',
      icon: Wand2,
      gradient: 'from-purple-500 to-pink-600',
      stats: '本月创建 156 个',
      actions: [
        { label: 'PPT课件生成', path: '/ai-generation?type=ppt' },
        { label: '习题集生成', path: '/ai-generation?type=quiz' },
        { label: '讲解文本生成', path: '/ai-generation?type=explanation' }
      ]
    },
    {
      title: 'AI智能对话',
      description: '与AI助手进行智能教学对话',
      icon: MessageSquare,
      gradient: 'from-green-500 to-teal-600',
      stats: '本月对话 234 次',
      actions: [
        { label: '智能助手', path: '/chat?mode=assistant' },
        { label: '教学建议', path: '/chat?mode=advice' },
        { label: '内容分析', path: '/chat?mode=analysis' }
      ]
    },
    {
      title: '资源素材库',
      description: '丰富的教学资源和素材管理',
      icon: Database,
      gradient: 'from-orange-500 to-red-600',
      stats: '2.3K+ 资源',
      actions: [
        { label: '资源管理', path: '/resource-center?tab=resources' },
        { label: '知识库', path: '/resource-center?tab=knowledge' },
        { label: '素材上传', path: '/upload' }
      ]
    },
    {
      title: '数据分析洞察',
      description: '深入了解教学效果和学习数据',
      icon: BarChart3,
      gradient: 'from-indigo-500 to-blue-600',
      stats: '89% 完成率',
      actions: [
        { label: '数据概览', path: '/analytics' },
        { label: '知识库搜索', path: '/knowledge' },
        { label: '项目详情', path: '/project/demo' }
      ]
    },
    {
      title: 'AI智能助手',
      description: '24/7 智能助手为您答疑解惑',
      icon: Brain,
      gradient: 'from-violet-500 to-purple-600',
      stats: '即时响应',
      actions: [
        { label: '智能对话', path: '/chat' },
        { label: '创建项目', path: '/create' },
        { label: 'AI生成', path: '/ai-generation' }
      ]
    }
  ];

  const quickStats = [
    { label: '今日活跃', value: '1,234', icon: Users, color: 'text-blue-600' },
    { label: '本月创建', value: '156', icon: Plus, color: 'text-green-600' },
    { label: '课程完成', value: '89%', icon: Target, color: 'text-purple-600' },
    { label: '用户满意', value: '4.8', icon: Star, color: 'text-yellow-600' }
  ];

  const recentActivities = [
    { title: '《数学基础课程》已发布', time: '2分钟前', type: 'course', icon: BookOpen },
    { title: '新增50个互动题目', time: '15分钟前', type: 'resource', icon: Upload },
    { title: '学生完成率提升12%', time: '1小时前', type: 'analytics', icon: TrendingUp },
    { title: '收到3条新反馈', time: '2小时前', type: 'feedback', icon: MessageSquare }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-gray-900 dark:via-blue-900 dark:to-indigo-900">
      {/* Hero Section */}
      <div className="relative overflow-hidden bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white">
        <div className="absolute inset-0 bg-black/30" />
        <div className="absolute top-0 right-0 opacity-10">
          <Sparkles className="h-64 w-64" />
        </div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-black mb-6 text-white drop-shadow-2xl tracking-tight">
              欢迎来到 <span className="bg-gradient-to-r from-yellow-300 to-orange-300 bg-clip-text text-transparent drop-shadow-lg">AI教学设计平台</span>
            </h1>
            <p className="text-xl md:text-2xl opacity-95 mb-8 max-w-3xl mx-auto font-semibold drop-shadow-lg">
              让人工智能助力您的教学创新，打造个性化、智能化的学习体验
            </p>
            <div className="flex flex-wrap justify-center gap-4 mb-8">
              {quickStats.map((stat, index) => (
                <div key={index} className="bg-white/20 backdrop-blur-sm rounded-xl px-6 py-4 flex items-center space-x-3">
                  <stat.icon className="h-6 w-6" />
                  <div>
                    <div className="text-2xl font-bold">{stat.value}</div>
                    <div className="text-sm opacity-80">{stat.label}</div>
                  </div>
                </div>
              ))}
            </div>
            <Button 
              onClick={() => navigate('/create')}
              size="lg"
              className="bg-white text-indigo-600 hover:bg-gray-100 font-semibold px-8 py-3 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200"
            >
              <Wand2 className="mr-2 h-5 w-5" />
              开始创作
            </Button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Feature Modules Grid */}
        <div className="mb-12">
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-8 text-center">
            功能模块
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {featureModules.map((module, index) => (
              <Card key={index} className="group hover:shadow-2xl transition-all duration-300 border-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm hover:scale-105">
                <CardHeader className="pb-4">
                  <div className="flex items-center justify-between">
                    <div className={`w-12 h-12 rounded-xl bg-gradient-to-r ${module.gradient} flex items-center justify-center shadow-lg`}>
                      <module.icon className="h-6 w-6 text-white" />
                    </div>
                    <Badge variant="secondary" className="text-xs">
                      {module.stats}
                    </Badge>
                  </div>
                  <CardTitle className="text-xl font-bold text-gray-900 dark:text-white group-hover:bg-gradient-to-r group-hover:from-indigo-600 group-hover:to-purple-600 group-hover:bg-clip-text group-hover:text-transparent transition-all duration-300">
                    {module.title}
                  </CardTitle>
                  <CardDescription className="text-gray-600 dark:text-gray-400">
                    {module.description}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {module.actions.map((action, actionIndex) => (
                      <Button
                        key={actionIndex}
                        variant="ghost"
                        onClick={() => navigate(action.path)}
                        className="w-full justify-between text-left hover:bg-gradient-to-r hover:from-indigo-50 hover:to-purple-50 dark:hover:from-gray-700 dark:hover:to-gray-600 transition-all duration-200"
                      >
                        <span>{action.label}</span>
                        <ArrowRight className="h-4 w-4" />
                      </Button>
                    ))}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>

        {/* Dashboard Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Recent Activities */}
          <div className="lg:col-span-2">
            <Card className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-0 shadow-xl">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Clock className="h-5 w-5 text-indigo-600" />
                  <span>最近活动</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {recentActivities.map((activity, index) => (
                    <div key={index} className="flex items-center space-x-4 p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200">
                      <div className="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center">
                        <activity.icon className="h-5 w-5 text-white" />
                      </div>
                      <div className="flex-1">
                        <p className="font-medium text-gray-900 dark:text-white">{activity.title}</p>
                        <p className="text-sm text-gray-500 dark:text-gray-400">{activity.time}</p>
                      </div>
                      <Button variant="ghost" size="sm">
                        <Eye className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Quick Actions & Stats */}
          <div className="space-y-6">
            {/* Quick Create */}
            <Card className="bg-gradient-to-br from-indigo-500 to-purple-600 text-white border-0 shadow-xl">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Zap className="h-5 w-5" />
                  <span>快速创建</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button 
                  onClick={() => navigate('/create')}
                  variant="secondary" 
                  className="w-full bg-white/20 hover:bg-white/30 text-white border-0"
                >
                  <BookOpen className="mr-2 h-4 w-4" />
                  新建课程
                </Button>
                <Button 
                  onClick={() => navigate('/create')}
                  variant="secondary" 
                  className="w-full bg-white/20 hover:bg-white/30 text-white border-0"
                >
                  <Wand2 className="mr-2 h-4 w-4" />
                  AI教案
                </Button>
                <Button 
                  onClick={() => navigate('/create')}
                  variant="secondary" 
                  className="w-full bg-white/20 hover:bg-white/30 text-white border-0"
                >
                  <Palette className="mr-2 h-4 w-4" />
                  互动内容
                </Button>
              </CardContent>
            </Card>

            {/* AI Assistant */}
            <Card className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-0 shadow-xl">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Brain className="h-5 w-5 text-purple-600" />
                  <span>AI助手</span>
                </CardTitle>
                <CardDescription>
                  智能助手随时为您服务
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="bg-gradient-to-r from-purple-50 to-pink-50 dark:from-gray-700 dark:to-gray-600 rounded-lg p-4">
                  <p className="text-sm text-gray-700 dark:text-gray-300 mb-3">
                    "今天想创建什么类型的课程？我可以帮您快速生成教案和互动内容。"
                  </p>
                  <Button 
                    size="sm" 
                    className="w-full bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600"
                    onClick={() => navigate('/chat')}
                  >
                    <MessageSquare className="mr-2 h-4 w-4" />
                    开始对话
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
