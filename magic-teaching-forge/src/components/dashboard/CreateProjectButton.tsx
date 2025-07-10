
import React from 'react';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';

const CreateProjectButton = () => {
  const navigate = useNavigate();

  const handleCreateProject = () => {
    console.log('创建新项目按钮被点击');
    toast({
      title: "创建新项目",
      description: "正在进入创建向导...",
    });
    navigate('/create');
  };

  return (
    <div className="text-center mb-12">
      <div className="relative inline-block">
        <Button 
          onClick={handleCreateProject}
          size="lg"
          className="relative z-10 bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white px-12 py-6 text-lg font-semibold rounded-2xl shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300"
        >
          <Plus className="mr-3 h-6 w-6" />
          创建新的教学设计
        </Button>
        <div className="absolute -inset-1 bg-gradient-to-r from-purple-600 to-blue-600 rounded-2xl blur opacity-25 animate-pulse pointer-events-none"></div>
      </div>
      <p className="text-gray-600 mt-4 text-lg">让AI为您生成专业的教学资源</p>
    </div>
  );
};

export default CreateProjectButton;
