import React, { useState, useEffect, useRef } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { 
  Send, 
  Bot, 
  User, 
  Mic, 
  MicOff, 
  Image as ImageIcon, 
  FileText, 
  Lightbulb, 
  BookOpen, 
  MessageSquare, 
  Clock, 
  Star,
  Copy,
  ThumbsUp,
  ThumbsDown,
  RefreshCw,
  Settings,
  Zap
} from 'lucide-react';
import { apiService } from '@/services/api';
import { toast } from '@/hooks/use-toast';
import DashboardHeader from '@/components/dashboard/DashboardHeader';
import { ConversationMessage, ConversationDetail, ConversationItem } from '@/types/api';

interface QuickQuestion {
  id: string;
  question: string;
  category: string;
  icon: React.ReactNode;
}

interface ChatSession {
  conversationId: string;
  title: string;
  messages: ConversationMessage[];
  createdAt: string;
  updatedAt: string;
}

const SmartChat = () => {
  const [currentSession, setCurrentSession] = useState<ChatSession | null>(null);
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  const [chatMode, setChatMode] = useState<'simple' | 'teaching'>('simple');
  const [streamingMessage, setStreamingMessage] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const quickQuestions: QuickQuestion[] = [
    {
      id: '1',
      question: '如何提高学生的学习兴趣？',
      category: '教学方法',
      icon: <Lightbulb className="h-4 w-4" />
    },
    {
      id: '2',
      question: '线性代数的核心概念有哪些？',
      category: '知识点',
      icon: <BookOpen className="h-4 w-4" />
    },
    {
      id: '3',
      question: '如何设计有效的课堂互动？',
      category: '教学设计',
      icon: <MessageSquare className="h-4 w-4" />
    },
    {
      id: '4',
      question: '微积分的实际应用场景',
      category: '应用案例',
      icon: <Zap className="h-4 w-4" />
    }
  ];

  useEffect(() => {
    loadChatSessions();
    createNewSession();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [currentSession?.messages, streamingMessage]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadChatSessions = async () => {
    try {
      const response = await apiService.getConversations(1, 20);
      const sessionData: ChatSession[] = response.conversations.map(conv => ({
        conversationId: conv.conversationId,
        title: conv.title,
        messages: [], // 消息将在选择会话时加载
        createdAt: conv.createdAt,
        updatedAt: conv.updatedAt
      }));
      setSessions(sessionData);
    } catch (error) {
      console.error('加载对话历史失败:', error);
      // 现有的模拟数据逻辑...
    }
  };

  // 添加加载对话详情的函数
  const loadConversationDetail = async (conversationId: string) => {
    try {
      const response = await apiService.getConversationDetail(conversationId);
      return response.messages || [];
    } catch (error) {
      console.error('加载对话详情失败:', error);
      return [];
    }
  };

  const createNewSession = () => {
    const newSession: ChatSession = {
      conversationId: Date.now().toString(),
      title: '新对话',
      messages: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    setCurrentSession(newSession);
  };

  const sendMessage = async () => {
    if (!message.trim() && !selectedImage) return;
    if (isLoading) return;

    const userMessage: ConversationMessage = {
      messageId: Date.now().toString(),
      role: 'user',
      content: message,
      timestamp: new Date().toISOString()
    };

    // 更新当前会话
    const updatedSession = {
      ...currentSession!,
      messages: [...(currentSession?.messages || []), userMessage],
      title: currentSession?.title === '新对话' ? message.slice(0, 20) + '...' : currentSession?.title || '新对话'
    };
    setCurrentSession(updatedSession);

    const currentMessage = message;
    setMessage('');
    setSelectedImage(null);
    setIsLoading(true);
    setStreamingMessage('');

    try {
      if (chatMode === 'simple') {
        // 简单聊天模式
        if (selectedImage) {
          // 图片分析
          const result = await apiService.analyzeImageByUpload(selectedImage, currentMessage || '分析这张图片');
          const assistantMessage: ConversationMessage = {
            messageId: (Date.now() + 1).toString(),
            role: 'assistant',
            content: result.content,
            timestamp: new Date().toISOString()
          };
          
          const finalSession = {
            ...updatedSession,
            messages: [...updatedSession.messages, assistantMessage],
            conversationId: result.conversationId || updatedSession.conversationId // 更新conversationId
          };
          setCurrentSession(finalSession);
        } else {
          // 简单聊天
          const result = await apiService.simpleChat(currentMessage, currentSession?.conversationId);
          const assistantMessage: ConversationMessage = {
            messageId: (Date.now() + 1).toString(),
            role: 'assistant',
            content: result.content, // 使用正确的字段名
            timestamp: new Date().toISOString()
          };
          
          const finalSession = {
            ...updatedSession,
            messages: [...updatedSession.messages, assistantMessage],
            conversationId: result.conversationId || updatedSession.conversationId // 更新conversationId
          };
          setCurrentSession(finalSession);
        }
      } else {
        // 教学助手模式
        const response = await apiService.chatWithAssistant({
          message: currentMessage,
          conversationId: currentSession?.conversationId,
          mode: 'teaching',
          context: {
            subject: '数学',
            grade: '大学',
            topic: '教学咨询'
          }
        });
        
        const assistantMessage: ConversationMessage = {
          messageId: (Date.now() + 1).toString(),
          role: 'assistant',
          content: response.response, // 修正字段名
          timestamp: new Date().toISOString()
        };
        
        const finalSession = {
          ...updatedSession,
          messages: [...updatedSession.messages, assistantMessage],
          conversationId: response.conversationId || updatedSession.conversationId // 更新conversationId
        };
        setCurrentSession(finalSession);
      }
    } catch (error) {
      toast({
        title: "发送失败",
        description: "无法发送消息，请重试",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickQuestion = (question: string) => {
    setMessage(question);
  };

  const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedImage(file);
    }
  };

  const startRecording = async () => {
    try {
      setIsRecording(true);
      // 实际应该实现语音录制功能
      toast({
        title: "录音开始",
        description: "请开始说话...",
      });
    } catch (error) {
      toast({
        title: "录音失败",
        description: "无法启动录音功能",
        variant: "destructive",
      });
    }
  };

  const stopRecording = async () => {
    try {
      setIsRecording(false);
      // 实际应该调用语音转文字API
      const mockTranscript = "这是模拟的语音转文字结果";
      setMessage(mockTranscript);
      toast({
        title: "录音完成",
        description: "语音已转换为文字",
      });
    } catch (error) {
      toast({
        title: "转换失败",
        description: "语音转文字失败",
        variant: "destructive",
      });
    }
  };

  const copyMessage = (content: string) => {
    navigator.clipboard.writeText(content);
    toast({
      title: "已复制",
      description: "消息内容已复制到剪贴板",
    });
  };

  const MessageBubble = ({ message }: { message: ConversationMessage }) => (
    <div className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'} mb-4`}>
      <div className={`flex items-start space-x-2 max-w-[80%] ${
        message.role === 'user' ? 'flex-row-reverse space-x-reverse' : ''
      }`}>
        <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${
          message.role === 'user' 
            ? 'bg-blue-600 text-white' 
            : 'bg-gray-200 text-gray-600'
        }`}>
          {message.role === 'user' ? <User className="h-4 w-4" /> : <Bot className="h-4 w-4" />}
        </div>
        
        <div className={`rounded-lg px-4 py-2 ${
          message.role === 'user'
            ? 'bg-blue-600 text-white'
            : 'bg-gray-100 text-gray-900'
        }`}>
          <div className="whitespace-pre-wrap">{message.content}</div>
          
          {message.role === 'assistant' && (
            <div className="flex items-center space-x-2 mt-2 pt-2 border-t border-gray-200">
              <Button 
                size="sm" 
                variant="ghost" 
                onClick={() => copyMessage(message.content)}
                className="h-6 px-2 text-xs"
              >
                <Copy className="h-3 w-3 mr-1" />
                复制
              </Button>
              <Button size="sm" variant="ghost" className="h-6 px-2 text-xs">
                <ThumbsUp className="h-3 w-3" />
              </Button>
              <Button size="sm" variant="ghost" className="h-6 px-2 text-xs">
                <ThumbsDown className="h-3 w-3" />
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  const handleSessionSelect = async (session: ChatSession) => {
    console.log('选择会话:', session);
    
    try {
      // 总是从服务器重新加载消息
      console.log('开始加载对话详情:', session.conversationId);
      const response = await apiService.getConversationDetail(session.conversationId);
      console.log('API响应:', response);
      
      const messages = response.messages || [];
      console.log('解析的消息:', messages);
      
      const updatedSession = { 
        ...session, 
        messages: messages.map(msg => ({
          messageId: Date.now().toString() + Math.random(),
          role: msg.role,
          content: msg.content,
          timestamp: msg.timestamp
        }))
      };
      
      console.log('更新后的会话:', updatedSession);
      setCurrentSession(updatedSession);
      
      // 更新sessions中的数据
      setSessions(prev => prev.map(s => 
        s.conversationId === session.conversationId ? updatedSession : s
      ));
      
    } catch (error) {
      console.error('加载对话详情失败:', error);
      // 如果加载失败，至少设置当前会话
      setCurrentSession(session);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      <DashboardHeader />
      
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">智能对话助手</h1>
          <p className="text-gray-600">与AI对话，快速咨询教学知识点和获取专业建议</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* 侧边栏 - 对话历史 */}
          <div className="lg:col-span-1">
            <Card className="mb-6">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">对话历史</CardTitle>
                  <Button size="sm" onClick={createNewSession}>
                    新对话
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-64">
                  <div className="space-y-2">
                    {sessions.map((session) => (
                      <div 
                        key={session.conversationId}
                        className={`p-3 rounded-lg cursor-pointer transition-colors ${
                          currentSession?.conversationId === session.conversationId 
                            ? 'bg-blue-100 border-blue-200' 
                            : 'hover:bg-gray-50'
                        }`}
                        onClick={() => handleSessionSelect(session)}
                      >
                        <h4 className="font-medium text-sm truncate">{session.title}</h4>
                        <div className="flex items-center space-x-1 mt-1 text-xs text-gray-500">
                          <Clock className="h-3 w-3" />
                          <span>{new Date(session.updatedAt).toLocaleDateString()}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>
            
            {/* 快速问题 */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">快速问题</CardTitle>
                <CardDescription>点击快速提问</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {quickQuestions.map((q) => (
                    <Button
                      key={q.id}
                      variant="outline"
                      size="sm"
                      className="w-full justify-start text-left h-auto p-3"
                      onClick={() => handleQuickQuestion(q.question)}
                    >
                      <div className="flex items-start space-x-2">
                        <div className="flex-shrink-0 mt-0.5">{q.icon}</div>
                        <div>
                          <div className="text-xs text-gray-500 mb-1">{q.category}</div>
                          <div className="text-sm">{q.question}</div>
                        </div>
                      </div>
                    </Button>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
          
          {/* 主对话区 */}
          <div className="lg:col-span-3">
            <Card className="h-[600px] flex flex-col">
              <CardHeader className="flex-shrink-0">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center space-x-2">
                      <Bot className="h-5 w-5" />
                      <span>{currentSession?.title || '新对话'}</span>
                    </CardTitle>
                    <CardDescription>选择对话模式并开始交流</CardDescription>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <Select value={chatMode} onValueChange={(value: 'simple' | 'teaching') => setChatMode(value)}>
                      <SelectTrigger className="w-32">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="simple">简单聊天</SelectItem>
                        <SelectItem value="teaching">教学助手</SelectItem>
                      </SelectContent>
                    </Select>
                    
                    <Button variant="outline" size="sm">
                      <Settings className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              
              {/* 消息区域 */}
              <CardContent className="flex-1 flex flex-col p-0">
                <ScrollArea className="flex-1 p-6">
                  {currentSession?.messages.length === 0 ? (
                    <div className="text-center py-12">
                      <Bot className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                      <p className="text-gray-600 mb-2">开始新的对话</p>
                      <p className="text-sm text-gray-500">输入您的问题或选择快速问题开始</p>
                    </div>
                  ) : (
                    <div>
                      {currentSession?.messages.map((msg) => (
                        <MessageBubble key={msg.messageId} message={msg} />
                      ))}
                      
                      {/* 流式消息 */}
                      {streamingMessage && (
                        <div className="flex justify-start mb-4">
                          <div className="flex items-start space-x-2 max-w-[80%]">
                            <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center">
                              <Bot className="h-4 w-4" />
                            </div>
                            <div className="bg-gray-100 text-gray-900 rounded-lg px-4 py-2">
                              <div className="whitespace-pre-wrap">{streamingMessage}</div>
                              <div className="animate-pulse inline-block w-2 h-4 bg-gray-400 ml-1"></div>
                            </div>
                          </div>
                        </div>
                      )}
                      
                      {isLoading && !streamingMessage && (
                        <div className="flex justify-start mb-4">
                          <div className="flex items-start space-x-2">
                            <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center">
                              <Bot className="h-4 w-4" />
                            </div>
                            <div className="bg-gray-100 rounded-lg px-4 py-2">
                              <div className="flex space-x-1">
                                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0.1s'}}></div>
                                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0.2s'}}></div>
                              </div>
                            </div>
                          </div>
                        </div>
                      )}
                      
                      <div ref={messagesEndRef} />
                    </div>
                  )}
                </ScrollArea>
                
                {/* 输入区域 */}
                <div className="border-t p-4">
                  {selectedImage && (
                    <div className="mb-3 p-2 bg-gray-50 rounded-lg">
                      <div className="flex items-center space-x-2">
                        <ImageIcon className="h-4 w-4 text-gray-500" />
                        <span className="text-sm text-gray-600">{selectedImage.name}</span>
                        <Button 
                          size="sm" 
                          variant="ghost" 
                          onClick={() => setSelectedImage(null)}
                          className="ml-auto"
                        >
                          ×
                        </Button>
                      </div>
                    </div>
                  )}
                  
                  <div className="flex items-end space-x-2">
                    <div className="flex-1">
                      <Textarea
                        placeholder={chatMode === 'teaching' ? '请输入您的教学问题...' : '输入消息...'}
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        onKeyPress={(e) => {
                          if (e.key === 'Enter' && !e.shiftKey) {
                            e.preventDefault();
                            sendMessage();
                          }
                        }}
                        className="min-h-[60px] resize-none"
                        disabled={isLoading}
                      />
                    </div>
                    
                    <div className="flex flex-col space-y-2">
                      <input
                        aria-label="上传图片"
                        title="选择要上传的图片"
                        placeholder="选择图片文件"
                        type="file"
                        ref={fileInputRef}
                        onChange={handleImageUpload}
                        accept="image/*"
                        className="hidden"
                      />
                      
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => fileInputRef.current?.click()}
                        disabled={isLoading}
                      >
                        <ImageIcon className="h-4 w-4" />
                      </Button>
                      
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={isRecording ? stopRecording : startRecording}
                        disabled={isLoading}
                        className={isRecording ? 'bg-red-100 text-red-600' : ''}
                      >
                        {isRecording ? <MicOff className="h-4 w-4" /> : <Mic className="h-4 w-4" />}
                      </Button>
                      
                      <Button
                        onClick={sendMessage}
                        disabled={(!message.trim() && !selectedImage) || isLoading}
                        size="sm"
                      >
                        <Send className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                  
                  <div className="flex items-center justify-between mt-2 text-xs text-gray-500">
                    <div className="flex items-center space-x-2">
                      <Badge variant="secondary" className="text-xs">
                        {chatMode === 'teaching' ? '教学助手模式' : '简单聊天模式'}
                      </Badge>
                      {selectedImage && (
                        <Badge variant="outline" className="text-xs">
                          图片分析
                        </Badge>
                      )}
                    </div>
                    <span>按 Enter 发送，Shift+Enter 换行</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
};

export default SmartChat;



