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
  Hash
} from 'lucide-react';
import { apiService } from '@/services/api';

interface QuizPreviewData {
  taskId: string;
  content: string;
  fileName: string;
  fileSize: number;
  topic: string;
  subject: string;
  courseLevel: string;
  difficulty?: string;
  questionCount?: number;
  generatedAt: string;
}

interface QuizPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  taskId: string;
  onDownload: () => void;
}

export const QuizPreviewModal: React.FC<QuizPreviewModalProps> = ({
  isOpen,
  onClose,
  taskId,
  onDownload
}) => {
  const [previewData, setPreviewData] = useState<QuizPreviewData | null>(null);
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

  // 解析习题内容为结构化格式
  const parseQuizContent = (content: string) => {
    const sections = content.split(/\n\s*\n/).filter(section => section.trim());
    return sections.map((section, index) => ({
      id: index,
      content: section.trim()
    }));
  };

  if (!previewData) {
    return null;
  }

  const quizSections = parseQuizContent(previewData.content);

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            习题预览 - {previewData.topic}
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
              <div className="flex items-center gap-4 flex-wrap">
                <Badge variant="outline" className="flex items-center gap-1">
                  <BookOpen className="h-3 w-3" />
                  {previewData.subject}
                </Badge>
                <Badge variant="outline" className="flex items-center gap-1">
                  <Target className="h-3 w-3" />
                  {previewData.courseLevel}
                </Badge>
                {previewData.difficulty && (
                  <Badge variant="outline">
                    难度: {previewData.difficulty}
                  </Badge>
                )}
                {previewData.questionCount && (
                  <Badge variant="outline" className="flex items-center gap-1">
                    <Hash className="h-3 w-3" />
                    {previewData.questionCount} 题
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

            {/* 习题内容 */}
            <ScrollArea className="flex-1">
              <div className="space-y-4 p-4">
                {quizSections.map((section, index) => (
                  <Card key={section.id} className="border-l-4 border-l-blue-500">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-lg flex items-center gap-2">
                        <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-sm font-medium">
                          {index + 1}
                        </span>
                        题目部分
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-gray-700 leading-relaxed">
                        {section.content}
                      </div>
                    </CardContent>
                  </Card>
                ))}
                
                {quizSections.length === 0 && (
                  <Card>
                    <CardContent className="text-center py-8">
                      <p className="text-gray-400">暂无习题内容</p>
                    </CardContent>
                  </Card>
                )}
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

export default QuizPreviewModal;
