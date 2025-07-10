
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { 
  ArrowLeft, 
  TrendingUp, 
  Users, 
  BookOpen, 
  Clock,
  BarChart3,
  PieChart,
  Activity,
  Award
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart as RechartsPieChart, Cell } from 'recharts';

const Analytics = () => {
  const navigate = useNavigate();
  const [timeRange, setTimeRange] = useState('30d');

  // 模拟数据
  const learningTrends = [
    { date: '6/1', students: 45, engagement: 78 },
    { date: '6/8', students: 52, engagement: 82 },
    { date: '6/15', students: 48, engagement: 85 },
    { date: '6/22', students: 65, engagement: 88 },
    { date: '6/29', students: 72, engagement: 91 }
  ];

  const subjectData = [
    { subject: '数学', projects: 8, avgScore: 85 },
    { subject: '语文', projects: 6, avgScore: 78 },
    { subject: '英语', projects: 5, avgScore: 82 },
    { subject: '物理', projects: 4, avgScore: 79 },
    { subject: '化学', projects: 3, avgScore: 88 }
  ];

  const engagementData = [
    { name: '高度参与', value: 65, color: '#10B981' },
    { name: '中等参与', value: 25, color: '#F59E0B' },
    { name: '低参与', value: 10, color: '#EF4444' }
  ];

  const stats = [
    { 
      title: '总学习时长', 
      value: '1,247', 
      unit: '小时',
      change: '+12%',
      icon: Clock,
      color: 'text-blue-600'
    },
    { 
      title: '活跃学生数', 
      value: '156', 
      unit: '人',
      change: '+8%',
      icon: Users,
      color: 'text-green-600'
    },
    { 
      title: '完成率', 
      value: '89', 
      unit: '%',
      change: '+5%',
      icon: Award,
      color: 'text-purple-600'
    },
    { 
      title: '满意度', 
      value: '4.8', 
      unit: '/5.0',
      change: '+0.2',
      icon: TrendingUp,
      color: 'text-orange-600'
    }
  ];

  const handleExport = () => {
    toast({
      title: "导出报告",
      description: "正在生成分析报告...",
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航 */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <Button 
                variant="ghost" 
                onClick={() => navigate('/dashboard')}
                className="text-gray-600 hover:text-gray-900"
              >
                <ArrowLeft className="mr-2 h-4 w-4" />
                返回
              </Button>
              <div>
                <h1 className="text-xl font-bold text-gray-900">教学分析</h1>
                <p className="text-sm text-gray-600">深入了解您的教学效果</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Select value={timeRange} onValueChange={setTimeRange}>
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="7d">7天</SelectItem>
                  <SelectItem value="30d">30天</SelectItem>
                  <SelectItem value="90d">90天</SelectItem>
                  <SelectItem value="1y">1年</SelectItem>
                </SelectContent>
              </Select>
              <Button onClick={handleExport}>
                <BarChart3 className="mr-2 h-4 w-4" />
                导出报告
              </Button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-6 py-8">
        {/* 统计概览 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          {stats.map((stat, index) => (
            <Card key={index}>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600 mb-1">{stat.title}</p>
                    <div className="flex items-baseline space-x-2">
                      <p className="text-2xl font-bold">{stat.value}</p>
                      <span className="text-sm text-gray-500">{stat.unit}</span>
                    </div>
                    <p className="text-sm text-green-600 mt-1">{stat.change}</p>
                  </div>
                  <stat.icon className={`h-8 w-8 ${stat.color}`} />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* 学习趋势图表 */}
          <Card>
            <CardHeader>
              <CardTitle>学习趋势</CardTitle>
              <CardDescription>学生数量和参与度变化</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={learningTrends}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="students" stroke="#8884d8" name="学生数" />
                  <Line type="monotone" dataKey="engagement" stroke="#82ca9d" name="参与度" />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* 参与度分布 */}
          <Card>
            <CardHeader>
              <CardTitle>学生参与度分布</CardTitle>
              <CardDescription>按参与程度分类的学生比例</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <RechartsPieChart>
                  <RechartsPieChart data={engagementData} cx="50%" cy="50%" outerRadius={80} dataKey="value">
                    {engagementData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </RechartsPieChart>
                  <Tooltip />
                </RechartsPieChart>
              </ResponsiveContainer>
              <div className="flex justify-center space-x-4 mt-4">
                {engagementData.map((item, index) => (
                  <div key={index} className="flex items-center space-x-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }}></div>
                    <span className="text-sm text-gray-600">{item.name}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 学科表现 */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle>各学科表现</CardTitle>
            <CardDescription>项目数量和平均得分统计</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={subjectData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="subject" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="projects" fill="#8884d8" name="项目数" />
                <Bar dataKey="avgScore" fill="#82ca9d" name="平均得分" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* 详细数据表格 */}
        <Card>
          <CardHeader>
            <CardTitle>详细统计</CardTitle>
            <CardDescription>各项指标的详细数据</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left py-3 px-4">学科</th>
                    <th className="text-left py-3 px-4">项目数</th>
                    <th className="text-left py-3 px-4">学生数</th>
                    <th className="text-left py-3 px-4">完成率</th>
                    <th className="text-left py-3 px-4">平均得分</th>
                    <th className="text-left py-3 px-4">满意度</th>
                  </tr>
                </thead>
                <tbody>
                  {subjectData.map((item, index) => (
                    <tr key={index} className="border-b hover:bg-gray-50">
                      <td className="py-3 px-4 font-medium">{item.subject}</td>
                      <td className="py-3 px-4">{item.projects}</td>
                      <td className="py-3 px-4">{Math.floor(Math.random() * 50) + 20}</td>
                      <td className="py-3 px-4">{Math.floor(Math.random() * 20) + 80}%</td>
                      <td className="py-3 px-4">{item.avgScore}</td>
                      <td className="py-3 px-4">{(Math.random() * 1 + 4).toFixed(1)}/5.0</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Analytics;
