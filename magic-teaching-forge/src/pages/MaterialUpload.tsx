import React, { useState, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Upload, FileText, Image, Mic, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';
// 需要先安装 react-dropzone 依赖
// npm install react-dropzone @types/react-dropzone
import { useDropzone } from 'react-dropzone';
import { apiService } from '@/services/api';
import { toast } from '@/hooks/use-toast';
import DashboardHeader from '@/components/dashboard/DashboardHeader';

interface UploadedFile {
  id: string;
  file: File;
  type: 'document' | 'image' | 'audio';
  status: 'uploading' | 'processing' | 'completed' | 'error';
  progress: number;
  result?: any;
  error?: string;
}

const MaterialUpload = () => {
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [metadata, setMetadata] = useState({
    subject: '',
    grade: '',
    tags: [] as string[],
  });
  const [tagInput, setTagInput] = useState('');

  const onDrop = useCallback(async (acceptedFiles: File[], fileType: 'document' | 'image' | 'audio') => {
    const newFiles: UploadedFile[] = acceptedFiles.map(file => ({
      id: Math.random().toString(36).substr(2, 9),
      file,
      type: fileType,
      status: 'uploading',
      progress: 0,
    }));

    setUploadedFiles(prev => [...prev, ...newFiles]);

    // 处理每个文件
    for (const uploadFile of newFiles) {
      try {
        // 模拟上传进度
        const progressInterval = setInterval(() => {
          setUploadedFiles(prev => prev.map(f => 
            f.id === uploadFile.id 
              ? { ...f, progress: Math.min(f.progress + 10, 90) }
              : f
          ));
        }, 200);

        let result;
        if (fileType === 'audio') {
          // 语音转文字
          result = await apiService.transcribeAudio(uploadFile.file);
        } else {
          // 普通文件上传
          result = await apiService.uploadFile(uploadFile.file, fileType);
        }

        clearInterval(progressInterval);

        if (result.success) {
          setUploadedFiles(prev => prev.map(f => 
            f.id === uploadFile.id 
              ? { ...f, status: 'completed', progress: 100, result: result.data }
              : f
          ));

          // 如果有元数据，上传到知识库
          if (metadata.subject || metadata.grade || metadata.tags.length > 0) {
            try {
              await apiService.uploadToKnowledgeBase(uploadFile.file, metadata);
              toast({
                title: "上传成功",
                description: `${uploadFile.file.name} 已添加到知识库`,
              });
            } catch (error) {
              console.error('知识库上传失败:', error);
            }
          }
        } else {
          throw new Error(result.message || '上传失败');
        }
      } catch (error: any) {
        setUploadedFiles(prev => prev.map(f => 
          f.id === uploadFile.id 
            ? { ...f, status: 'error', error: error.message }
            : f
        ));
        toast({
          title: "上传失败",
          description: error.message,
          variant: "destructive",
        });
      }
    }
  }, [metadata]);

  const documentDropzone = useDropzone({
    onDrop: (files) => onDrop(files, 'document'),
    accept: {
      'application/pdf': ['.pdf'],
      'application/msword': ['.doc'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
    },
    multiple: true,
  });

  const imageDropzone = useDropzone({
    onDrop: (files) => onDrop(files, 'image'),
    accept: {
      'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.bmp', '.webp'],
    },
    multiple: true,
  });

  const audioDropzone = useDropzone({
    onDrop: (files) => onDrop(files, 'audio'),
    accept: {
      'audio/*': ['.mp3', '.wav', '.m4a', '.aac', '.ogg'],
    },
    multiple: true,
  });

  const addTag = () => {
    if (tagInput.trim() && !metadata.tags.includes(tagInput.trim())) {
      setMetadata(prev => ({
        ...prev,
        tags: [...prev.tags, tagInput.trim()]
      }));
      setTagInput('');
    }
  };

  const removeTag = (tagToRemove: string) => {
    setMetadata(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'uploading':
      case 'processing':
        return <Loader2 className="h-4 w-4 animate-spin text-blue-500" />;
      case 'completed':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'error':
        return <AlertCircle className="h-4 w-4 text-red-500" />;
      default:
        return null;
    }
  };

  const DropzoneCard = ({ dropzone, icon: Icon, title, description, acceptedTypes }: any) => (
    <Card 
      {...dropzone.getRootProps()} 
      className={`cursor-pointer transition-all duration-200 hover:shadow-lg ${
        dropzone.isDragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
      }`}
    >
      <input {...dropzone.getInputProps()} />
      <CardContent className="flex flex-col items-center justify-center py-12">
        <Icon className={`h-12 w-12 mb-4 ${
          dropzone.isDragActive ? 'text-blue-500' : 'text-gray-400'
        }`} />
        <h3 className="text-lg font-semibold mb-2">{title}</h3>
        <p className="text-gray-600 text-center mb-4">{description}</p>
        <div className="flex flex-wrap gap-2 justify-center">
          {acceptedTypes.map((type: string, index: number) => (
            <Badge key={index} variant="secondary">{type}</Badge>
          ))}
        </div>
        <Button className="mt-4" variant="outline">
          <Upload className="h-4 w-4 mr-2" />
          选择文件或拖拽到此处
        </Button>
      </CardContent>
    </Card>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      <DashboardHeader />
      
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">多模态素材上传</h1>
          <p className="text-gray-600">上传文档、图片、音频等教学素材，自动处理并构建知识库</p>
        </div>

        {/* 元数据设置 */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle>素材信息</CardTitle>
            <CardDescription>设置素材的学科、年级和标签信息，便于后续管理和检索</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="subject">学科</Label>
                <Select value={metadata.subject} onValueChange={(value) => setMetadata(prev => ({ ...prev, subject: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择学科" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="数学">数学</SelectItem>
                    <SelectItem value="物理">物理</SelectItem>
                    <SelectItem value="化学">化学</SelectItem>
                    <SelectItem value="生物">生物</SelectItem>
                    <SelectItem value="语文">语文</SelectItem>
                    <SelectItem value="英语">英语</SelectItem>
                    <SelectItem value="历史">历史</SelectItem>
                    <SelectItem value="地理">地理</SelectItem>
                    <SelectItem value="政治">政治</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="grade">年级</Label>
                <Select value={metadata.grade} onValueChange={(value) => setMetadata(prev => ({ ...prev, grade: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择年级" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="大一">大一</SelectItem>
                    <SelectItem value="大二">大二</SelectItem>
                    <SelectItem value="大三">大三</SelectItem>
                    <SelectItem value="大四">大四</SelectItem>
                    <SelectItem value="研一">研一</SelectItem>
                    <SelectItem value="研二">研二</SelectItem>
                    <SelectItem value="研三">研三</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            
            <div>
              <Label htmlFor="tags">标签</Label>
              <div className="flex space-x-2 mb-2">
                <Input
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  placeholder="输入标签"
                  onKeyPress={(e) => e.key === 'Enter' && addTag()}
                />
                <Button onClick={addTag} variant="outline">添加</Button>
              </div>
              <div className="flex flex-wrap gap-2">
                {metadata.tags.map((tag, index) => (
                  <Badge key={index} variant="secondary" className="cursor-pointer" onClick={() => removeTag(tag)}>
                    {tag} ×
                  </Badge>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 文件上传区域 */}
        <Tabs defaultValue="document" className="mb-8">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="document">文档素材</TabsTrigger>
            <TabsTrigger value="image">图片素材</TabsTrigger>
            <TabsTrigger value="audio">语音素材</TabsTrigger>
          </TabsList>
          
          <TabsContent value="document" className="mt-6">
            <DropzoneCard
              dropzone={documentDropzone}
              icon={FileText}
              title="文档上传"
              description="支持Word、PDF等文档格式，自动提取文本内容"
              acceptedTypes={['PDF', 'DOC', 'DOCX', 'TXT']}
            />
          </TabsContent>
          
          <TabsContent value="image" className="mt-6">
            <DropzoneCard
              dropzone={imageDropzone}
              icon={Image}
              title="图片上传"
              description="支持各种图片格式，可进行OCR文字识别和AI分析"
              acceptedTypes={['PNG', 'JPG', 'JPEG', 'GIF', 'BMP', 'WEBP']}
            />
          </TabsContent>
          
          <TabsContent value="audio" className="mt-6">
            <DropzoneCard
              dropzone={audioDropzone}
              icon={Mic}
              title="语音上传"
              description="支持音频文件，自动转换为文字内容"
              acceptedTypes={['MP3', 'WAV', 'M4A', 'AAC', 'OGG']}
            />
          </TabsContent>
        </Tabs>

        {/* 上传文件列表 */}
        {uploadedFiles.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>上传进度</CardTitle>
              <CardDescription>查看文件上传和处理状态</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {uploadedFiles.map((file) => (
                  <div key={file.id} className="flex items-center space-x-4 p-4 border rounded-lg">
                    <div className="flex-shrink-0">
                      {getStatusIcon(file.status)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {file.file.name}
                      </p>
                      <p className="text-sm text-gray-500">
                        {file.type === 'document' && '文档'}
                        {file.type === 'image' && '图片'}
                        {file.type === 'audio' && '音频'}
                        {' • '}
                        {(file.file.size / 1024 / 1024).toFixed(2)} MB
                      </p>
                      {file.status === 'uploading' || file.status === 'processing' ? (
                        <Progress value={file.progress} className="mt-2" />
                      ) : null}
                      {file.error && (
                        <p className="text-sm text-red-500 mt-1">{file.error}</p>
                      )}
                      {file.result && file.type === 'audio' && file.result.text && (
                        <div className="mt-2 p-2 bg-gray-50 rounded text-sm">
                          <strong>转录结果：</strong>{file.result.text}
                        </div>
                      )}
                    </div>
                    <div className="flex-shrink-0">
                      <Badge variant={file.status === 'completed' ? 'default' : file.status === 'error' ? 'destructive' : 'secondary'}>
                        {file.status === 'uploading' && '上传中'}
                        {file.status === 'processing' && '处理中'}
                        {file.status === 'completed' && '完成'}
                        {file.status === 'error' && '失败'}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  );
};

export default MaterialUpload;