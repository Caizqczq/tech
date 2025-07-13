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
import { Presentation, FileQuestion, BookOpen, Sparkles, Download, Eye, Loader2 } from 'lucide-react';
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
    content: '',
    slides: [10],
    style: 'professional'
  });
  
  // 习题生成表单
  const [quizForm, setQuizForm] = useState({
    topic: '',
    content: '',
    questionCount: [5],
    difficulty: 'medium',
    questionTypes: ['multiple_choice']
  });
  
  // 讲解文本生成表单
  const [explanationForm, setExplanationForm] = useState({
    topic: '',
    content: '',
    level: 'undergraduate',
    style: 'detailed'
  });

  const generatePPT = async () => {
    try {
      const response = await apiService.generatePPT({
        topic: pptForm.topic,
        content: pptForm.content,
        slides: pptForm.slides[0],
        style: pptForm.style
      });
      
      if (response.success) {
        const newTask: GenerationTask = {
          id: response.data.taskId,
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
        
        // 模拟进度更新
        simulateProgress(response.data.taskId);
      }
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
        content: quizForm.content,
        questionCount: quizForm.questionCount[0],
        difficulty: quizForm.difficulty,
        questionTypes: quizForm.questionTypes
      });
      
      if (response.success) {
        const newTask: GenerationTask = {
          id: response.data.taskId,
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
        
        simulateProgress(response.data.taskId);
      }
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
        content: explanationForm.content,
        level: explanationForm.level,
        style: explanationForm.style
      });
      
      if (response.success) {
        const newTask: GenerationTask = {
          id: response.data.taskId,
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
        
        simulateProgress(response.data.taskId);
      }
    } catch (error: any) {
      toast({
        title: "生成失败",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  const simulateProgress = (taskId: string) => {
    const interval = setInterval(() => {
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
    }, 1000);
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
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      <DashboardHeader />
      
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">AI资源自动制作</h1>
          <p className="text-gray-600">使用AI智能生成PPT、习题和讲解文本，提升教学效率</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 生成表单 */}
          <div className="lg:col-span-2">
            <Tabs value={activeTab} onValueChange={setActiveTab}>
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="ppt" className="flex items-center space-x-2">
                  <Presentation className="h-4 w-4" />
                  <span>生成PPT</span>
                </TabsTrigger>
                <TabsTrigger value="quiz" className="flex items-center space-x-2">
                  <FileQuestion className="h-4 w-4" />
                  <span>生成习题</span>
                </TabsTrigger>
                <TabsTrigger value="explanation" className="flex items-center space-x-2">
                  <BookOpen className="h-4 w-4" />
                  <span>生成讲解</span>
                </TabsTrigger>
              </TabsList>
              
              {/* PPT生成 */}
              <TabsContent value="ppt">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <Presentation className="h-5 w-5 text-blue-600" />
                      <span>PPT演示文稿生成</span>
                    </CardTitle>
                    <CardDescription>
                      输入主题和内容，AI将为您生成专业的PPT演示文稿
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div>
                      <Label htmlFor="ppt-topic">主题</Label>
                      <Input
                        id="ppt-topic"
                        value={pptForm.topic}
                        onChange={(e) => setPptForm(prev => ({ ...prev, topic: e.target.value }))}
                        placeholder="例如：线性代数基础概念"
                      />
                    </div>
                    
                    <div>
                      <Label htmlFor="ppt-content">内容描述</Label>
                      <Textarea
                        id="ppt-content"
                        value={pptForm.content}
                        onChange={(e) => setPptForm(prev => ({ ...prev, content: e.target.value }))}
                        placeholder="详细描述您希望PPT包含的内容..."
                        rows={4}
                      />
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <Label>幻灯片数量: {pptForm.slides[0]}</Label>
                        <Slider
                          value={pptForm.slides}
                          onValueChange={(value) => setPptForm(prev => ({ ...prev, slides: value }))}
                          max={30}
                          min={5}
                          step={1}
                          className="mt-2"
                        />
                      </div>
                      
                      <div>
                        <Label htmlFor="ppt-style">演示风格</Label>
                        <Select value={pptForm.style} onValueChange={(value) => setPptForm(prev => ({ ...prev, style: value }))}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="professional">专业商务</SelectItem>
                            <SelectItem value="academic">学术风格</SelectItem>
                            <SelectItem value="creative">创意活泼</SelectItem>
                            <SelectItem value="minimal">简约清新</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                    
                    <Button 
                      onClick={generatePPT} 
                      className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
                      disabled={!pptForm.topic || !pptForm.content}
                    >
                      <Sparkles className="h-4 w-4 mr-2" />
                      生成PPT
                    </Button>
                  </CardContent>
                </Card>
              </TabsContent>
              
              {/* 习题生成 */}
              <TabsContent value="quiz">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <FileQuestion className="h-5 w-5 text-green-600" />
                      <span>习题练习生成</span>
                    </CardTitle>
                    <CardDescription>
                      基于教学内容智能生成各类习题和练习
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div>
                      <Label htmlFor="quiz-topic">主题</Label>
                      <Input
                        id="quiz-topic"
                        value={quizForm.topic}
                        onChange={(e) => setQuizForm(prev => ({ ...prev, topic: e.target.value }))}
                        placeholder="例如：微积分导数计算"
                      />
                    </div>
                    
                    <div>
                      <Label htmlFor="quiz-content">教学内容</Label>
                      <Textarea
                        id="quiz-content"
                        value={quizForm.content}
                        onChange={(e) => setQuizForm(prev => ({ ...prev, content: e.target.value }))}
                        placeholder="输入相关的教学内容和知识点..."
                        rows={4}
                      />
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <Label>题目数量: {quizForm.questionCount[0]}</Label>
                        <Slider
                          value={quizForm.questionCount}
                          onValueChange={(value) => setQuizForm(prev => ({ ...prev, questionCount: value }))}
                          max={20}
                          min={3}
                          step={1}
                          className="mt-2"
                        />
                      </div>
                      
                      <div>
                        <Label htmlFor="quiz-difficulty">难度等级</Label>
                        <Select value={quizForm.difficulty} onValueChange={(value) => setQuizForm(prev => ({ ...prev, difficulty: value }))}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="easy">简单</SelectItem>
                            <SelectItem value="medium">中等</SelectItem>
                            <SelectItem value="hard">困难</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                    
                    <Button 
                      onClick={generateQuiz} 
                      className="w-full bg-gradient-to-r from-green-500 to-blue-600 hover:from-green-600 hover:to-blue-700"
                      disabled={!quizForm.topic || !quizForm.content}
                    >
                      <Sparkles className="h-4 w-4 mr-2" />
                      生成习题
                    </Button>
                  </CardContent>
                </Card>
              </TabsContent>
              
              {/* 讲解文本生成 */}
              <TabsContent value="explanation">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center space-x-2">
                      <BookOpen className="h-5 w-5 text-orange-600" />
                      <span>讲解文本生成</span>
                    </CardTitle>
                    <CardDescription>
                      生成详细的教学讲解文本和说明材料
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div>
                      <Label htmlFor="explanation-topic">主题</Label>
                      <Input
                        id="explanation-topic"
                        value={explanationForm.topic}
                        onChange={(e) => setExplanationForm(prev => ({ ...prev, topic: e.target.value }))}
                        placeholder="例如：函数极限的概念"
                      />
                    </div>
                    
                    <div>
                      <Label htmlFor="explanation-content">内容要点</Label>
                      <Textarea
                        id="explanation-content"
                        value={explanationForm.content}
                        onChange={(e) => setExplanationForm(prev => ({ ...prev, content: e.target.value }))}
                        placeholder="列出需要讲解的关键概念和要点..."
                        rows={4}
                      />
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="explanation-level">教学层次</Label>
                        <Select value={explanationForm.level} onValueChange={(value) => setExplanationForm(prev => ({ ...prev, level: value }))}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="undergraduate">本科生</SelectItem>
                            <SelectItem value="graduate">研究生</SelectItem>
                            <SelectItem value="advanced">高级课程</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      
                      <div>
                        <Label htmlFor="explanation-style">讲解风格</Label>
                        <Select value={explanationForm.style} onValueChange={(value) => setExplanationForm(prev => ({ ...prev, style: value }))}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="detailed">详细深入</SelectItem>
                            <SelectItem value="concise">简洁明了</SelectItem>
                            <SelectItem value="interactive">互动式</SelectItem>
                            <SelectItem value="example_rich">案例丰富</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                    
                    <Button 
                      onClick={generateExplanation} 
                      className="w-full bg-gradient-to-r from-orange-500 to-red-600 hover:from-orange-600 hover:to-red-700"
                      disabled={!explanationForm.topic || !explanationForm.content}
                    >
                      <Sparkles className="h-4 w-4 mr-2" />
                      生成讲解
                    </Button>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>
          
          {/* 任务列表 */}
          <div>
            <Card>
              <CardHeader>
                <CardTitle>生成任务</CardTitle>
                <CardDescription>查看AI资源生成进度和结果</CardDescription>
              </CardHeader>
              <CardContent>
                {tasks.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">
                    <Sparkles className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                    <p>暂无生成任务</p>
                    <p className="text-sm">开始创建您的第一个AI资源</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {tasks.map((task) => (
                      <div key={task.id} className="border rounded-lg p-4">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center space-x-2">
                            {getTaskIcon(task.type)}
                            <span className="font-medium">{getTaskTitle(task.type)}</span>
                          </div>
                          <Badge variant={task.status === 'completed' ? 'default' : task.status === 'failed' ? 'destructive' : 'secondary'}>
                            {task.status === 'pending' && '等待中'}
                            {task.status === 'processing' && '生成中'}
                            {task.status === 'completed' && '已完成'}
                            {task.status === 'failed' && '失败'}
                          </Badge>
                        </div>
                        
                        <p className="text-sm text-gray-600 mb-2">{task.title}</p>
                        
                        {task.status === 'processing' && (
                          <div className="mb-3">
                            <div className="flex items-center space-x-2 mb-1">
                              <Loader2 className="h-3 w-3 animate-spin" />
                              <span className="text-xs text-gray-500">生成进度: {Math.round(task.progress)}%</span>
                            </div>
                            <Progress value={task.progress} className="h-2" />
                          </div>
                        )}
                        
                        {task.status === 'completed' && task.output && (
                          <div className="flex space-x-2 mt-3">
                            <Button size="sm" variant="outline" className="flex-1">
                              <Eye className="h-3 w-3 mr-1" />
                              预览
                            </Button>
                            <Button size="sm" className="flex-1">
                              <Download className="h-3 w-3 mr-1" />
                              下载
                            </Button>
                          </div>
                        )}
                        
                        {task.error && (
                          <p className="text-xs text-red-500 mt-2">{task.error}</p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AIGeneration;