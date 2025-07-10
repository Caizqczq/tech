
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Progress } from '@/components/ui/progress';
import { 
  FileText, 
  Link, 
  Upload, 
  MessageSquare, 
  Sparkles, 
  CheckCircle, 
  Loader2,
  ArrowRight,
  ArrowLeft
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const CreateWizard = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedSource, setSelectedSource] = useState('');
  const [sourceContent, setSourceContent] = useState('');
  const [teachingConfig, setTeachingConfig] = useState({
    grade: '',
    style: '',
    outputs: []
  });
  const [isGenerating, setIsGenerating] = useState(false);
  const [generationSteps, setGenerationSteps] = useState([
    { id: 1, text: '正在解析您的需求...', status: 'pending' },
    { id: 2, text: '正在从知识库中检索相关内容...', status: 'pending' },
    { id: 3, text: '正在生成教学大纲...', status: 'pending' },
    { id: 4, text: '正在生成PPT课件...', status: 'pending' },
    { id: 5, text: '正在生成配图...', status: 'pending' },
    { id: 6, text: '正在生成测验题目...', status: 'pending' }
  ]);

  const sourceOptions = [
    {
      id: 'topic',
      title: '从一个主题开始',
      description: '输入您想要教学的主题',
      icon: MessageSquare,
      placeholder: '例如：光合作用、三角函数、中国古代诗歌...'
    },
    {
      id: 'text',
      title: '粘贴一段文本',
      description: '基于现有文本内容生成教学资源',
      icon: FileText,
      placeholder: '粘贴您的教学材料或参考文本...'
    },
    {
      id: 'url',
      title: '导入网页链接',
      description: '从在线资源生成教学内容',
      icon: Link,
      placeholder: 'https://example.com/article'
    },
    {
      id: 'file',
      title: '上传本地文件',
      description: '支持PDF、DOCX等格式',
      icon: Upload,
      placeholder: '拖拽文件到此处或点击上传'
    }
  ];

  const grades = [
    '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级',
    '初中一年级', '初中二年级', '初中三年级',
    '高中一年级', '高中二年级', '高中三年级'
  ];

  const styles = [
    { value: 'professional', label: '专业严谨', description: '注重学科准确性和系统性' },
    { value: 'interactive', label: '活泼有趣', description: '生动有趣，富有互动性' },
    { value: 'inspiring', label: '启发思考', description: '注重培养学生的思维能力' }
  ];

  const outputs = [
    { id: 'lesson_plan', label: '教案', description: '详细的教学计划和流程' },
    { id: 'ppt', label: 'PPT课件', description: '精美的演示文稿' },
    { id: 'images', label: '配图', description: '相关的教学图片和插图' },
    { id: 'quiz', label: '随堂测验', description: '检测学习效果的题目' }
  ];

  const handleSourceSelect = (sourceId: string) => {
    setSelectedSource(sourceId);
    setSourceContent('');
  };

  const handleNext = () => {
    if (currentStep === 1 && (!selectedSource || !sourceContent)) {
      return;
    }
    if (currentStep === 2 && (!teachingConfig.grade || !teachingConfig.style || teachingConfig.outputs.length === 0)) {
      return;
    }
    if (currentStep === 2) {
      startGeneration();
    } else {
      setCurrentStep(prev => Math.min(prev + 1, 3));
    }
  };

  const handleBack = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1));
  };

  const startGeneration = () => {
    setCurrentStep(3);
    setIsGenerating(true);
    
    // 模拟AI生成过程
    const steps = [...generationSteps];
    let currentStepIndex = 0;
    
    const updateStep = () => {
      if (currentStepIndex < steps.length) {
        steps[currentStepIndex].status = 'completed';
        if (currentStepIndex + 1 < steps.length) {
          steps[currentStepIndex + 1].status = 'active';
        }
        setGenerationSteps([...steps]);
        currentStepIndex++;
        
        if (currentStepIndex < steps.length) {
          setTimeout(updateStep, 2000);
        } else {
          setTimeout(() => {
            setIsGenerating(false);
            navigate('/project/demo');
          }, 1000);
        }
      }
    };
    
    setTimeout(() => {
      steps[0].status = 'active';
      setGenerationSteps([...steps]);
      setTimeout(updateStep, 2000);
    }, 500);
  };

  const handleOutputToggle = (outputId: string) => {
    setTeachingConfig(prev => ({
      ...prev,
      outputs: prev.outputs.includes(outputId) 
        ? prev.outputs.filter(id => id !== outputId)
        : [...prev.outputs, outputId]
    }));
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      {/* 返回按钮 */}
      <div className="p-6">
        <Button 
          variant="ghost" 
          onClick={() => navigate('/dashboard')}
          className="text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          返回仪表盘
        </Button>
      </div>

      <div className="max-w-4xl mx-auto px-6 py-8">
        {/* 步骤指示器 */}
        <div className="mb-8">
          <div className="flex items-center justify-center space-x-8">
            {[1, 2, 3].map((step) => (
              <div key={step} className="flex items-center">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold ${
                  currentStep >= step 
                    ? 'bg-purple-600 text-white' 
                    : 'bg-gray-200 text-gray-500'
                }`}>
                  {step}
                </div>
                {step < 3 && (
                  <div className={`w-16 h-1 mx-4 ${
                    currentStep > step ? 'bg-purple-600' : 'bg-gray-200'
                  }`}></div>
                )}
              </div>
            ))}
          </div>
          <div className="flex justify-center space-x-20 mt-4">
            <span className={`text-sm ${currentStep >= 1 ? 'text-purple-600 font-medium' : 'text-gray-500'}`}>
              定义源材料
            </span>
            <span className={`text-sm ${currentStep >= 2 ? 'text-purple-600 font-medium' : 'text-gray-500'}`}>
              设定教学目标
            </span>
            <span className={`text-sm ${currentStep >= 3 ? 'text-purple-600 font-medium' : 'text-gray-500'}`}>
              AI生成魔法
            </span>
          </div>
        </div>

        {/* 步骤一：定义源材料 */}
        {currentStep === 1 && (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-2">定义您的源材料</h2>
              <p className="text-lg text-gray-600">选择一种方式开始您的教学设计</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {sourceOptions.map((option) => (
                <Card 
                  key={option.id}
                  className={`cursor-pointer transition-all duration-300 hover:shadow-lg ${
                    selectedSource === option.id 
                      ? 'ring-2 ring-purple-500 bg-purple-50' 
                      : 'hover:shadow-md'
                  }`}
                  onClick={() => handleSourceSelect(option.id)}
                >
                  <CardHeader className="pb-4">
                    <div className="flex items-center space-x-3">
                      <div className={`p-2 rounded-lg ${
                        selectedSource === option.id ? 'bg-purple-600 text-white' : 'bg-gray-100 text-gray-600'
                      }`}>
                        <option.icon className="h-5 w-5" />
                      </div>
                      <div>
                        <CardTitle className="text-lg">{option.title}</CardTitle>
                        <CardDescription>{option.description}</CardDescription>
                      </div>
                    </div>
                  </CardHeader>
                </Card>
              ))}
            </div>

            {/* 输入区域 */}
            {selectedSource && (
              <Card className="mt-6 animate-fade-in">
                <CardContent className="p-6">
                  {selectedSource === 'topic' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        教学主题
                      </label>
                      <Input
                        value={sourceContent}
                        onChange={(e) => setSourceContent(e.target.value)}
                        placeholder="例如：光合作用的原理与过程"
                        className="w-full"
                      />
                    </div>
                  )}
                  {selectedSource === 'text' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        教学文本内容
                      </label>
                      <Textarea
                        value={sourceContent}
                        onChange={(e) => setSourceContent(e.target.value)}
                        placeholder="粘贴您的教学材料或参考文本..."
                        rows={8}
                        className="w-full"
                      />
                    </div>
                  )}
                  {selectedSource === 'url' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        网页链接
                      </label>
                      <Input
                        value={sourceContent}
                        onChange={(e) => setSourceContent(e.target.value)}
                        placeholder="https://example.com/article"
                        className="w-full"
                      />
                    </div>
                  )}
                  {selectedSource === 'file' && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        上传文件
                      </label>
                      <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-purple-400 transition-colors">
                        <Upload className="mx-auto h-12 w-12 text-gray-400 mb-4" />
                        <p className="text-gray-600">拖拽文件到此处或点击上传</p>
                        <p className="text-sm text-gray-500 mt-2">支持 PDF, DOCX, TXT 格式</p>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}
          </div>
        )}

        {/* 步骤二：设定教学目标 */}
        {currentStep === 2 && (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-2">设定教学目标</h2>
              <p className="text-lg text-gray-600">定制您的教学需求和风格</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* 左侧配置 */}
              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle>教学对象</CardTitle>
                    <CardDescription>选择目标学生的年级水平</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Select value={teachingConfig.grade} onValueChange={(value) => 
                      setTeachingConfig(prev => ({...prev, grade: value}))
                    }>
                      <SelectTrigger>
                        <SelectValue placeholder="选择年级" />
                      </SelectTrigger>
                      <SelectContent>
                        {grades.map((grade) => (
                          <SelectItem key={grade} value={grade}>{grade}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>教学风格</CardTitle>
                    <CardDescription>选择适合的教学方式</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {styles.map((style) => (
                      <div 
                        key={style.value}
                        className={`p-3 rounded-lg border cursor-pointer transition-all ${
                          teachingConfig.style === style.value
                            ? 'border-purple-500 bg-purple-50'
                            : 'border-gray-200 hover:border-gray-300'
                        }`}
                        onClick={() => setTeachingConfig(prev => ({...prev, style: style.value}))}
                      >
                        <div className="flex items-center space-x-3">
                          <div className={`w-4 h-4 rounded-full border-2 ${
                            teachingConfig.style === style.value
                              ? 'border-purple-500 bg-purple-500'
                              : 'border-gray-300'
                          }`}></div>
                          <div>
                            <p className="font-medium text-gray-900">{style.label}</p>
                            <p className="text-sm text-gray-600">{style.description}</p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>
              </div>

              {/* 右侧输出选择 */}
              <div>
                <Card>
                  <CardHeader>
                    <CardTitle>期望产出</CardTitle>
                    <CardDescription>选择您需要生成的教学资源（可多选）</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {outputs.map((output) => (
                      <div key={output.id} className="flex items-start space-x-3">
                        <Checkbox
                          id={output.id}
                          checked={teachingConfig.outputs.includes(output.id)}
                          onCheckedChange={() => handleOutputToggle(output.id)}
                          className="mt-1"
                        />
                        <div className="flex-1">
                          <label 
                            htmlFor={output.id}
                            className="font-medium text-gray-900 cursor-pointer"
                          >
                            {output.label}
                          </label>
                          <p className="text-sm text-gray-600">{output.description}</p>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>
              </div>
            </div>
          </div>
        )}

        {/* 步骤三：AI生成过程 */}
        {currentStep === 3 && (
          <div className="space-y-8">
            <div className="text-center">
              <div className="mb-6">
                <Sparkles className="mx-auto h-16 w-16 text-purple-600 animate-pulse" />
              </div>
              <h2 className="text-3xl font-bold text-gray-900 mb-2">AI魔法生成中...</h2>
              <p className="text-lg text-gray-600">请稍候，我们正在为您创造教学奇迹</p>
            </div>

            <Card className="bg-gradient-to-r from-purple-50 to-blue-50 border-none">
              <CardContent className="p-8">
                <div className="space-y-6">
                  {generationSteps.map((step, index) => (
                    <div key={step.id} className="flex items-center space-x-4">
                      <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${
                        step.status === 'completed' 
                          ? 'bg-green-500 text-white'
                          : step.status === 'active'
                          ? 'bg-purple-600 text-white'
                          : 'bg-gray-200 text-gray-500'
                      }`}>
                        {step.status === 'completed' ? (
                          <CheckCircle className="h-5 w-5" />
                        ) : step.status === 'active' ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          <span className="text-sm font-medium">{index + 1}</span>
                        )}
                      </div>
                      <div className="flex-1">
                        <p className={`font-medium ${
                          step.status === 'completed' ? 'text-green-700' :
                          step.status === 'active' ? 'text-purple-700' :
                          'text-gray-500'
                        }`}>
                          {step.text}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* 进度条 */}
            <div className="space-y-2">
              <div className="flex justify-between text-sm text-gray-600">
                <span>生成进度</span>
                <span>{Math.round((generationSteps.filter(s => s.status === 'completed').length / generationSteps.length) * 100)}%</span>
              </div>
              <Progress 
                value={(generationSteps.filter(s => s.status === 'completed').length / generationSteps.length) * 100}
                className="h-2"
              />
            </div>
          </div>
        )}

        {/* 操作按钮 */}
        <div className="flex justify-between mt-12">
          <Button
            variant="outline"
            onClick={handleBack}
            disabled={currentStep === 1 || isGenerating}
            className="px-6"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            上一步
          </Button>
          
          {currentStep < 3 && (
            <Button
              onClick={handleNext}
              disabled={
                (currentStep === 1 && (!selectedSource || !sourceContent)) ||
                (currentStep === 2 && (!teachingConfig.grade || !teachingConfig.style || teachingConfig.outputs.length === 0))
              }
              className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 px-6"
            >
              {currentStep === 2 ? '开始生成' : '下一步'}
              <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default CreateWizard;
