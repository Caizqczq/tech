
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

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token'); // 改为 'token'
    if (token) {
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
      localStorage.removeItem('token'); // 改为 'token'
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      const response = await apiService.login({ email, password });
      localStorage.setItem('token', response.token); // 改为 'token'
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
      localStorage.removeItem('token'); // 改为 'token'
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
    isAuthenticated: !!user,
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
