
import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useNavigate } from 'react-router-dom';
import { toast } from '@/hooks/use-toast';

const CommunityResources = () => {
  const navigate = useNavigate();

  const handleCommunityResourceClick = (resourceTitle: string) => {
    navigate('/community');
    toast({
      title: "查看社区资源",
      description: `正在查看 "${resourceTitle}"...`,
    });
  };

  return (
    <div className="mt-12">
      <Card className="bg-gradient-to-r from-blue-50 to-purple-50 border-none">
        <CardHeader>
          <CardTitle className="text-xl font-semibold text-gray-900">社区热门资源</CardTitle>
          <CardDescription>发现其他教师分享的优质教学设计</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div 
              onClick={() => handleCommunityResourceClick('化学实验安全指南')}
              className="bg-white p-4 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            >
              <h4 className="font-medium text-gray-900 mb-2">化学实验安全指南</h4>
              <p className="text-sm text-gray-600 mb-2">适用于高中化学教学</p>
              <div className="flex items-center text-xs text-gray-500">
                <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded">化学</span>
                <span className="ml-2">1.2k 下载</span>
              </div>
            </div>
            <div 
              onClick={() => handleCommunityResourceClick('古诗词情感分析')}
              className="bg-white p-4 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            >
              <h4 className="font-medium text-gray-900 mb-2">古诗词情感分析</h4>
              <p className="text-sm text-gray-600 mb-2">创新的语文教学方法</p>
              <div className="flex items-center text-xs text-gray-500">
                <span className="bg-green-100 text-green-800 px-2 py-1 rounded">语文</span>
                <span className="ml-2">956 下载</span>
              </div>
            </div>
            <div 
              onClick={() => handleCommunityResourceClick('几何图形可视化')}
              className="bg-white p-4 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            >
              <h4 className="font-medium text-gray-900 mb-2">几何图形可视化</h4>
              <p className="text-sm text-gray-600 mb-2">互动式数学教学工具</p>
              <div className="flex items-center text-xs text-gray-500">
                <span className="bg-purple-100 text-purple-800 px-2 py-1 rounded">数学</span>
                <span className="ml-2">834 下载</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default CommunityResources;
