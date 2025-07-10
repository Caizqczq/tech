
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Heart, MessageCircle, Share2, Plus, ArrowLeft, Eye, BookOpen } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';

const Community = () => {
  const navigate = useNavigate();
  const [newPostTitle, setNewPostTitle] = useState('');
  const [newPostContent, setNewPostContent] = useState('');

  const posts = [
    {
      id: 1,
      title: '如何提高学生在数学课堂上的参与度？',
      content: '最近在教学中发现学生对数学的兴趣不高，想请教各位老师有什么好的方法...',
      author: '李老师',
      subject: '数学',
      likes: 24,
      comments: 8,
      views: 156,
      createdAt: '2024-06-20',
      tags: ['教学方法', '学生参与', '数学']
    },
    {
      id: 2,
      title: '分享一个语文课堂的创新教学案例',
      content: '在教授《诗经》时，我尝试了角色扮演的方法，效果很好。学生们通过扮演不同的角色...',
      author: '王老师',
      subject: '语文',
      likes: 18,
      comments: 12,
      views: 203,
      createdAt: '2024-06-18',
      tags: ['教学案例', '创新方法', '语文']
    },
    {
      id: 3,
      title: '化学实验安全问题讨论',
      content: '在化学实验教学中，安全问题是重中之重。想和大家分享一些实验安全的经验...',
      author: '张老师',
      subject: '化学',
      likes: 15,
      comments: 6,
      views: 89,
      createdAt: '2024-06-15',
      tags: ['实验安全', '化学', '教学经验']
    }
  ];

  const handleLike = (postId: number) => {
    toast({
      title: "点赞成功",
      description: "感谢您的支持！",
    });
  };

  const handleComment = (postId: number) => {
    toast({
      title: "评论功能",
      description: "正在打开评论界面...",
    });
  };

  const handleShare = (postId: number) => {
    toast({
      title: "分享成功",
      description: "帖子链接已复制到剪贴板",
    });
  };

  const handleCreatePost = () => {
    if (!newPostTitle.trim() || !newPostContent.trim()) {
      toast({
        title: "请填写完整信息",
        description: "标题和内容不能为空",
        variant: "destructive",
      });
      return;
    }

    toast({
      title: "发布成功",
      description: "您的帖子已发布到社区",
    });
    setNewPostTitle('');
    setNewPostContent('');
  };

  const handleResourceClick = (resourceTitle: string) => {
    toast({
      title: "查看资源",
      description: `正在查看 "${resourceTitle}"...`,
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      {/* 顶部导航栏 */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <Button
                variant="ghost"
                onClick={() => navigate('/dashboard')}
                className="flex items-center space-x-2"
              >
                <ArrowLeft className="h-4 w-4" />
                <span>返回</span>
              </Button>
              <h1 className="text-2xl font-bold text-gray-900">教师社区</h1>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 主要内容区 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 发布新帖子 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Plus className="h-5 w-5" />
                  <span>发布新帖子</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Input
                  placeholder="请输入帖子标题..."
                  value={newPostTitle}
                  onChange={(e) => setNewPostTitle(e.target.value)}
                />
                <Textarea
                  placeholder="分享您的想法或问题..."
                  value={newPostContent}
                  onChange={(e) => setNewPostContent(e.target.value)}
                  rows={4}
                />
                <Button onClick={handleCreatePost} className="w-full">
                  发布帖子
                </Button>
              </CardContent>
            </Card>

            {/* 帖子列表 */}
            <div className="space-y-6">
              {posts.map((post) => (
                <Card key={post.id} className="hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-semibold">
                          {post.author.charAt(0)}
                        </div>
                        <div>
                          <p className="font-semibold text-gray-900">{post.author}</p>
                          <p className="text-sm text-gray-500">{post.createdAt}</p>
                        </div>
                      </div>
                      <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-sm">
                        {post.subject}
                      </span>
                    </div>
                    <CardTitle className="text-lg font-semibold text-gray-900 mt-3">
                      {post.title}
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-gray-700 mb-4">{post.content}</p>
                    
                    <div className="flex flex-wrap gap-2 mb-4">
                      {post.tags.map((tag, index) => (
                        <span key={index} className="bg-gray-100 text-gray-700 px-2 py-1 rounded-full text-xs">
                          #{tag}
                        </span>
                      ))}
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-6">
                        <button 
                          onClick={() => handleLike(post.id)}
                          className="flex items-center space-x-1 text-gray-500 hover:text-red-500 transition-colors"
                        >
                          <Heart className="h-4 w-4" />
                          <span>{post.likes}</span>
                        </button>
                        <button 
                          onClick={() => handleComment(post.id)}
                          className="flex items-center space-x-1 text-gray-500 hover:text-blue-500 transition-colors"
                        >
                          <MessageCircle className="h-4 w-4" />
                          <span>{post.comments}</span>
                        </button>
                        <button 
                          onClick={() => handleShare(post.id)}
                          className="flex items-center space-x-1 text-gray-500 hover:text-green-500 transition-colors"
                        >
                          <Share2 className="h-4 w-4" />
                        </button>
                      </div>
                      <div className="flex items-center space-x-1 text-gray-500">
                        <Eye className="h-4 w-4" />
                        <span>{post.views}</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          {/* 侧边栏 */}
          <div className="space-y-6">
            {/* 热门资源 */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg font-semibold">热门资源</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div 
                  onClick={() => handleResourceClick('化学实验安全指南')}
                  className="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                >
                  <h4 className="font-medium text-gray-900 mb-1">化学实验安全指南</h4>
                  <p className="text-sm text-gray-600 mb-2">适用于高中化学教学</p>
                  <div className="flex items-center text-xs text-gray-500">
                    <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded">化学</span>
                    <span className="ml-2">1.2k 下载</span>
                  </div>
                </div>
                <div 
                  onClick={() => handleResourceClick('古诗词情感分析')}
                  className="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                >
                  <h4 className="font-medium text-gray-900 mb-1">古诗词情感分析</h4>
                  <p className="text-sm text-gray-600 mb-2">创新的语文教学方法</p>
                  <div className="flex items-center text-xs text-gray-500">
                    <span className="bg-green-100 text-green-800 px-2 py-1 rounded">语文</span>
                    <span className="ml-2">956 下载</span>
                  </div>
                </div>
                <div 
                  onClick={() => handleResourceClick('几何图形可视化')}
                  className="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                >
                  <h4 className="font-medium text-gray-900 mb-1">几何图形可视化</h4>
                  <p className="text-sm text-gray-600 mb-2">互动式数学教学工具</p>
                  <div className="flex items-center text-xs text-gray-500">
                    <span className="bg-purple-100 text-purple-800 px-2 py-1 rounded">数学</span>
                    <span className="ml-2">834 下载</span>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* 活跃用户 */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg font-semibold">活跃用户</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {['王老师', '李老师', '张老师', '陈老师'].map((teacher, index) => (
                  <div key={teacher} className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                      {teacher.charAt(0)}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{teacher}</p>
                      <p className="text-xs text-gray-500">贡献了 {Math.floor(Math.random() * 20) + 5} 个资源</p>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Community;
