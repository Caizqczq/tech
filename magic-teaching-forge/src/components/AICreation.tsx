import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Wand2,
  FileText,
  PresentationChart,
  BookOpen,
  Brain,
  Sparkles,
  Clock,
  Target,
  Settings,
  Play,
  Download,
  Eye,
  RefreshCw,
  CheckCircle,
  AlertCircle,
  Loader2,
  Plus,
  Lightbulb,
  GraduationCap,
  Calculator,
  Microscope
} from 'lucide-react';

interface GenerationTask {
  id: string;
  type: 'ppt' | 'quiz' | 'explanation';
  title: string;
  status: 'processing' | 'completed' | 'failed';
  progress: number;
  estimatedTime?: number;
  createdAt: string;
  preview?: any;
}

const AICreation: React.FC = () => {
  const [activeTab, setActiveTab] = useState('ppt');
  const [tasks, setTasks] = useState<GenerationTask[]>([
    {
      id: 'task_001',
      type: 'ppt',
      title: '微积分基本定理课件',
      status: 'completed',
      progress: 100,
      createdAt: '2024-01-15T10:30:00',
      preview: { slideCount: 25, duration: 90 }
    },
    {
      id: 'task_002',
      type: 'quiz',
      title: '线性代数特征值习题集',
      status: 'processing',
      progress: 65,
      estimatedTime: 120,
      createdAt: '2024-01-15T11:00:00',
      preview: { questionCount: 15, difficulty: 'intermediate' }
    }
  ]);

  const [formData, setFormData] = useState({
    topic: '',
    subject: '',
    courseLevel: 'undergraduate',
    style: 'academic',
    slideCount: 20,
    questionCount: 10,
    difficulty: 'intermediate',
    includeFormulas: true,
    includeProofs: true,
    duration: 90,
    language: 'zh'
  });

  const creationTypes = [
    {
      id: 'ppt',
      title: 'PPT课件生成',
      description: '智能生成专业学术PPT课件',
      icon: PresentationChart,
      color: 'from-blue-500 to-indigo-600',
      features: ['自动排版', '公式渲染', '图表生成', '模板适配']
    },
    {
      id: 'quiz',
      title: '习题集生成',
      description: '自动生成多样化习题与解答',
      icon: BookOpen,
      color: 'from-green-500 to-emerald-600',
      features: ['多题型支持', '难度分级', '详细解答', '知识点覆盖']
    },
    {
      id: 'explanation',
      title: '讲解文本生成',
      description: '生成详细的学术讲解内容',
      icon: FileText,
      color: 'from-purple-500 to-violet-600',
      features: ['逻辑清晰', '实例丰富', '证明完整', '应用导向']
    }
  ];

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'processing': return <Loader2 className="h-4 w-4 animate-spin text-blue-500" />;
      case 'completed': return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'failed': return <AlertCircle className="h-4 w-4 text-red-500" />;
      default: return <Clock className="h-4 w-4 text-gray-400" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'processing': return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'completed': return 'bg-green-100 text-green-700 border-green-200';
      case 'failed': return 'bg-red-100 text-red-700 border-red-200';
      default: return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const handleGenerate = () => {
    const newTask: GenerationTask = {
      id: `task_${Date.now()}`,
      type: activeTab as 'ppt' | 'quiz' | 'explanation',
      title: `${formData.topic} - ${creationTypes.find(t => t.id === activeTab)?.title}`,
      status: 'processing',
      progress: 0,
      estimatedTime: activeTab === 'ppt' ? 300 : activeTab === 'quiz' ? 180 : 240,
      createdAt: new Date().toISOString(),
      preview: {
        slideCount: formData.slideCount,
        questionCount: formData.questionCount,
        difficulty: formData.difficulty
      }
    };
    setTasks([newTask, ...tasks]);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="text-center space-y-4">
        <div className="inline-flex items-center space-x-3 px-6 py-3 bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl rounded-2xl border border-white/30 dark:border-gray-700/30">
          <div className="w-12 h-12 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center">
            <Wand2 className="h-6 w-6 text-white" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">AI智能创作</h2>
            <p className="text-gray-600 dark:text-gray-300">让AI助力您的教学内容创作</p>
          </div>
        </div>
      </div>

      {/* Creation Types */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {creationTypes.map((type) => {
          const IconComponent = type.icon;
          return (
            <Card 
              key={type.id}
              className={`cursor-pointer transition-all duration-300 hover:scale-105 hover:shadow-xl ${
                activeTab === type.id 
                  ? 'ring-2 ring-indigo-500 bg-gradient-to-br from-white to-indigo-50 dark:from-gray-800 dark:to-indigo-900/20' 
                  : 'hover:bg-white/80 dark:hover:bg-gray-800/80'
              } bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30`}
              onClick={() => setActiveTab(type.id)}
            >
              <CardHeader className="pb-3">
                <div className="flex items-center space-x-3">
                  <div className={`w-12 h-12 bg-gradient-to-br ${type.color} rounded-xl flex items-center justify-center`}>
                    <IconComponent className="h-6 w-6 text-white" />
                  </div>
                  <div className="flex-1">
                    <CardTitle className="text-lg font-semibold text-gray-900 dark:text-white">
                      {type.title}
                    </CardTitle>
                    <CardDescription className="text-gray-600 dark:text-gray-400">
                      {type.description}
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="flex flex-wrap gap-1">
                  {type.features.map((feature) => (
                    <Badge key={feature} variant="secondary" className="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 border-indigo-200">
                      {feature}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Generation Form */}
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Settings className="h-5 w-5" />
              <span>创作参数设置</span>
            </CardTitle>
            <CardDescription>
              配置AI创作的详细参数，生成符合需求的教学内容
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">主题</label>
                <Input
                  placeholder="如：微积分基本定理"
                  value={formData.topic}
                  onChange={(e) => setFormData({...formData, topic: e.target.value})}
                  className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30"
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">学科</label>
                <Input
                  placeholder="如：高等数学"
                  value={formData.subject}
                  onChange={(e) => setFormData({...formData, subject: e.target.value})}
                  className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">课程层次</label>
                <Select value={formData.courseLevel} onValueChange={(value) => setFormData({...formData, courseLevel: value})}>
                  <SelectTrigger className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="undergraduate">本科</SelectItem>
                    <SelectItem value="graduate">研究生</SelectItem>
                    <SelectItem value="doctoral">博士</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">风格</label>
                <Select value={formData.style} onValueChange={(value) => setFormData({...formData, style: value})}>
                  <SelectTrigger className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="academic">学术严谨</SelectItem>
                    <SelectItem value="professional">专业实用</SelectItem>
                    <SelectItem value="interactive">互动生动</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {activeTab === 'ppt' && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">幻灯片数量</label>
                  <Input
                    type="number"
                    value={formData.slideCount}
                    onChange={(e) => setFormData({...formData, slideCount: parseInt(e.target.value) || 20})}
                    className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">授课时长(分钟)</label>
                  <Input
                    type="number"
                    value={formData.duration}
                    onChange={(e) => setFormData({...formData, duration: parseInt(e.target.value) || 90})}
                    className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30"
                  />
                </div>
              </div>
            )}

            {activeTab === 'quiz' && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">题目数量</label>
                  <Input
                    type="number"
                    value={formData.questionCount}
                    onChange={(e) => setFormData({...formData, questionCount: parseInt(e.target.value) || 10})}
                    className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">难度等级</label>
                  <Select value={formData.difficulty} onValueChange={(value) => setFormData({...formData, difficulty: value})}>
                    <SelectTrigger className="bg-white/80 dark:bg-gray-700/80 border-white/30 dark:border-gray-600/30">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="basic">基础</SelectItem>
                      <SelectItem value="intermediate">中等</SelectItem>
                      <SelectItem value="advanced">高级</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            )}

            <div className="flex items-center space-x-4 pt-2">
              <label className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  checked={formData.includeFormulas}
                  onChange={(e) => setFormData({...formData, includeFormulas: e.target.checked})}
                  className="rounded border-gray-300"
                />
                <span className="text-sm text-gray-700 dark:text-gray-300">包含数学公式</span>
              </label>
              <label className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  checked={formData.includeProofs}
                  onChange={(e) => setFormData({...formData, includeProofs: e.target.checked})}
                  className="rounded border-gray-300"
                />
                <span className="text-sm text-gray-700 dark:text-gray-300">包含证明过程</span>
              </label>
            </div>

            <Button 
              onClick={handleGenerate}
              disabled={!formData.topic || !formData.subject}
              className="w-full bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl"
            >
              <Sparkles className="h-4 w-4 mr-2" />
              开始AI创作
            </Button>
          </CardContent>
        </Card>

        {/* Task List */}
        <Card className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-xl border-white/30 dark:border-gray-700/30">
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Brain className="h-5 w-5" />
              <span>创作任务</span>
            </CardTitle>
            <CardDescription>
              查看AI创作任务的进度和结果
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {tasks.length === 0 ? (
                <div className="text-center py-8">
                  <div className="w-16 h-16 bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <Plus className="h-8 w-8 text-gray-400" />
                  </div>
                  <p className="text-gray-500 dark:text-gray-400">暂无创作任务</p>
                  <p className="text-sm text-gray-400 dark:text-gray-500">开始您的第一个AI创作任务</p>
                </div>
              ) : (
                tasks.map((task) => {
                  const typeInfo = creationTypes.find(t => t.id === task.type);
                  const TypeIcon = typeInfo?.icon || FileText;
                  
                  return (
                    <div key={task.id} className="p-4 bg-white/40 dark:bg-gray-700/40 rounded-xl border border-white/20 dark:border-gray-600/20">
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center space-x-3">
                          <div className={`w-10 h-10 bg-gradient-to-br ${typeInfo?.color || 'from-gray-400 to-gray-500'} rounded-lg flex items-center justify-center`}>
                            <TypeIcon className="h-5 w-5 text-white" />
                          </div>
                          <div className="flex-1">
                            <h4 className="font-medium text-gray-900 dark:text-white">{task.title}</h4>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                              {new Date(task.createdAt).toLocaleString()}
                            </p>
                          </div>
                        </div>
                        <Badge className={`${getStatusColor(task.status)} border`}>
                          <div className="flex items-center space-x-1">
                            {getStatusIcon(task.status)}
                            <span className="text-xs">
                              {task.status === 'processing' ? '生成中' : 
                               task.status === 'completed' ? '已完成' : '失败'}
                            </span>
                          </div>
                        </Badge>
                      </div>
                      
                      {task.status === 'processing' && (
                        <div className="space-y-2">
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-gray-600 dark:text-gray-400">进度</span>
                            <span className="text-gray-900 dark:text-white">{task.progress}%</span>
                          </div>
                          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                            <div 
                              className="bg-gradient-to-r from-indigo-500 to-purple-600 h-2 rounded-full transition-all duration-300"
                              style={{ width: `${task.progress}%` }}
                            ></div>
                          </div>
                          {task.estimatedTime && (
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                              预计剩余时间: {Math.max(0, Math.round(task.estimatedTime * (100 - task.progress) / 100))}秒
                            </p>
                          )}
                        </div>
                      )}
                      
                      {task.status === 'completed' && (
                        <div className="flex items-center space-x-2 pt-2">
                          <Button size="sm" variant="outline" className="h-8">
                            <Eye className="h-3 w-3 mr-1" />
                            预览
                          </Button>
                          <Button size="sm" variant="outline" className="h-8">
                            <Download className="h-3 w-3 mr-1" />
                            下载
                          </Button>
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AICreation;