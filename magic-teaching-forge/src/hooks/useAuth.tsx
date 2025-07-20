
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types/api';
import { apiService } from '@/services/api';
import { toast } from '@/hooks/use-toast';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<boolean>;
  register: (username: string, email: string, password: string, role?: 'teacher' | 'admin', subject?: string, institution?: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// 检查token是否存在的辅助函数
const hasToken = (): boolean => {
  return !!localStorage.getItem('token');
};

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (hasToken()) {
      // 有token就尝试获取用户信息，但失败不影响认证状态
      getCurrentUser();
    } else {
      setLoading(false);
    }
  }, []);

  const getCurrentUser = async () => {
    try {
      const user = await apiService.getCurrentUser();
      setUser(user);
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 不清除token，不影响认证状态
      // 用户信息获取失败不代表未认证，只是暂时无法显示用户信息
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      const response = await apiService.login({ email, password });
      // 简单存储token，JWT包含过期时间信息
      localStorage.setItem('token', response.token);
      setUser(response.user);
      toast({
        title: "登录成功",
        description: `欢迎回来，${response.user.username}!`,
      });
      return true;
    } catch (error: any) {
      toast({
        title: "登录失败",
        description: error.message || "请检查您的邮箱和密码",
        variant: "destructive",
      });
      return false;
    }
  };

  const register = async (username: string, email: string, password: string, role: 'teacher' | 'admin' = 'teacher', subject?: string, institution?: string): Promise<boolean> => {
    try {
      const user = await apiService.register({ 
        username, 
        email, 
        password, 
        role, 
        subject, 
        institution 
      });
      // 注册成功后需要重新登录获取token
      const loginSuccess = await login(email, password);
      if (loginSuccess) {
        toast({
          title: "注册成功",
          description: `欢迎加入，${user.username}!`,
        });
        return true;
      }
      return false;
    } catch (error: any) {
      toast({
        title: "注册失败",
        description: error.message || "注册过程中出现错误",
        variant: "destructive",
      });
      return false;
    }
  };

  const logout = async () => {
    try {
      await apiService.logout();
    } catch (error) {
      console.error('登出API调用失败:', error);
    } finally {
      // 只在用户主动登出时清除token
      localStorage.removeItem('token');
      setUser(null);
      toast({
        title: "已退出登录",
        description: "您已成功退出账户",
      });
    }
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    // 完全基于token存在性判断认证状态
    isAuthenticated: hasToken(),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
