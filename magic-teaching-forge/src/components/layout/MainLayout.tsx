import React, { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { Badge } from '@/components/ui/badge';
import { 
  Home, 
  Wand2, 
  BookOpen, 
  Upload, 
  BarChart3, 
  MessageSquare, 
  FolderOpen,
  Settings,
  Bell,
  Search,
  Menu,
  User,
  LogOut,
  Sparkles,
  Brain,
  Zap,
  ChevronDown,
  ChevronRight,
  Sun,
  Moon,
  Palette,
  Cpu,
  Database,
  Globe
} from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import LoginDialog from '@/components/auth/LoginDialog';
import { toast } from '@/hooks/use-toast';
import { cn } from '@/lib/utils';

interface NavigationItem {
  title: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
  description?: string;
}



const MainLayout: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [activeDropdown, setActiveDropdown] = useState<string | null>(null);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuth();
  const [showLoginDialog, setShowLoginDialog] = useState(false);

  const navigationItems = [
    { 
      icon: Home, 
      label: '首页', 
      path: '/dashboard', 
      gradient: 'from-blue-500 to-purple-600'
    },
    { 
      icon: MessageSquare, 
      label: 'AI对话', 
      path: '/chat', 
      gradient: 'from-green-500 to-teal-600'
    },
    { 
      icon: Wand2, 
      label: 'AI创作', 
      path: '/ai-generation', 
      badge: 'New',
      gradient: 'from-purple-500 to-pink-600',
      subItems: [
        { label: 'PPT课件生成', path: '/ai-generation?type=ppt' },
        { label: '习题集生成', path: '/ai-generation?type=quiz' },
        { label: '讲解文本生成', path: '/ai-generation?type=explanation' }
      ]
    },
    { 
      icon: Database, 
      label: '资源中心', 
      path: '/resource-center', 
      gradient: 'from-orange-500 to-red-600',
      subItems: [
        { label: '资源管理', path: '/resource-center?tab=resources' },
        { label: '知识库', path: '/resource-center?tab=knowledge' },
        { label: '智能问答', path: '/resource-center?tab=qa' }
      ]
    },
    { 
      icon: BarChart3, 
      label: '数据洞察', 
      path: '/analytics', 
      gradient: 'from-indigo-500 to-blue-600'
    }
  ];

  const handleNavigation = (path: string) => {
    navigate(path);
    setMobileMenuOpen(false);
    setActiveDropdown(null);
  };

  const toggleDropdown = (label: string) => {
    setActiveDropdown(activeDropdown === label ? null : label);
  };

  const handleLogout = () => {
    logout();
    toast({
      title: "已退出登录",
      description: "您已成功退出登录",
    });
  };

  const handleNotificationClick = () => {
    toast({
      title: "通知中心",
      description: "暂无新通知",
    });
  };

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode);
    document.documentElement.classList.toggle('dark');
  };

  const SidebarContent = () => (
    <div className="flex flex-col h-full">
      {/* Logo */}
      <div className="p-6 border-b border-border/50">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-blue-600 rounded-xl flex items-center justify-center">
            <Brain className="w-6 h-6 text-white" />
          </div>
          <div>
            <h2 className="text-xl font-bold bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent">
              EduForge
            </h2>
            <p className="text-xs text-muted-foreground">AI教学设计平台</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-2">
        {navigationItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <Button
              key={item.path}
              variant={isActive ? "secondary" : "ghost"}
              className={cn(
                "w-full justify-start h-12 px-4 group transition-all duration-200",
                isActive 
                  ? "bg-primary/10 text-primary border border-primary/20 shadow-sm" 
                  : "hover:bg-accent/50 hover:translate-x-1"
              )}
              onClick={() => {
                navigate(item.path);
                setMobileMenuOpen(false);
              }}
            >
              <item.icon className={cn(
                "w-5 h-5 mr-3 transition-colors",
                isActive ? "text-primary" : "text-muted-foreground group-hover:text-foreground"
              )} />
              <div className="flex-1 text-left">
                <div className="flex items-center justify-between">
                  <span className="font-medium">{item.label}</span>
                  {item.badge && (
                    <Badge variant="secondary" className="ml-2 text-xs px-2 py-0.5">
                      {item.badge}
                    </Badge>
                  )}
                </div>
                <p className="text-xs text-muted-foreground mt-0.5 truncate">
                  {/* 由于navigationItems中没有定义description属性,这里暂时移除description的显示 */}
                </p>
              </div>
              {isActive && <ChevronRight className="w-4 h-4 ml-2" />}
            </Button>
          );
        })}
      </nav>

      {/* User Section */}
      {isAuthenticated && (
        <div className="p-4 border-t border-border/50">
          <div className="flex items-center space-x-3 p-3 rounded-lg bg-accent/30">
            <Avatar className="w-10 h-10">
              <AvatarImage src={user?.avatar} alt={user?.username} />
              <AvatarFallback className="bg-gradient-to-br from-purple-500 to-blue-600 text-white">
                {user?.username?.[0]?.toUpperCase()}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 min-w-0">
              <p className="font-medium text-sm truncate">{user?.username}</p>
              <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-gray-900 dark:via-blue-900 dark:to-indigo-900">
      {/* Top Navigation Bar */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-white/70 dark:bg-gray-900/70 backdrop-blur-xl border-b border-white/20 dark:border-gray-700/20 shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <div className="flex items-center space-x-3">
              <div className="relative">
                <div className="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
                  <Sparkles className="h-6 w-6 text-white" />
                </div>
                <div className="absolute -top-1 -right-1 w-4 h-4 bg-gradient-to-r from-pink-500 to-rose-500 rounded-full animate-pulse" />
              </div>
              <div className="hidden sm:block">
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white drop-shadow-sm">
                  AI教学设计平台
                </h1>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-300">智能化教育解决方案</p>
              </div>
            </div>

            {/* Desktop Navigation */}
             <div className="hidden lg:flex items-center space-x-2">
               {navigationItems.map((item) => {
                const isActive = location.pathname === item.path;
                const hasSubItems = item.subItems && item.subItems.length > 0;
                
                return (
                  <div key={item.label} className="relative">
                    <button
                      onClick={() => hasSubItems ? toggleDropdown(item.label) : handleNavigation(item.path)}
                      className={cn(
                        'group flex items-center space-x-3 px-5 py-3 rounded-2xl text-sm font-medium transition-all duration-300 relative overflow-hidden min-w-fit whitespace-nowrap',
                        isActive
                          ? `bg-gradient-to-r ${item.gradient} text-white shadow-xl shadow-indigo-500/25 scale-105 ring-2 ring-white/20`
                          : 'text-gray-700 dark:text-gray-300 hover:bg-white/60 dark:hover:bg-gray-800/60 hover:shadow-lg hover:scale-102'
                      )}
                    >
                      <div className={cn(
                        'flex items-center justify-center transition-all duration-300',
                        isActive ? 'text-white' : `text-gray-500 group-hover:text-indigo-600 dark:group-hover:text-indigo-400`
                      )}>
                        <item.icon className="h-5 w-5 flex-shrink-0" />
                      </div>
                      <span className="font-semibold tracking-wide">{item.label}</span>
                      {item.badge && (
                        <Badge 
                          variant={isActive ? "secondary" : "default"} 
                          className={cn(
                            "text-xs px-2.5 py-1 font-medium flex-shrink-0",
                            isActive ? "bg-white/25 text-white border-white/30" : "bg-gradient-to-r from-indigo-100 to-purple-100 text-indigo-700 border-indigo-200"
                          )}
                        >
                          {item.badge}
                        </Badge>
                      )}
                      {hasSubItems && (
                        <ChevronDown className={cn(
                          "h-4 w-4 transition-transform duration-300 flex-shrink-0",
                          activeDropdown === item.label ? "rotate-180" : ""
                        )} />
                      )}
                      {isActive && (
                        <div className="absolute inset-0 bg-gradient-to-r from-white/15 to-white/5 rounded-2xl" />
                      )}
                    </button>
                    
                    {/* Dropdown Menu */}
                    {hasSubItems && activeDropdown === item.label && (
                      <div className="absolute top-full left-0 mt-3 w-64 bg-white/95 dark:bg-gray-800/95 backdrop-blur-2xl rounded-2xl shadow-2xl border border-white/30 dark:border-gray-700/30 py-3 z-50 animate-in slide-in-from-top-2 duration-300">
                        <div className="px-3 pb-2 mb-2 border-b border-gray-200/50 dark:border-gray-700/50">
                          <p className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">{item.label}</p>
                        </div>
                        {item.subItems?.map((subItem, index) => (
                          <button
                            key={subItem.path}
                            onClick={() => handleNavigation(subItem.path)}
                            className="w-full text-left px-4 py-3 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gradient-to-r hover:from-indigo-50 hover:to-purple-50 dark:hover:from-gray-700/50 dark:hover:to-gray-600/50 hover:text-indigo-700 dark:hover:text-indigo-300 transition-all duration-300 rounded-xl mx-2 group"
                            style={{ animationDelay: `${index * 50}ms` }}
                          >
                            <div className="flex items-center space-x-3">
                              <div className="w-2 h-2 rounded-full bg-gradient-to-r from-indigo-400 to-purple-400 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                              <span className="group-hover:translate-x-1 transition-transform duration-300">{subItem.label}</span>
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

            {/* Right Side Actions */}
            <div className="flex items-center space-x-3">
              {/* Search */}
              <div className="hidden md:block relative">
                <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  className="w-72 pl-12 pr-6 py-3 bg-white/60 dark:bg-gray-800/60 border border-white/30 dark:border-gray-700/30 rounded-2xl text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 backdrop-blur-xl transition-all duration-300 hover:bg-white/70 dark:hover:bg-gray-800/70"
                  placeholder="搜索课程、资源、模板..."
                  type="search"
                />
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                  <kbd className="px-2 py-1 text-xs text-gray-400 bg-gray-100/50 dark:bg-gray-700/50 rounded border border-gray-200/50 dark:border-gray-600/50">⌘K</kbd>
                </div>
              </div>

              {/* Theme Toggle */}
              <Button 
                variant="ghost" 
                size="sm" 
                onClick={toggleTheme}
                className="rounded-xl hover:bg-white/50 dark:hover:bg-gray-800/50"
              >
                {isDarkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
              </Button>
              
              {/* Notifications */}
              <Button 
                variant="ghost" 
                size="sm" 
                className="relative rounded-xl hover:bg-white/50 dark:hover:bg-gray-800/50"
              >
                <Bell className="h-5 w-5" />
                <span className="absolute -top-1 -right-1 h-4 w-4 bg-gradient-to-r from-red-500 to-pink-500 rounded-full text-xs text-white flex items-center justify-center animate-pulse">
                  3
                </span>
              </Button>

              {/* User Menu */}
              {user ? (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" className="relative h-10 w-10 rounded-xl hover:bg-white/50 dark:hover:bg-gray-800/50">
                      <Avatar className="h-8 w-8">
                        <AvatarImage src={user.avatar} alt={user.name} />
                        <AvatarFallback className="bg-gradient-to-br from-indigo-500 to-purple-600 text-white">
                          {user.username?.charAt(0) || 'U'}
                        </AvatarFallback>
                      </Avatar>
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent className="w-56 bg-white/90 dark:bg-gray-800/90 backdrop-blur-xl border border-white/20 dark:border-gray-700/20" align="end">
                    <div className="flex items-center justify-start gap-2 p-2">
                      <div className="flex flex-col space-y-1 leading-none">
                        <p className="font-medium">{user.username}</p>
                        <p className="w-[200px] truncate text-sm text-muted-foreground">
                          {user.email}
                        </p>
                      </div>
                    </div>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem>
                      <User className="mr-2 h-4 w-4" />
                      <span>个人资料</span>
                    </DropdownMenuItem>
                    <DropdownMenuItem>
                      <Settings className="mr-2 h-4 w-4" />
                      <span>设置</span>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onClick={handleLogout}>
                      <LogOut className="mr-2 h-4 w-4" />
                      <span>退出登录</span>
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              ) : (
                <Button 
                  onClick={() => setShowLoginDialog(true)} 
                  size="sm"
                  className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white rounded-xl"
                >
                  登录
                </Button>
              )}

              {/* Mobile Menu Button */}
              <Button
                variant="ghost"
                size="sm"
                className="lg:hidden rounded-xl hover:bg-white/50 dark:hover:bg-gray-800/50"
                onClick={() => setMobileMenuOpen(true)}
              >
                <Menu className="h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>
      </nav>

      {/* Mobile Menu */}
      <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
        <SheetContent side="left" className="w-80 p-0 bg-white/95 dark:bg-gray-900/95 backdrop-blur-xl">
          <div className="flex flex-col h-full">
            {/* Mobile Header */}
            <div className="flex items-center space-x-3 p-6 border-b border-gray-200/50 dark:border-gray-700/50">
              <div className="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center">
                <Sparkles className="h-6 w-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900 dark:text-white">
                  AI教学设计平台
                </h1>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-300">智能化教育解决方案</p>
              </div>
            </div>

            {/* Mobile Navigation */}
            <div className="flex-1 overflow-y-auto p-4">
              <div className="space-y-3">
                 {navigationItems.map((item) => {
                  const isActive = location.pathname === item.path;
                  const hasSubItems = item.subItems && item.subItems.length > 0;
                  
                  return (
                    <div key={item.label}>
                      <button
                        onClick={() => hasSubItems ? toggleDropdown(item.label) : handleNavigation(item.path)}
                        className={cn(
                          'w-full flex items-center justify-between p-4 rounded-2xl text-sm font-semibold transition-all duration-300 min-h-[60px]',
                          isActive
                            ? `bg-gradient-to-r ${item.gradient} text-white shadow-xl ring-2 ring-white/20`
                            : 'text-gray-700 dark:text-gray-300 hover:bg-gradient-to-r hover:from-gray-50 hover:to-gray-100 dark:hover:from-gray-800 dark:hover:to-gray-700 hover:shadow-lg'
                        )}
                      >
                        <div className="flex items-center space-x-4 flex-1 min-w-0">
                          <div className={cn(
                            'flex items-center justify-center w-10 h-10 rounded-xl transition-all duration-300',
                            isActive ? 'bg-white/20' : 'bg-gray-100 dark:bg-gray-800'
                          )}>
                            <item.icon className={cn(
                              "h-5 w-5 transition-colors duration-300",
                              isActive ? 'text-white' : 'text-gray-600 dark:text-gray-400'
                            )} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center space-x-2">
                              <span className="truncate">{item.label}</span>
                              {item.badge && (
                                <Badge 
                                  variant={isActive ? "secondary" : "default"} 
                                  className={cn(
                                    "text-xs px-2.5 py-1 font-medium flex-shrink-0",
                                    isActive ? "bg-white/25 text-white border-white/30" : "bg-gradient-to-r from-indigo-100 to-purple-100 text-indigo-700 border-indigo-200"
                                  )}
                                >
                                  {item.badge}
                                </Badge>
                              )}
                            </div>
                          </div>
                        </div>
                        {hasSubItems && (
                          <ChevronDown className={cn(
                            "h-5 w-5 transition-transform duration-300 flex-shrink-0 ml-2",
                            activeDropdown === item.label ? "rotate-180" : ""
                          )} />
                        )}
                      </button>
                      
                      {/* Mobile Submenu */}
                      {hasSubItems && activeDropdown === item.label && (
                        <div className="mt-3 ml-6 space-y-2 animate-in slide-in-from-top-2 duration-300">
                          {item.subItems?.map((subItem, index) => (
                            <button
                              key={subItem.path}
                              onClick={() => handleNavigation(subItem.path)}
                              className="w-full text-left p-3 text-sm font-medium text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-gradient-to-r hover:from-indigo-50 hover:to-purple-50 dark:hover:from-gray-800/50 dark:hover:to-gray-700/50 rounded-xl transition-all duration-300 group"
                              style={{ animationDelay: `${index * 50}ms` }}
                            >
                              <div className="flex items-center space-x-3">
                                <div className="w-2 h-2 rounded-full bg-gradient-to-r from-indigo-400 to-purple-400 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                                <span className="group-hover:translate-x-1 transition-transform duration-300">{subItem.label}</span>
                              </div>
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Mobile Footer */}
            <div className="p-4 border-t border-gray-200/50 dark:border-gray-700/50">
              <div className="bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-gray-800 dark:to-gray-700 rounded-xl p-4">
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center">
                    <Brain className="h-4 w-4 text-white" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-900 dark:text-white">AI助手</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">随时为您服务</p>
                  </div>
                  <Zap className="h-4 w-4 text-yellow-500 animate-pulse" />
                </div>
              </div>
            </div>
          </div>
        </SheetContent>
      </Sheet>

      {/* Main Content */}
      <main className="pt-16">
        <div className="min-h-screen">
          <Outlet />
        </div>
      </main>

      <LoginDialog open={showLoginDialog} onOpenChange={setShowLoginDialog} />
    </div>
  );
};

export default MainLayout;