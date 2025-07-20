import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useToast } from '@/hooks/use-toast';
import { 
  Download, 
  FileText,
  Calendar,
  BookOpen,
  Target,
  Palette
} from 'lucide-react';
import { apiService } from '@/services/api';

interface ExplanationPreviewData {
  taskId: string;
  content: string;
  fileName: string;
  fileSize: number;
  topic: string;
  subject: string;
  courseLevel: string;
  style?: string;
  generatedAt: string;
}

interface ExplanationPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  taskId: string;
  onDownload: () => void;
}

export const ExplanationPreviewModal: React.FC<ExplanationPreviewModalProps> = ({
  isOpen,
  onClose,
  taskId,
  onDownload
}) => {
  const [previewData, setPreviewData] = useState<ExplanationPreviewData | null>(null);
  const [isLoading, setIsLoading] = useState(false);
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
      setPreviewData(data);
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

  // 解析讲解内容为结构化格式
  const parseExplanationContent = (content: string) => {
    // 按双换行分割段落
    const paragraphs = content.split(/\n\s*\n/).filter(p => p.trim());
    
    return paragraphs.map((paragraph, index) => {
      const trimmed = paragraph.trim();
      
      // 检测是否是标题（通常以数字、字母或特殊符号开头）
      const isTitle = /^[\d一二三四五六七八九十]+[、．.]|^[A-Za-z]+[、．.]|^#+\s|^【.*】/.test(trimmed);
      
      return {
        id: index,
        content: trimmed,
        isTitle,
        type: isTitle ? 'title' : 'content'
      };
    });
  };

  if (!previewData) {
    return null;
  }

  const explanationSections = parseExplanationContent(previewData.content);

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-5xl max-h-[90vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            讲解预览 - {previewData.topic}
          </DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="flex items-center justify-center h-96">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
            <span className="ml-2">加载中...</span>
          </div>
        ) : (
          <div className="flex flex-col h-full">
            {/* 信息栏 */}
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg mb-4">
              <div className="flex items-center gap-4 flex-wrap">
                <Badge variant="outline" className="flex items-center gap-1">
                  <BookOpen className="h-3 w-3" />
                  {previewData.subject}
                </Badge>
                <Badge variant="outline" className="flex items-center gap-1">
                  <Target className="h-3 w-3" />
                  {previewData.courseLevel}
                </Badge>
                {previewData.style && (
                  <Badge variant="outline" className="flex items-center gap-1">
                    <Palette className="h-3 w-3" />
                    {previewData.style}
                  </Badge>
                )}
                <Badge variant="outline" className="flex items-center gap-1">
                  <Calendar className="h-3 w-3" />
                  {new Date(previewData.generatedAt).toLocaleString()}
                </Badge>
                <Badge variant="outline">
                  {(previewData.fileSize / 1024).toFixed(1)} KB
                </Badge>
              </div>
            </div>

            {/* 讲解内容 */}
            <ScrollArea className="flex-1">
              <div className="space-y-4 p-4">
                <Card className="border-l-4 border-l-green-500">
                  <CardHeader className="pb-3">
                    <CardTitle className="text-xl text-green-800">
                      {previewData.topic} - 教学讲解
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {explanationSections.map((section) => (
                      <div key={section.id} className="space-y-2">
                        {section.isTitle ? (
                          <h3 className="text-lg font-semibold text-gray-800 border-b border-gray-200 pb-1">
                            {section.content}
                          </h3>
                        ) : (
                          <div className="text-gray-700 leading-relaxed whitespace-pre-wrap pl-4">
                            {section.content}
                          </div>
                        )}
                      </div>
                    ))}
                    
                    {explanationSections.length === 0 && (
                      <div className="text-center py-8">
                        <p className="text-gray-400">暂无讲解内容</p>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </ScrollArea>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            关闭
          </Button>
          <Button onClick={onDownload} className="flex items-center gap-1">
            <Download className="h-4 w-4" />
            下载Word文档
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default ExplanationPreviewModal;
