import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Slider } from '@/components/ui/slider';
import { Separator } from '@/components/ui/separator';
import { 
  Presentation, 
  FileQuestion, 
  BookOpen, 
  Sparkles, 
  Download, 
  Eye, 
  Loader2,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
  Trash2,
  RefreshCw,
  Settings,
  Info
} from 'lucide-react';
import { apiService } from '@/services/api';
import { toast } from '@/hooks/use-toast';
import DashboardHeader from '@/components/dashboard/DashboardHeader';

interface GenerationTask {
  id: string;
  type: 'ppt' | 'quiz' | 'explanation';
  title: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  input: any;
  output?: any;
  error?: string;
  createdAt: string;
}

const AIGeneration = () => {
  const [tasks, setTasks] = useState<GenerationTask[]>([]);
  const [activeTab, setActiveTab] = useState('ppt');
  
  // PPT生成表单
  const [pptForm, setPptForm] = useState({
    topic: '',
    subject: '',
    courseLevel: 'undergraduate',
    slideCount: 10,
    style: 'professional'
  });
  
  // 习题生成表单
  const [quizForm, setQuizForm] = useState({
    topic: '',
    subject: '',
    courseLevel: 'undergraduate',
    questionCount: [5],
    difficulty: 'intermediate',
    questionTypes: 'calculation,proof,application'
  });
  
  // 讲解文本生成表单
  const [explanationForm, setExplanationForm] = useState({
    topic: '',
    subject: '',
    courseLevel: 'undergraduate',
    style: 'rigorous',
    length: 'detailed'
  });

  const generatePPT = async () => {
    try {
      const response = await apiService.generatePPT({
        topic: pptForm.topic,
        subject: pptForm.subject,
        courseLevel: pptForm.courseLevel,
        slideCount: pptForm.slideCount,
        style: pptForm.style
      });
      
      const newTask: GenerationTask = {
        id: response.taskId,
        type: 'ppt',
        title: pptForm.topic,
        status: 'processing',
        progress: 0,
        input: pptForm,
        createdAt: new Date().toISOString()
      };
      
      setTasks(prev => [newTask, ...prev]);
      
      toast({
        title: "PPT生成已开始",
        description: "正在为您生成PPT，请稍候...",
      });
      
      // 开始检查任务进度
      checkTaskProgress(response.taskId);
    } catch (error: any) {
      toast({
        title: "生成失败",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  const generateQuiz = async () => {
    try {
      const response = await apiService.generateQuiz({
        topic: quizForm.topic,
        subject: quizForm.subject,
        courseLevel: quizForm.courseLevel,
        questionCount: quizForm.questionCount[0],
        difficulty: quizForm.difficulty,
        questionTypes: quizForm.questionTypes
      });
      
      const newTask: GenerationTask = {
        id: response.taskId,
        type: 'quiz',
        title: quizForm.topic,
        status: 'processing',
        progress: 0,
        input: quizForm,
        createdAt: new Date().toISOString()
      };
      
      setTasks(prev => [newTask, ...prev]);
      
      toast({
        title: "习题生成已开始",
        description: "正在为您生成习题，请稍候...",
      });
      
      checkTaskProgress(response.taskId);
    } catch (error: any) {
      toast({
        title: "生成失败",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  const generateExplanation = async () => {
    try {
      const response = await apiService.generateExplanation({
        topic: explanationForm.topic,
        subject: explanationForm.subject,
        courseLevel: explanationForm.courseLevel,
        style: explanationForm.style,
        length: explanationForm.length
      });
      
      const newTask: GenerationTask = {
        id: response.taskId,
        type: 'explanation',
        title: explanationForm.topic,
        status: 'processing',
        progress: 0,
        input: explanationForm,
        createdAt: new Date().toISOString()
      };
      
      setTasks(prev => [newTask, ...prev]);
      
      toast({
        title: "讲解文本生成已开始",
        description: "正在为您生成讲解文本，请稍候...",
      });
      
      checkTaskProgress(response.taskId);
    } catch (error: any) {
      toast({
        title: "生成失败",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  const checkTaskProgress = async (taskId: string) => {
    const interval = setInterval(async () => {
      try {
        const response = await apiService.getTaskStatus(taskId);
        
        setTasks(prev => prev.map(task => {
          if (task.id === taskId) {
            if (response.status === 'completed') {
              clearInterval(interval);
              return {
                ...task,
                status: 'completed',
                progress: 100,
                output: response.result
              };
            } else if (response.status === 'failed') {
              clearInterval(interval);
              return {
                ...task,
                status: 'failed',
                progress: 0,
                error: response.error || '任务执行失败'
              };
            } else {
              return {
                ...task,
                status: 'processing',
                progress: response.progress || task.progress
              };
            }
          }
          return task;
        }));
      } catch (error) {
        console.error('检查任务状态失败:', error);
        // 如果API调用失败，则回退到模拟进度
        setTasks(prev => prev.map(task => {
          if (task.id === taskId && task.status === 'processing') {
            const newProgress = Math.min(task.progress + Math.random() * 20, 100);
            if (newProgress >= 100) {
              clearInterval(interval);
              return {
                ...task,
                status: 'completed',
                progress: 100,
                output: {
                  downloadUrl: `/api/download/${taskId}`,
                  previewUrl: `/api/preview/${taskId}`
                }
              };
            }
            return { ...task, progress: newProgress };
          }
          return task;
        }));
      }
    }, 2000); // 每2秒检查一次
  };

  const getTaskIcon = (type: string) => {
    switch (type) {
      case 'ppt': return <Presentation className="h-5 w-5" />;
      case 'quiz': return <FileQuestion className="h-5 w-5" />;
      case 'explanation': return <BookOpen className="h-5 w-5" />;
      default: return <Sparkles className="h-5 w-5" />;
    }
  };

  const getTaskTitle = (type: string) => {
    switch (type) {
      case 'ppt': return 'PPT演示文稿';
      case 'quiz': return '习题练习';
      case 'explanation': return '讲解文本';
      default: return '未知任务';
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
      <DashboardHeader />
      
      <main className="max-w-7xl mx-auto px-6 py-8">
        {/* 页面标题区域 */}
        <div className="mb-10">
          <div className="flex items-center space-x-3 mb-4">
            <div className="p-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl">
              <Sparkles className="h-8 w-8 text-white" />
            </div>
            <div>
              <h1 className="text-4xl font-bold bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent">
                AI资源自动制作
              </h1>
              <p className="text-lg text-gray-600 mt-1">
                使用先进AI技术智能生成教学资源，让教学更高效、更精彩
              </p>
            </div>
          </div>
          
          {/* 功能统计卡片 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
            <div className="bg-white/70 backdrop-blur-sm rounded-xl p-4 border border-blue-200/50">
              <div className="flex items-center space-x-3">
                <div className="p-2 bg-blue-100 rounded-lg">
                  <Presentation className="h-5 w-5 text-blue-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">PPT生成</p>
                  <p className="text-xs text-gray-500">专业演示文稿</p>
                </div>
              </div>
            </div>
            <div className="bg-white/70 backdrop-blur-sm rounded-xl p-4 border border-green-200/50">
              <div className="flex items-center space-x-3">
                <div className="p-2 bg-green-100 rounded-lg">
                  <FileQuestion className="h-5 w-5 text-green-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">习题生成</p>
                  <p className="text-xs text-gray-500">智能练习题库</p>
                </div>
              </div>
            </div>
            <div className="bg-white/70 backdrop-blur-sm rounded-xl p-4 border border-orange-200/50">
              <div className="flex items-center space-x-3">
                <div className="p-2 bg-orange-100 rounded-lg">
                  <BookOpen className="h-5 w-5 text-orange-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">讲解生成</p>
                  <p className="text-xs text-gray-500">详细教学文本</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 生成表单 */}
          <div className="lg:col-span-2">
            <div className="bg-white/80 backdrop-blur-sm rounded-2xl border border-white/50 shadow-xl">
              <Tabs value={activeTab} onValueChange={setActiveTab}>
                <div className="p-6 pb-0">
                  <TabsList className="grid w-full grid-cols-3 bg-gray-100/80 p-1 rounded-xl">
                    <TabsTrigger 
                      value="ppt" 
                      className="flex items-center space-x-2 data-[state=active]:bg-white data-[state=active]:shadow-sm rounded-lg transition-all duration-200"
                    >
                      <Presentation className="h-4 w-4" />
                      <span className="hidden sm:inline">生成PPT</span>
                      <span className="sm:hidden">PPT</span>
                    </TabsTrigger>
                    <TabsTrigger 
                      value="quiz" 
                      className="flex items-center space-x-2 data-[state=active]:bg-white data-[state=active]:shadow-sm rounded-lg transition-all duration-200"
                    >
                      <FileQuestion className="h-4 w-4" />
                      <span className="hidden sm:inline">生成习题</span>
                      <span className="sm:hidden">习题</span>
                    </TabsTrigger>
                    <TabsTrigger 
                      value="explanation" 
                      className="flex items-center space-x-2 data-[state=active]:bg-white data-[state=active]:shadow-sm rounded-lg transition-all duration-200"
                    >
                      <BookOpen className="h-4 w-4" />
                      <span className="hidden sm:inline">生成讲解</span>
                      <span className="sm:hidden">讲解</span>
                    </TabsTrigger>
                  </TabsList>
                </div>
              
                {/* PPT生成 */}
                <TabsContent value="ppt" className="p-6 pt-4">
                  <div className="space-y-6">
                    <div className="flex items-center space-x-3">
                      <div className="p-2 bg-blue-100 rounded-lg">
                        <Presentation className="h-6 w-6 text-blue-600" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-gray-900">PPT演示文稿生成</h3>
                        <p className="text-sm text-gray-600">输入主题和内容，AI将为您生成专业的PPT演示文稿</p>
                      </div>
                    </div>
                    
                    <Separator className="my-4" />
                    
                    <div className="space-y-5">
                      <div className="space-y-2">
                        <Label htmlFor="ppt-topic" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
                          <span>主题</span>
                          <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="ppt-topic"
                          value={pptForm.topic}
                          onChange={(e) => setPptForm(prev => ({ ...prev, topic: e.target.value }))}
                          placeholder="例如：线性代数基础概念"
                          className="h-11 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="ppt-subject" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
                          <span>学科</span>
                          <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="ppt-subject"
                          value={pptForm.subject}
                          onChange={(e) => setPptForm(prev => ({ ...prev, subject: e.target.value }))}
                          placeholder="例如：数学、物理、化学等"
                          className="h-11 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="ppt-courseLevel" className="text-sm font-medium text-gray-700">课程层次</Label>
                        <Select value={pptForm.courseLevel} onValueChange={(value) => setPptForm(prev => ({ ...prev, courseLevel: value }))}>
                          <SelectTrigger className="h-11 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="undergraduate">本科</SelectItem>
                            <SelectItem value="graduate">研究生</SelectItem>
                            <SelectItem value="doctoral">博士</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-3">
                          <Label className="text-sm font-medium text-gray-700">
                            幻灯片数量: <span className="font-semibold text-blue-600">{pptForm.slideCount}</span>
                          </Label>
                          <div className="px-3">
                            <Slider
                              value={[pptForm.slideCount]}
                              onValueChange={(value) => setPptForm(prev => ({ ...prev, slideCount: value[0] }))}
                              max={30}
                              min={5}
                              step={1}
                              className="w-full"
                            />
                            <div className="flex justify-between text-xs text-gray-500 mt-1">
                              <span>5</span>
                              <span>30</span>
                            </div>
                          </div>
                        </div>
                        
                        <div className="space-y-2">
                          <Label htmlFor="ppt-style" className="text-sm font-medium text-gray-700">演示风格</Label>
                          <Select value={pptForm.style} onValueChange={(value) => setPptForm(prev => ({ ...prev, style: value }))}>
                            <SelectTrigger className="h-11 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="professional">🏢 专业商务</SelectItem>
                              <SelectItem value="academic">🎓 学术风格</SelectItem>
                              <SelectItem value="creative">🎨 创意活泼</SelectItem>
                              <SelectItem value="minimal">✨ 简约清新</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>
                      
                      <div className="pt-4">
                        <Button 
                          onClick={generatePPT} 
                          className="w-full h-12 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                          disabled={!pptForm.topic || !pptForm.subject}
                        >
                          <Sparkles className="h-5 w-5 mr-2" />
                          {!pptForm.topic || !pptForm.subject ? '请填写必填项' : '开始生成PPT'}
                        </Button>
                      </div>
                    </div>
                  </div>
                </TabsContent>
              
                {/* 习题生成 */}
                <TabsContent value="quiz" className="p-6 pt-4">
                  <div className="space-y-6">
                    <div className="flex items-center space-x-3">
                      <div className="p-2 bg-green-100 rounded-lg">
                        <FileQuestion className="h-6 w-6 text-green-600" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-gray-900">习题练习生成</h3>
                        <p className="text-sm text-gray-600">基于教学内容智能生成各类习题和练习</p>
                      </div>
                    </div>
                    
                    <Separator className="my-4" />
                    
                    <div className="space-y-5">
                      <div className="space-y-2">
                        <Label htmlFor="quiz-topic" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
                          <span>主题</span>
                          <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="quiz-topic"
                          value={quizForm.topic}
                          onChange={(e) => setQuizForm(prev => ({ ...prev, topic: e.target.value }))}
                          placeholder="例如：微积分导数计算"
                          className="h-11 border-gray-200 focus:border-green-500 focus:ring-green-500/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="quiz-subject" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
                          <span>学科</span>
                          <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="quiz-subject"
                          value={quizForm.subject}
                          onChange={(e) => setQuizForm(prev => ({ ...prev, subject: e.target.value }))}
                          placeholder="例如：数学、物理、化学等"
                          className="h-11 border-gray-200 focus:border-green-500 focus:ring-green-500/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="quiz-courseLevel" className="text-sm font-medium text-gray-700">课程层次</Label>
                        <Select value={quizForm.courseLevel} onValueChange={(value) => setQuizForm(prev => ({ ...prev, courseLevel: value }))}>
                          <SelectTrigger className="h-11 border-gray-200 focus:border-green-500 focus:ring-green-500/20">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="undergraduate">🎓 本科</SelectItem>
                            <SelectItem value="graduate">👨‍🎓 研究生</SelectItem>
                            <SelectItem value="doctoral">🏆 博士</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-3">
                          <Label className="text-sm font-medium text-gray-700">
                            题目数量: <span className="font-semibold text-green-600">{quizForm.questionCount[0]}</span>
                          </Label>
                          <div className="px-3">
                            <Slider
                              value={quizForm.questionCount}
                              onValueChange={(value) => setQuizForm(prev => ({ ...prev, questionCount: value }))}
                              max={20}
                              min={3}
                              step={1}
                              className="w-full"
                            />
                            <div className="flex justify-between text-xs text-gray-500 mt-1">
                              <span>3</span>
                              <span>20</span>
                            </div>
                          </div>
                        </div>
                        
                        <div className="space-y-2">
                          <Label htmlFor="quiz-difficulty" className="text-sm font-medium text-gray-700">难度等级</Label>
                          <Select value={quizForm.difficulty} onValueChange={(value) => setQuizForm(prev => ({ ...prev, difficulty: value }))}>
                            <SelectTrigger className="h-11 border-gray-200 focus:border-green-500 focus:ring-green-500/20">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="basic">🟢 基础</SelectItem>
                              <SelectItem value="intermediate">🟡 中等</SelectItem>
                              <SelectItem value="advanced">🔴 高级</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>
                      
                      <div className="pt-4">
                        <Button 
                          onClick={generateQuiz} 
                          className="w-full h-12 bg-gradient-to-r from-green-500 to-blue-600 hover:from-green-600 hover:to-blue-700 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                          disabled={!quizForm.topic || !quizForm.subject}
                        >
                          <Sparkles className="h-5 w-5 mr-2" />
                          {!quizForm.topic || !quizForm.subject ? '请填写必填项' : '开始生成习题'}
                        </Button>
                      </div>
                    </div>
                  </div>
                </TabsContent>
              
                {/* 讲解文本生成 */}
                <TabsContent value="explanation" className="p-6 pt-4">
                  <div className="space-y-6">
                    <div className="flex items-center space-x-3">
                      <div className="p-2 bg-orange-100 rounded-lg">
                        <BookOpen className="h-6 w-6 text-orange-600" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-gray-900">讲解文本生成</h3>
                        <p className="text-sm text-gray-600">生成详细的教学讲解文本和说明材料</p>
                      </div>
                    </div>
                    
                    <Separator className="my-4" />
                    
                    <div className="space-y-5">
                      <div className="space-y-2">
                        <Label htmlFor="explanation-topic" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
                          <span>主题</span>
                          <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="explanation-topic"
                          value={explanationForm.topic}
                          onChange={(e) => setExplanationForm(prev => ({ ...prev, topic: e.target.value }))}
                          placeholder="例如：函数极限的概念"
                          className="h-11 border-gray-200 focus:border-orange-500 focus:ring-orange-500/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="explanation-subject" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
                          <span>学科</span>
                          <span className="text-red-500">*</span>
                        </Label>
                        <Input
                          id="explanation-subject"
                          value={explanationForm.subject}
                          onChange={(e) => setExplanationForm(prev => ({ ...prev, subject: e.target.value }))}
                          placeholder="例如：数学、物理、化学等"
                          className="h-11 border-gray-200 focus:border-orange-500 focus:ring-orange-500/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="explanation-courseLevel" className="text-sm font-medium text-gray-700">课程层次</Label>
                        <Select value={explanationForm.courseLevel} onValueChange={(value) => setExplanationForm(prev => ({ ...prev, courseLevel: value }))}>
                          <SelectTrigger className="h-11 border-gray-200 focus:border-orange-500 focus:ring-orange-500/20">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="undergraduate">🎓 本科</SelectItem>
                            <SelectItem value="graduate">👨‍🎓 研究生</SelectItem>
                            <SelectItem value="doctoral">🏆 博士</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2">
                          <Label htmlFor="explanation-style" className="text-sm font-medium text-gray-700">讲解风格</Label>
                          <Select value={explanationForm.style} onValueChange={(value) => setExplanationForm(prev => ({ ...prev, style: value }))}>
                            <SelectTrigger className="h-11 border-gray-200 focus:border-orange-500 focus:ring-orange-500/20">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="rigorous">📚 严谨学术</SelectItem>
                              <SelectItem value="intuitive">💡 直观易懂</SelectItem>
                              <SelectItem value="applied">🔧 应用导向</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                        
                        <div className="space-y-2">
                          <Label htmlFor="explanation-length" className="text-sm font-medium text-gray-700">内容详细度</Label>
                          <Select value={explanationForm.length} onValueChange={(value) => setExplanationForm(prev => ({ ...prev, length: value }))}>
                            <SelectTrigger className="h-11 border-gray-200 focus:border-orange-500 focus:ring-orange-500/20">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="brief">📝 简要概述</SelectItem>
                              <SelectItem value="detailed">📄 详细讲解</SelectItem>
                              <SelectItem value="comprehensive">📚 全面深入</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>
                      
                      <div className="pt-4">
                        <Button 
                          onClick={generateExplanation} 
                          className="w-full h-12 bg-gradient-to-r from-orange-500 to-red-600 hover:from-orange-600 hover:to-red-700 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                          disabled={!explanationForm.topic || !explanationForm.subject}
                        >
                          <Sparkles className="h-5 w-5 mr-2" />
                          {!explanationForm.topic || !explanationForm.subject ? '请填写必填项' : '开始生成讲解'}
                        </Button>
                      </div>
                    </div>
                  </div>
                </TabsContent>
            </Tabs>
          </div>
          
          {/* 任务列表 */}
          <div>
            <div className="bg-white/80 backdrop-blur-sm rounded-2xl border border-white/50 shadow-xl">
              <div className="p-6 border-b border-gray-100">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="p-2 bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg">
                      <Clock className="h-5 w-5 text-white" />
                    </div>
                    <div>
                      <h3 className="text-xl font-bold text-gray-900">生成任务</h3>
                      <p className="text-sm text-gray-500">管理您的AI生成任务</p>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button variant="outline" size="sm" className="text-gray-600">
                      <RefreshCw className="h-4 w-4 mr-1" />
                      刷新
                    </Button>
                    <Button variant="outline" size="sm" className="text-gray-600">
                      <Settings className="h-4 w-4 mr-1" />
                      设置
                    </Button>
                  </div>
                </div>
              </div>
              
              <div className="p-6">
                {tasks.length === 0 ? (
                  <div className="text-center py-12 text-gray-500">
                    <div className="p-4 bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl w-24 h-24 mx-auto mb-6 flex items-center justify-center">
                      <Sparkles className="h-12 w-12 text-blue-400" />
                    </div>
                    <h4 className="text-lg font-medium text-gray-700 mb-2">暂无生成任务</h4>
                    <p className="text-sm text-gray-500">开始创建您的第一个AI资源</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {tasks.map((task) => (
                      <div key={task.id} className="group bg-gradient-to-r from-white to-gray-50/50 border border-gray-200/60 rounded-xl p-5 hover:shadow-lg hover:border-blue-200/60 transition-all duration-200">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-4">
                            <div className="relative">
                              <div className={`p-3 rounded-xl transition-colors duration-200 ${
                                task.status === 'completed' ? 'bg-green-100 text-green-600' :
                                task.status === 'failed' ? 'bg-red-100 text-red-600' :
                                'bg-blue-100 text-blue-600'
                              }`}>
                                {getTaskIcon(task.type)}
                              </div>
                              {task.status === 'completed' && (
                                <div className="absolute -top-1 -right-1 p-1 bg-green-500 rounded-full">
                                  <CheckCircle className="h-3 w-3 text-white" />
                                </div>
                              )}
                              {task.status === 'failed' && (
                                <div className="absolute -top-1 -right-1 p-1 bg-red-500 rounded-full">
                                  <XCircle className="h-3 w-3 text-white" />
                                </div>
                              )}
                              {task.status === 'processing' && (
                                <div className="absolute -top-1 -right-1 p-1 bg-blue-500 rounded-full animate-pulse">
                                  <Loader2 className="h-3 w-3 text-white animate-spin" />
                                </div>
                              )}
                            </div>
                            <div className="flex-1">
                              <div className="flex items-center space-x-2 mb-1">
                                <h4 className="font-semibold text-gray-900">{getTaskTitle(task.type)}</h4>
                                <Badge variant={task.status === 'completed' ? 'default' : task.status === 'failed' ? 'destructive' : 'secondary'} className="text-xs">
                                  {task.status === 'pending' && '等待中'}
                                  {task.status === 'processing' && '生成中'}
                                  {task.status === 'completed' && '已完成'}
                                  {task.status === 'failed' && '失败'}
                                </Badge>
                              </div>
                              <p className="text-sm text-gray-600 mb-2">{task.title}</p>
                              {task.status === 'processing' && (
                                <div className="w-48">
                                  <div className="flex items-center justify-between text-xs text-gray-500 mb-1">
                                    <span>进度</span>
                                    <span>{Math.round(task.progress)}%</span>
                                  </div>
                                  <Progress value={task.progress} className="h-2" />
                                </div>
                              )}
                            </div>
                          </div>
                          
                          <div className="flex items-center space-x-2">
                            {task.status === 'completed' && task.output && (
                              <>
                                <Button size="sm" variant="outline" className="text-blue-600 border-blue-200 hover:bg-blue-50">
                                  <Eye className="h-4 w-4 mr-1" />
                                  预览
                                </Button>
                                <Button size="sm" className="bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white">
                                  <Download className="h-4 w-4 mr-1" />
                                  下载
                                </Button>
                              </>
                            )}
                            {task.status === 'failed' && (
                              <Button size="sm" variant="outline" className="text-red-600 border-red-200 hover:bg-red-50">
                                <RefreshCw className="h-4 w-4 mr-1" />
                                重试
                              </Button>
                            )}
                            <Button size="sm" variant="ghost" className="text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity">
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                        
                        {task.error && (
                          <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                            <div className="flex items-start space-x-2">
                              <XCircle className="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
                              <div>
                                <h5 className="text-sm font-medium text-red-800 mb-1">生成失败</h5>
                                <p className="text-sm text-red-600">{task.error}</p>
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
      </main>
    </div>
  );
};

export default AIGeneration;