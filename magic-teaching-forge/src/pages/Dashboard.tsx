
import React from 'react';
import DashboardHeader from '@/components/dashboard/DashboardHeader';
import StatsCards from '@/components/dashboard/StatsCards';
import CreateProjectButton from '@/components/dashboard/CreateProjectButton';
import RecentProjects from '@/components/dashboard/RecentProjects';
import CommunityResources from '@/components/dashboard/CommunityResources';

const Dashboard = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      <DashboardHeader />
      
      <main className="max-w-7xl mx-auto px-6 py-8">
        <StatsCards />
        <CreateProjectButton />
        <RecentProjects />
        <CommunityResources />
      </main>
    </div>
  );
};

export default Dashboard;
