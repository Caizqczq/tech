import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import { toast } from '@/hooks/use-toast';
import { apiService } from '@/services/api';
import {
  FileText,
  Image,
  Music,
  Video,
  Download,
  Share2,
  Eye,
  Calendar,
  User,
  Tag,
  FileSize,
  Clock,
  Loader2,
  AlertCircle,
  Volume2,
  VolumeX,
  Play,
  Pause,
  RotateCcw,
  ZoomIn,
  ZoomOut,
  Maximize,
  X
} from 'lucide-react';

interface ResourceDetailVO {
  id: string;
  title: string;
  description: string;
  subject: string;
  courseLevel: string;
  resourceType: string;
  originalName: string;
  fileSize: number;
  contentType: string;
  keywords: string[];
  downloadUrl: string;
  transcriptionText?: string;
  isVectorized: boolean;
  knowledgeBaseIds: string[];
  createdAt: string;
  updatedAt: string;
}

interface FilePreviewVO {
  previewType: string;
  previewUrl: string;
  originalUrl: string;
  previewable: boolean;
  previewImages?: string[];
  textContent?: string;
  metadata?: Record<string, any>;
  errorMessage?: string;
}

interface ResourcePreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  resourceId: string;
}

const ResourcePreviewModal: React.FC<ResourcePreviewModalProps> = ({
  isOpen,
  onClose,
  resourceId
}) => {
  const [resource, setResource] = useState<ResourceDetailVO | null>(null);
  const [preview, setPreview] = useState<FilePreviewVO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('preview');

  // 音频播放控制
  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);

  // 图片预览控制
  const [imageScale, setImageScale] = useState(1);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const fetchResourceData = async () => {
    if (!resourceId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      // 并行获取资源详情和预览信息
      const [resourceResponse, previewResponse] = await Promise.all([
        apiService.getResourceDetail(resourceId),
        apiService.getResourcePreview(resourceId)
      ]);
      
      setResource(resourceResponse);
      setPreview(previewResponse);
    } catch (err: any) {
      setError(err.message || '获取资源信息失败');
      toast({
        title: "加载失败",
        description: err.message || '无法加载资源信息',
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && resourceId) {
      fetchResourceData();
    } else if (!isOpen) {
      // 重置状态当模态框关闭时
      setResource(null);
      setPreview(null);
      setError(null);
      setActiveTab('preview');
    }
  }, [isOpen, resourceId]);

  const handleDownload = async () => {
    if (!resource) return;
    
    try {
      const response = await apiService.getResourceDownloadUrl(resource.id);
      window.open(response.downloadUrl, '_blank');
    } catch (err: any) {
      toast({
        title: "下载失败",
        description: err.message || '无法获取下载链接',
        variant: "destructive",
      });
    }
  };

  const handleShare = async () => {
    if (!resource) return;
    
    try {
      await navigator.clipboard.writeText(
        `${resource.title} - ${window.location.origin}/resource/${resource.id}`
      );
      toast({
        title: "链接已复制",
        description: "资源链接已复制到剪贴板",
      });
    } catch (err) {
      toast({
        title: "复制失败",
        description: "无法复制链接到剪贴板",
        variant: "destructive",
      });
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getResourceIcon = (contentType: string) => {
    if (contentType.startsWith('image/')) return <Image className="h-5 w-5" />;
    if (contentType.startsWith('audio/')) return <Music className="h-5 w-5" />;
    if (contentType.startsWith('video/')) return <Video className="h-5 w-5" />;
    return <FileText className="h-5 w-5" />;
  };

  const renderPreview = () => {
    if (!preview) return null;

    if (!preview.previewable) {
      return (
        <div className="flex flex-col items-center justify-center h-64 text-gray-500">
          <AlertCircle className="h-12 w-12 mb-4" />
          <p className="text-lg font-medium">无法预览此文件</p>
          <p className="text-sm text-center mt-2">
            {preview.errorMessage || '该文件类型不支持预览'}
          </p>
        </div>
      );
    }

    switch (preview.previewType) {
      case 'image':
        return (
          <div className="relative bg-gray-50 dark:bg-gray-900 rounded-lg p-4">
            <img
              src={preview.previewUrl}
              alt={resource?.title}
              className="w-full h-auto max-h-[600px] object-contain rounded-lg mx-auto"
              style={{ transform: `scale(${imageScale})` }}
            />
            <div className="absolute top-6 right-6 flex space-x-2">
              <Button
                size="sm"
                variant="secondary"
                onClick={() => setImageScale(Math.max(0.5, imageScale - 0.25))}
              >
                <ZoomOut className="h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="secondary"
                onClick={() => setImageScale(Math.min(3, imageScale + 0.25))}
              >
                <ZoomIn className="h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="secondary"
                onClick={() => setImageScale(1)}
              >
                <RotateCcw className="h-4 w-4" />
              </Button>
            </div>
          </div>
        );

      case 'audio':
        return (
          <div className="space-y-4">
            <audio
              src={preview.previewUrl}
              controls
              className="w-full"
              onTimeUpdate={(e) => setCurrentTime(e.currentTarget.currentTime)}
              onDurationChange={(e) => setDuration(e.currentTarget.duration)}
            />
            {preview.textContent && (
              <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-6">
                <h4 className="font-medium mb-4 flex items-center text-lg">
                  <FileText className="h-5 w-5 mr-2" />
                  转录文本
                </h4>
                <ScrollArea className="h-48">
                  <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap leading-relaxed">
                    {preview.textContent}
                  </p>
                </ScrollArea>
              </div>
            )}
          </div>
        );

      case 'pdf':
        return (
          <div className="w-full h-[600px] border rounded-lg bg-white">
            <iframe
              src={preview.previewUrl}
              className="w-full h-full rounded-lg"
              title="PDF预览"
            />
          </div>
        );

      case 'text':
        return (
          <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-6">
            <ScrollArea className="h-[500px]">
              <iframe
                src={preview.previewUrl}
                className="w-full h-full border-none bg-white rounded"
                title="文本预览"
              />
            </ScrollArea>
          </div>
        );

      default:
        return (
          <div className="flex flex-col items-center justify-center h-64 text-gray-500">
            <FileText className="h-12 w-12 mb-4" />
            <p className="text-lg font-medium">预览不可用</p>
            <p className="text-sm text-center mt-2">
              可以下载文件查看完整内容
            </p>
          </div>
        );
    }
  };

  const renderResourceInfo = () => {
    if (!resource) return null;
    
    // 确保所有需要的字段都有默认值
    const resourceData = {
      ...resource,
      keywords: resource.keywords || [],
      knowledgeBaseIds: resource.knowledgeBaseIds || [],
      description: resource.description || '',
      transcriptionText: resource.transcriptionText || ''
    };

    return (
      <div className="space-y-6">
        {/* 基本信息 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              {getResourceIcon(resource.contentType)}
              <span>基本信息</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  原始文件名
                </label>
                <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                  {resourceData.originalName}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  文件大小
                </label>
                <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                  {formatFileSize(resourceData.fileSize)}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  学科领域
                </label>
                <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                  {resourceData.subject}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  课程层次
                </label>
                <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                  {resourceData.courseLevel === 'undergraduate' ? '本科' : 
                   resourceData.courseLevel === 'graduate' ? '研究生' : '博士'}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  资源类型
                </label>
                <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                  {resourceData.resourceType}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  创建时间
                </label>
                <p className="mt-1 text-sm text-gray-900 dark:text-gray-100">
                  {formatDate(resourceData.createdAt)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 描述信息 */}
        {resourceData.description && (
          <Card>
            <CardHeader>
              <CardTitle>描述信息</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
                {resourceData.description}
              </p>
            </CardContent>
          </Card>
        )}

        {/* 关键词 */}
        {resourceData.keywords && resourceData.keywords.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>关键词</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {resourceData.keywords.map((keyword, index) => (
                  <Badge key={index} variant="secondary">
                    {keyword}
                  </Badge>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* 向量化状态 */}
        <Card>
          <CardHeader>
            <CardTitle>处理状态</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center space-x-2">
              <Badge variant={resourceData.isVectorized ? "default" : "secondary"}>
                {resourceData.isVectorized ? "已向量化" : "未向量化"}
              </Badge>
              {resourceData.knowledgeBaseIds && resourceData.knowledgeBaseIds.length > 0 && (
                <Badge variant="outline">
                  属于 {resourceData.knowledgeBaseIds.length} 个知识库
                </Badge>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-7xl max-h-[95vh] w-[90vw] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between">
            <span className="truncate">{resource?.title || '资源详情'}</span>
            <div className="flex items-center space-x-2">
              <Button
                size="sm"
                variant="outline"
                onClick={handleDownload}
                disabled={!resource}
              >
                <Download className="h-4 w-4 mr-2" />
                下载
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={handleShare}
                disabled={!resource}
              >
                <Share2 className="h-4 w-4 mr-2" />
                分享
              </Button>
            </div>
          </DialogTitle>
        </DialogHeader>

        {loading && (
          <div className="flex items-center justify-center h-64">
            <Loader2 className="h-8 w-8 animate-spin" />
            <span className="ml-2">加载中...</span>
          </div>
        )}

        {error && (
          <div className="flex items-center justify-center h-64 text-red-500">
            <AlertCircle className="h-8 w-8 mr-2" />
            <span>{error}</span>
          </div>
        )}

        {!loading && !error && resource && (
          <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full">
            <TabsList className="grid w-full grid-cols-2 mb-6">
              <TabsTrigger value="preview" className="text-base py-3">
                <Eye className="h-5 w-5 mr-2" />
                预览
              </TabsTrigger>
              <TabsTrigger value="info" className="text-base py-3">
                <FileText className="h-5 w-5 mr-2" />
                详情
              </TabsTrigger>
            </TabsList>

            <TabsContent value="preview" className="mt-0">
              <ScrollArea className="h-[75vh] pr-4">
                <div className="space-y-4">
                  {renderPreview()}
                </div>
              </ScrollArea>
            </TabsContent>

            <TabsContent value="info" className="mt-0">
              <ScrollArea className="h-[75vh] pr-4">
                <div className="space-y-6">
                  {renderResourceInfo()}
                </div>
              </ScrollArea>
            </TabsContent>
          </Tabs>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ResourcePreviewModal;