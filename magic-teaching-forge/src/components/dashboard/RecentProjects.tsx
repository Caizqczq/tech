
import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';

const RecentProjects = () => {
  const navigate = useNavigate();
  
  const recentProjects = [
    {
      id: 1,
      title: "光合作用教学设计",
      subject: "生物",
      grade: "高中一年级",
      createdAt: "2024-06-20",
      thumbnail: "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=200&fit=crop",
      progress: 100
    },
    {
      id: 2,
      title: "三角函数基础",
      subject: "数学",
      grade: "高中二年级", 
      createdAt: "2024-06-18",
      thumbnail: "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=400&h=200&fit=crop",
      progress: 85
    },
    {
      id: 3,
      title: "中国古代诗歌鉴赏",
      subject: "语文",
      grade: "高中三年级",
      createdAt: "2024-06-15",
      thumbnail: "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=200&fit=crop",
      progress: 60
    }
  ];

  const handleProjectClick = (projectId: number) => {
    if (projectId === 1) {
      navigate('/project/demo');
    } else {
      navigate(`/project/${projectId}`);
    }
    toast({
      title: "打开项目",
      description: `正在打开项目 ${projectId}...`,
    });
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-900">最近的项目</h2>
        <Button variant="outline" className="text-gray-600 hover:text-gray-900">
          查看全部
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {recentProjects.map((project) => (
          <Card 
            key={project.id} 
            className="group hover:shadow-xl transition-all duration-300 hover:-translate-y-2 cursor-pointer"
            onClick={() => handleProjectClick(project.id)}
          >
            <div className="relative overflow-hidden rounded-t-lg">
              <img 
                src={project.thumbnail} 
                alt={project.title}
                className="w-full h-48 object-cover group-hover:scale-110 transition-transform duration-300"
              />
              <div className="absolute inset-0 bg-black opacity-0 group-hover:opacity-20 transition-opacity"></div>
              <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
                <Button 
                  size="sm" 
                  variant="secondary" 
                  className="bg-white/90 hover:bg-white"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleProjectClick(project.id);
                  }}
                >
                  打开
                </Button>
              </div>
            </div>
            <CardHeader className="pb-2">
              <CardTitle className="text-lg font-semibold text-gray-900 group-hover:text-purple-600 transition-colors">
                {project.title}
              </CardTitle>
              <CardDescription className="text-sm text-gray-600">
                {project.subject} • {project.grade}
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="flex items-center justify-between text-sm text-gray-500 mb-3">
                <div className="flex items-center">
                  <Clock className="h-4 w-4 mr-1" />
                  {project.createdAt}
                </div>
                <span className="text-green-600">{project.progress}% 完成</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-gradient-to-r from-green-400 to-green-600 h-2 rounded-full transition-all duration-300"
                  style={{width: `${project.progress}%`}}
                ></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};

export default RecentProjects;
