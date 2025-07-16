import React, { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Upload, FileText, Music, Files } from 'lucide-react';
import { apiService } from '@/services/api';
import { toast } from '@/hooks/use-toast';

interface UploadDialogProps {
  onUploadSuccess: () => void;
}

export const UploadDialog: React.FC<UploadDialogProps> = ({ onUploadSuccess }) => {
  const [open, setOpen] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadType, setUploadType] = useState('document');
  // toast is imported directly from hooks

  const [documentForm, setDocumentForm] = useState({
    file: null as File | null,
    subject: '',
    courseLevel: '', // 改为空字符串，让用户必须选择
    resourceType: 'lesson_plan',
    title: '',
    description: '',
    keywords: '',
    autoVectorize: true,
    autoExtractKeywords: true,
  });

  const [audioForm, setAudioForm] = useState({
    file: null as File | null,
    transcriptionMode: 'sync',
    needTranscription: true,
    subject: '',
    resourceType: 'lecture',
    description: '',
    speaker: '',
    language: 'zh',
    autoVectorize: true,
  });

  const [batchForm, setBatchForm] = useState({
    files: [] as File[],
    subject: '',
    courseLevel: '', // 改为空字符串，让用户必须选择
    autoVectorize: true,
  });

  const handleDocumentUpload = async () => {
    if (!documentForm.file || !documentForm.subject || !documentForm.courseLevel) {
      toast({
        title: "请填写必填项",
        description: "文件、学科和课程层次为必填项",
        variant: "destructive",
      });
      return;
    }

    setUploading(true);
    try {
      await apiService.uploadDocument(documentForm.file, {
        subject: documentForm.subject,
        courseLevel: documentForm.courseLevel,
        resourceType: documentForm.resourceType,
        title: documentForm.title,
        description: documentForm.description,
        keywords: documentForm.keywords,
        autoVectorize: documentForm.autoVectorize,
        autoExtractKeywords: documentForm.autoExtractKeywords,
      });

      toast({
        title: "上传成功",
        description: "文档已成功上传并处理",
      });

      setOpen(false);
      onUploadSuccess();
      resetForms();
    } catch (error: any) {
      toast({
        title: "上传失败",
        description: error.message || "文档上传失败",
        variant: "destructive",
      });
    } finally {
      setUploading(false);
    }
  };

  const handleAudioUpload = async () => {
    if (!audioForm.file) {
      toast({
        title: "请选择文件",
        description: "请选择要上传的音频文件",
        variant: "destructive",
      });
      return;
    }

    setUploading(true);
    try {
      await apiService.uploadAudio(audioForm.file, audioForm);

      toast({
        title: "上传成功",
        description: "音频已成功上传并处理",
      });

      setOpen(false);
      onUploadSuccess();
      resetForms();
    } catch (error: any) {
      toast({
        title: "上传失败",
        description: error.message || "音频上传失败",
        variant: "destructive",
      });
    } finally {
      setUploading(false);
    }
  };

  const handleBatchUpload = async () => {
    if (batchForm.files.length === 0 || !batchForm.subject || !batchForm.courseLevel) {
      toast({
        title: "请填写必填项",
        description: "文件、学科和课程层次为必填项",
        variant: "destructive",
      });
      return;
    }

    setUploading(true);
    try {
      const result = await apiService.uploadBatch(batchForm.files, {
        subject: batchForm.subject,
        courseLevel: batchForm.courseLevel,
        autoVectorize: batchForm.autoVectorize,
      });

      toast({
        title: "批量上传完成",
        description: `成功上传 ${result.successCount} 个文件，失败 ${result.failedCount} 个`,
      });

      setOpen(false);
      onUploadSuccess();
      resetForms();
    } catch (error: any) {
      toast({
        title: "上传失败",
        description: error.message || "批量上传失败",
        variant: "destructive",
      });
    } finally {
      setUploading(false);
    }
  };

  const resetForms = () => {
    setDocumentForm({
      file: null,
      subject: '',
      courseLevel: '',
      resourceType: 'lesson_plan',
      title: '',
      description: '',
      keywords: '',
      autoVectorize: true,
      autoExtractKeywords: true,
    });
    setAudioForm({
      file: null,
      transcriptionMode: 'sync',
      needTranscription: true,
      subject: '',
      resourceType: 'lecture',
      description: '',
      speaker: '',
      language: 'zh',
      autoVectorize: true,
    });
    setBatchForm({
      files: [],
      subject: '',
      courseLevel: '',
      autoVectorize: true,
    });
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl">
          <Upload className="h-4 w-4 mr-2" />
          上传资源
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>上传教学资源</DialogTitle>
        </DialogHeader>

        <Tabs value={uploadType} onValueChange={setUploadType}>
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="document">
              <FileText className="h-4 w-4 mr-2" />
              文档上传
            </TabsTrigger>
            <TabsTrigger value="audio">
              <Music className="h-4 w-4 mr-2" />
              音频上传
            </TabsTrigger>
            <TabsTrigger value="batch">
              <Files className="h-4 w-4 mr-2" />
              批量上传
            </TabsTrigger>
          </TabsList>

          <TabsContent value="document" className="space-y-4">
            <div>
              <Label htmlFor="doc-file">选择文档 *</Label>
              <Input
                id="doc-file"
                type="file"
                accept=".pdf,.doc,.docx,.ppt,.pptx,.txt,.md"
                onChange={(e) => setDocumentForm(prev => ({ ...prev, file: e.target.files?.[0] || null }))}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="doc-subject">学科 *</Label>
                <Input
                  id="doc-subject"
                  value={documentForm.subject}
                  onChange={(e) => setDocumentForm(prev => ({ ...prev, subject: e.target.value }))}
                  placeholder="如：数学、物理"
                />
              </div>
              <div>
                <Label htmlFor="doc-level">课程层次 *</Label>
                <Select value={documentForm.courseLevel} onValueChange={(value) => setDocumentForm(prev => ({ ...prev, courseLevel: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择课程层次" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="undergraduate">本科</SelectItem>
                    <SelectItem value="graduate">研究生</SelectItem>
                    <SelectItem value="doctoral">博士</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div>
              <Label htmlFor="doc-type">资源类型</Label>
              <Select value={documentForm.resourceType} onValueChange={(value) => setDocumentForm(prev => ({ ...prev, resourceType: value }))}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="lesson_plan">教案课件</SelectItem>
                  <SelectItem value="syllabus">教学大纲</SelectItem>
                  <SelectItem value="paper">学术论文</SelectItem>
                  <SelectItem value="textbook">教材资料</SelectItem>
                  <SelectItem value="exercise">练习题库</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="doc-title">标题</Label>
              <Input
                id="doc-title"
                value={documentForm.title}
                onChange={(e) => setDocumentForm(prev => ({ ...prev, title: e.target.value }))}
                placeholder="资源标题"
              />
            </div>

            <div>
              <Label htmlFor="doc-desc">描述</Label>
              <Textarea
                id="doc-desc"
                value={documentForm.description}
                onChange={(e) => setDocumentForm(prev => ({ ...prev, description: e.target.value }))}
                placeholder="资源描述"
              />
            </div>

            <div>
              <Label htmlFor="doc-keywords">关键词</Label>
              <Input
                id="doc-keywords"
                value={documentForm.keywords}
                onChange={(e) => setDocumentForm(prev => ({ ...prev, keywords: e.target.value }))}
                placeholder="用逗号分隔多个关键词"
              />
            </div>

            <Button onClick={handleDocumentUpload} disabled={uploading} className="w-full">
              {uploading ? '上传中...' : '上传文档'}
            </Button>
          </TabsContent>

          <TabsContent value="audio" className="space-y-4">
            <div>
              <Label htmlFor="audio-file">选择音频 *</Label>
              <Input
                id="audio-file"
                type="file"
                accept=".mp3,.wav,.m4a,.flac"
                onChange={(e) => setAudioForm(prev => ({ ...prev, file: e.target.files?.[0] || null }))}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="audio-subject">学科</Label>
                <Input
                  id="audio-subject"
                  value={audioForm.subject}
                  onChange={(e) => setAudioForm(prev => ({ ...prev, subject: e.target.value }))}
                  placeholder="如：数学、物理"
                />
              </div>
              <div>
                <Label htmlFor="audio-speaker">讲者</Label>
                <Input
                  id="audio-speaker"
                  value={audioForm.speaker}
                  onChange={(e) => setAudioForm(prev => ({ ...prev, speaker: e.target.value }))}
                  placeholder="讲者姓名"
                />
              </div>
            </div>

            <div>
              <Label htmlFor="audio-desc">描述</Label>
              <Textarea
                id="audio-desc"
                value={audioForm.description}
                onChange={(e) => setAudioForm(prev => ({ ...prev, description: e.target.value }))}
                placeholder="音频内容描述"
              />
            </div>

            <div>
              <Label htmlFor="audio-type">资源类型</Label>
              <Select value={audioForm.resourceType} onValueChange={(value) => setAudioForm(prev => ({ ...prev, resourceType: value }))}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="lecture">讲座录音</SelectItem>
                  <SelectItem value="seminar">研讨会</SelectItem>
                  <SelectItem value="discussion">讨论记录</SelectItem>
                  <SelectItem value="interview">访谈录音</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <Button onClick={handleAudioUpload} disabled={uploading} className="w-full">
              {uploading ? '上传中...' : '上传音频'}
            </Button>
          </TabsContent>

          <TabsContent value="batch" className="space-y-4">
            <div>
              <Label htmlFor="batch-files">选择多个文件 *</Label>
              <Input
                id="batch-files"
                type="file"
                multiple
                accept=".pdf,.doc,.docx,.ppt,.pptx,.txt,.md,.mp3,.wav,.m4a,.flac"
                onChange={(e) => setBatchForm(prev => ({ ...prev, files: Array.from(e.target.files || []) }))}
              />
              {batchForm.files.length > 0 && (
                <p className="text-sm text-gray-600 mt-2">
                  已选择 {batchForm.files.length} 个文件
                </p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="batch-subject">学科 *</Label>
                <Input
                  id="batch-subject"
                  value={batchForm.subject}
                  onChange={(e) => setBatchForm(prev => ({ ...prev, subject: e.target.value }))}
                  placeholder="如：数学、物理"
                />
              </div>
              <div>
                <Label htmlFor="batch-level">课程层次 *</Label>
                <Select value={batchForm.courseLevel} onValueChange={(value) => setBatchForm(prev => ({ ...prev, courseLevel: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择课程层次" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="undergraduate">本科</SelectItem>
                    <SelectItem value="graduate">研究生</SelectItem>
                    <SelectItem value="doctoral">博士</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <Button onClick={handleBatchUpload} disabled={uploading} className="w-full">
              {uploading ? '上传中...' : '批量上传'}
            </Button>
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
};


