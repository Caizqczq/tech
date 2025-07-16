import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Slider } from '@/components/ui/slider';
import { Separator } from '@/components/ui/separator';
import { Presentation, Sparkles } from 'lucide-react';

interface PPTFormData {
  topic: string;
  subject: string;
  courseLevel: string;
  slideCount: number;
  style: string;
}

interface PPTGenerationFormProps {
  onGenerate: (formData: PPTFormData) => void;
  isGenerating?: boolean;
}

const PPTGenerationForm: React.FC<PPTGenerationFormProps> = ({
  onGenerate,
  isGenerating = false,
}) => {
  const [formData, setFormData] = useState<PPTFormData>({
    topic: '',
    subject: '',
    courseLevel: 'undergraduate',
    slideCount: 10,
    style: 'professional'
  });

  const handleSubmit = () => {
    if (!formData.topic || !formData.subject) return;
    onGenerate(formData);
  };

  const updateFormData = (field: keyof PPTFormData, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <Card className="bg-white/80 backdrop-blur-sm border-0 shadow-xl">
      <CardHeader>
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-blue-100 rounded-lg">
            <Presentation className="h-6 w-6 text-blue-600" />
          </div>
          <div>
            <CardTitle className="text-xl font-semibold text-gray-900">PPT演示文稿生成</CardTitle>
            <p className="text-sm text-gray-600">输入主题和内容，AI将为您生成专业的PPT演示文稿</p>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-6">
        <Separator />
        
        <div className="space-y-5">
          <div className="space-y-2">
            <Label htmlFor="ppt-topic" className="text-sm font-medium text-gray-700 flex items-center space-x-1">
              <span>主题</span>
              <span className="text-red-500">*</span>
            </Label>
            <Input
              id="ppt-topic"
              value={formData.topic}
              onChange={(e) => updateFormData('topic', e.target.value)}
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
              value={formData.subject}
              onChange={(e) => updateFormData('subject', e.target.value)}
              placeholder="例如：数学、物理、化学等"
              className="h-11 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="ppt-courseLevel" className="text-sm font-medium text-gray-700">课程层次</Label>
            <Select 
              value={formData.courseLevel} 
              onValueChange={(value) => updateFormData('courseLevel', value)}
            >
              <SelectTrigger className="h-11 border-gray-200 focus:border-blue-500 focus:ring-blue-500/20">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="undergraduate">本科生</SelectItem>
                <SelectItem value="graduate">研究生</SelectItem>
                <SelectItem value="high_school">高中</SelectItem>
                <SelectItem value="middle_school">初中</SelectItem>
              </SelectContent>
            </Select>
          </div>
        
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-3">
              <Label className="text-sm font-medium text-gray-700">
                幻灯片数量: <span className="font-semibold text-blue-600">{formData.slideCount}</span>
              </Label>
              <div className="px-3">
                <Slider
                  value={[formData.slideCount]}
                  onValueChange={(value) => updateFormData('slideCount', value[0])}
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
              <Select 
                value={formData.style} 
                onValueChange={(value) => updateFormData('style', value)}
              >
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
              onClick={handleSubmit}
              className="w-full h-12 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-medium rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={!formData.topic || !formData.subject || isGenerating}
            >
              <Sparkles className="h-5 w-5 mr-2" />
              {isGenerating ? '生成中...' : 
               (!formData.topic || !formData.subject ? '请填写必填项' : '开始生成PPT')}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default PPTGenerationForm;