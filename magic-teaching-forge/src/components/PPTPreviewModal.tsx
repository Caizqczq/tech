import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useToast } from '@/hooks/use-toast';
import { 
  Edit3, 
  Save, 
  Download, 
  RotateCcw, 
  Plus, 
  Trash2, 
  FileText,
  Calendar,
  Layers,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import { apiService } from '@/services/api';

interface PPTSlide {
  id: string;
  title: string;
  content: string[];
  isTitle?: boolean;
}

interface PPTPreviewData {
  taskId: string;
  content: string;
  fileName: string;
  fileSize: number;
  topic: string;
  generatedAt: string;
  slides?: PPTSlide[];
}

interface PPTPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  taskId: string;
  onDownload: () => void;
}

export const PPTPreviewModal: React.FC<PPTPreviewModalProps> = ({
  isOpen,
  onClose,
  taskId,
  onDownload
}) => {
  const [previewData, setPreviewData] = useState<PPTPreviewData | null>(null);
  const [editedSlides, setEditedSlides] = useState<PPTSlide[]>([]);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [currentSlideIndex, setCurrentSlideIndex] = useState(0);
  const [viewMode, setViewMode] = useState<'grid' | 'single'>('grid');
  const { toast } = useToast();

  // 加载预览数据
  useEffect(() => {
    if (isOpen && taskId) {
      loadPreviewData();
    }
  }, [isOpen, taskId]);

  const loadPreviewData = async () => {
    setIsLoading(true);
    try {
      const data = await apiService.previewFile(taskId);
      const parsedSlides = parseContentToSlides(data.content, data.topic);
      const previewDataWithSlides = {
        ...data,
        slides: parsedSlides
      };
      setPreviewData(previewDataWithSlides);
      setEditedSlides(parsedSlides);
    } catch (error: any) {
      toast({
        title: "加载失败",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 解析文本内容为幻灯片结构
  const parseContentToSlides = (content: string, topic: string): PPTSlide[] => {
    const slides: PPTSlide[] = [];
    
    // 添加标题页
    slides.push({
      id: 'title-slide',
      title: topic,
      content: ['AI生成的演示文稿', '智能教学助手'],
      isTitle: true
    });

    // 解析内容页
    const slidePattern = /【幻灯片\d+】(.+?)(?=【幻灯片\d+】|$)/gs;
    let match;
    let slideIndex = 1;

    while ((match = slidePattern.exec(content)) !== null) {
      const slideContent = match[1].trim();
      const lines = slideContent.split('\n').filter(line => line.trim());
      
      if (lines.length > 0) {
        const title = lines[0].trim();
        const contentLines = lines.slice(1).filter(line => line.trim());
        
        slides.push({
          id: `slide-${slideIndex}`,
          title,
          content: contentLines,
          isTitle: false
        });
        slideIndex++;
      }
    }

    // 如果没有匹配到标准格式，按段落分割
    if (slides.length === 1) {
      const paragraphs = content.split('\n\n').filter(p => p.trim());
      paragraphs.forEach((paragraph, index) => {
        const lines = paragraph.split('\n').filter(line => line.trim());
        if (lines.length > 0) {
          slides.push({
            id: `slide-${index + 1}`,
            title: lines[0] || `幻灯片 ${index + 1}`,
            content: lines.slice(1),
            isTitle: false
          });
        }
      });
    }

    return slides;
  };

  // 编辑幻灯片标题
  const updateSlideTitle = (slideId: string, newTitle: string) => {
    setEditedSlides(prev => 
      prev.map(slide => 
        slide.id === slideId ? { ...slide, title: newTitle } : slide
      )
    );
  };

  // 编辑幻灯片内容
  const updateSlideContent = (slideId: string, newContent: string[]) => {
    setEditedSlides(prev => 
      prev.map(slide => 
        slide.id === slideId ? { ...slide, content: newContent } : slide
      )
    );
  };

  // 添加内容要点
  const addContentPoint = (slideId: string) => {
    setEditedSlides(prev => 
      prev.map(slide => 
        slide.id === slideId 
          ? { ...slide, content: [...slide.content, '新要点'] }
          : slide
      )
    );
  };

  // 删除内容要点
  const removeContentPoint = (slideId: string, pointIndex: number) => {
    setEditedSlides(prev => 
      prev.map(slide => 
        slide.id === slideId 
          ? { 
              ...slide, 
              content: slide.content.filter((_, index) => index !== pointIndex)
            }
          : slide
      )
    );
  };

  // 重置到原始版本
  const resetToOriginal = () => {
    if (previewData?.slides) {
      setEditedSlides(previewData.slides);
      setIsEditing(false);
      toast({
        title: "已重置",
        description: "内容已恢复到原始版本",
      });
    }
  };

  // 保存修改并重新生成
  const saveAndRegenerate = async () => {
    setIsSaving(true);
    try {
      // 构建新的内容格式
      const newContent = editedSlides
        .filter(slide => !slide.isTitle)
        .map((slide, index) => {
          const contentText = slide.content.join('\n');
          return `【幻灯片${index + 1}】${slide.title}\n${contentText}`;
        })
        .join('\n\n');

      // 调用后端API重新生成PPT
      await apiService.regeneratePPT(taskId, {
        content: newContent,
        slides: editedSlides
      });

      toast({
        title: "保存成功",
        description: "PPT已重新生成，可以重新下载",
      });
      
      setIsEditing(false);
      // 重新加载数据
      await loadPreviewData();
      
    } catch (error: any) {
      toast({
        title: "保存失败",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setIsSaving(false);
    }
  };

  if (!previewData) {
    return null;
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-6xl max-h-[90vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            PPT预览 - {previewData.topic}
          </DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="flex items-center justify-center h-96">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-2">加载中...</span>
          </div>
        ) : (
          <div className="flex flex-col h-full">
            {/* 信息栏 */}
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg mb-4">
              <div className="flex items-center gap-4">
                <Badge variant="outline" className="flex items-center gap-1">
                  <Layers className="h-3 w-3" />
                  {editedSlides.length} 张幻灯片
                </Badge>
                <Badge variant="outline" className="flex items-center gap-1">
                  <Calendar className="h-3 w-3" />
                  {new Date(previewData.generatedAt).toLocaleString()}
                </Badge>
                <Badge variant="outline">
                  {(previewData.fileSize / 1024).toFixed(1)} KB
                </Badge>
              </div>
              
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setViewMode(viewMode === 'grid' ? 'single' : 'grid')}
                >
                  {viewMode === 'grid' ? '单页视图' : '网格视图'}
                </Button>
                
                {!isEditing ? (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setIsEditing(true)}
                    className="flex items-center gap-1"
                  >
                    <Edit3 className="h-4 w-4" />
                    编辑
                  </Button>
                ) : (
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={resetToOriginal}
                      className="flex items-center gap-1"
                    >
                      <RotateCcw className="h-4 w-4" />
                      重置
                    </Button>
                    <Button
                      variant="default"
                      size="sm"
                      onClick={saveAndRegenerate}
                      disabled={isSaving}
                      className="flex items-center gap-1"
                    >
                      <Save className="h-4 w-4" />
                      {isSaving ? '保存中...' : '保存'}
                    </Button>
                  </div>
                )}
              </div>
            </div>

            {/* 幻灯片内容 */}
            <ScrollArea className="flex-1">
              {viewMode === 'grid' ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-4">
                  {editedSlides.map((slide, index) => (
                    <SlideCard
                      key={slide.id}
                      slide={slide}
                      index={index}
                      isEditing={isEditing}
                      onUpdateTitle={(title) => updateSlideTitle(slide.id, title)}
                      onUpdateContent={(content) => updateSlideContent(slide.id, content)}
                      onAddPoint={() => addContentPoint(slide.id)}
                      onRemovePoint={(pointIndex) => removeContentPoint(slide.id, pointIndex)}
                    />
                  ))}
                </div>
              ) : (
                <div className="p-4">
                  <div className="flex items-center justify-between mb-4">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentSlideIndex(Math.max(0, currentSlideIndex - 1))}
                      disabled={currentSlideIndex === 0}
                    >
                      <ChevronLeft className="h-4 w-4" />
                      上一张
                    </Button>
                    
                    <span className="text-sm text-gray-600">
                      {currentSlideIndex + 1} / {editedSlides.length}
                    </span>
                    
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentSlideIndex(Math.min(editedSlides.length - 1, currentSlideIndex + 1))}
                      disabled={currentSlideIndex === editedSlides.length - 1}
                    >
                      下一张
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </div>
                  
                  <SlideCard
                    slide={editedSlides[currentSlideIndex]}
                    index={currentSlideIndex}
                    isEditing={isEditing}
                    onUpdateTitle={(title) => updateSlideTitle(editedSlides[currentSlideIndex].id, title)}
                    onUpdateContent={(content) => updateSlideContent(editedSlides[currentSlideIndex].id, content)}
                    onAddPoint={() => addContentPoint(editedSlides[currentSlideIndex].id)}
                    onRemovePoint={(pointIndex) => removeContentPoint(editedSlides[currentSlideIndex].id, pointIndex)}
                    isSingleView={true}
                  />
                </div>
              )}
            </ScrollArea>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            关闭
          </Button>
          <Button onClick={onDownload} className="flex items-center gap-1">
            <Download className="h-4 w-4" />
            下载PPT
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

// 幻灯片卡片组件
interface SlideCardProps {
  slide: PPTSlide;
  index: number;
  isEditing: boolean;
  onUpdateTitle: (title: string) => void;
  onUpdateContent: (content: string[]) => void;
  onAddPoint: () => void;
  onRemovePoint: (index: number) => void;
  isSingleView?: boolean;
}

const SlideCard: React.FC<SlideCardProps> = ({
  slide,
  index,
  isEditing,
  onUpdateTitle,
  onUpdateContent,
  onAddPoint,
  onRemovePoint,
  isSingleView = false
}) => {
  const updateContentPoint = (pointIndex: number, newValue: string) => {
    const newContent = [...slide.content];
    newContent[pointIndex] = newValue;
    onUpdateContent(newContent);
  };

  return (
    <Card className={`${isSingleView ? 'w-full max-w-4xl mx-auto' : ''} ${slide.isTitle ? 'border-blue-200 bg-blue-50' : ''}`}>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <Badge variant={slide.isTitle ? 'default' : 'secondary'} className="text-xs">
            {slide.isTitle ? '标题页' : `第${index}页`}
          </Badge>
          {isEditing && !slide.isTitle && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onAddPoint}
              className="h-6 w-6 p-0"
            >
              <Plus className="h-3 w-3" />
            </Button>
          )}
        </div>
        
        {isEditing ? (
          <Input
            value={slide.title}
            onChange={(e) => onUpdateTitle(e.target.value)}
            className="font-semibold"
            placeholder="幻灯片标题"
          />
        ) : (
          <CardTitle className={`${isSingleView ? 'text-2xl' : 'text-lg'}`}>
            {slide.title}
          </CardTitle>
        )}
      </CardHeader>
      
      <CardContent>
        <div className="space-y-2">
          {slide.content.map((point, pointIndex) => (
            <div key={pointIndex} className="flex items-start gap-2">
              <div className="w-2 h-2 rounded-full bg-blue-500 mt-2 flex-shrink-0" />
              {isEditing ? (
                <div className="flex-1 flex items-center gap-2">
                  <Textarea
                    value={point}
                    onChange={(e) => updateContentPoint(pointIndex, e.target.value)}
                    className="min-h-[60px] resize-none"
                    placeholder="内容要点"
                  />
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onRemovePoint(pointIndex)}
                    className="h-6 w-6 p-0 text-red-500 hover:text-red-700"
                  >
                    <Trash2 className="h-3 w-3" />
                  </Button>
                </div>
              ) : (
                <p className={`${isSingleView ? 'text-base' : 'text-sm'} text-gray-700 leading-relaxed`}>
                  {point}
                </p>
              )}
            </div>
          ))}
          
          {slide.content.length === 0 && (
            <p className="text-gray-400 text-sm italic">暂无内容</p>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default PPTPreviewModal;
