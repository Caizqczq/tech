
import React, { useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAuth } from '@/hooks/useAuth';
import { toast } from '@/hooks/use-toast';
import { Loader2 } from 'lucide-react';

interface LoginDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const LoginDialog: React.FC<LoginDialogProps> = ({ open, onOpenChange }) => {
  const { login, register } = useAuth();
  const [loading, setLoading] = useState(false);
  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [registerForm, setRegisterForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'teacher' as 'teacher' | 'admin',
    subject: '',
    institution: ''
  });

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const success = await login(loginForm.email, loginForm.password);
    if (success) {
      onOpenChange(false);
      setLoginForm({ email: '', password: '' });
    }
    setLoading(false);
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (registerForm.password !== registerForm.confirmPassword) {
      return;
    }
    setLoading(true);
    try {
      const success = await register(
        registerForm.username,
        registerForm.email,
        registerForm.password,
        registerForm.role,
        registerForm.subject || undefined,
        registerForm.institution || undefined
      );
      if (success) {
        toast({
          title: "注册成功",
          description: "欢迎加入智能教学平台！",
        });
        onOpenChange(false);
        setRegisterForm({ username: '', email: '', password: '', confirmPassword: '', role: 'teacher', subject: '', institution: '' });
      }
    } catch (error) {
      toast({
        title: "注册失败",
        description: "请检查输入信息",
        variant: "destructive",
      });
    }
    setLoading(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>欢迎使用AI教学设计平台</DialogTitle>
          <DialogDescription>登录或注册您的账户以开始使用</DialogDescription>
        </DialogHeader>
        
        <Tabs defaultValue="login" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="login">登录</TabsTrigger>
            <TabsTrigger value="register">注册</TabsTrigger>
          </TabsList>
          
          <TabsContent value="login" className="space-y-4">
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="login-email">邮箱</Label>
                <Input
                  id="login-email"
                  type="email"
                  value={loginForm.email}
                  onChange={(e) => setLoginForm(prev => ({ ...prev, email: e.target.value }))}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="login-password">密码</Label>
                <Input
                  id="login-password"
                  type="password"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm(prev => ({ ...prev, password: e.target.value }))}
                  required
                />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                登录
              </Button>
            </form>
          </TabsContent>
          
          <TabsContent value="register" className="space-y-4">
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="register-username">姓名</Label>
                <Input
                  id="register-username"
                  type="text"
                  placeholder="请输入您的真实姓名"
                  value={registerForm.username}
                  onChange={(e) => setRegisterForm(prev => ({ ...prev, username: e.target.value }))}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="register-email">邮箱</Label>
                <Input
                  id="register-email"
                  type="email"
                  placeholder="请输入邮箱地址"
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm(prev => ({ ...prev, email: e.target.value }))}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="register-role">身份</Label>
                <Select value={registerForm.role} onValueChange={(value: 'teacher' | 'admin') => setRegisterForm(prev => ({ ...prev, role: value }))}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="teacher">教师</SelectItem>
                    <SelectItem value="admin">管理员</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              {registerForm.role === 'teacher' && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="register-subject">任教学科</Label>
                    <Input
                      id="register-subject"
                      type="text"
                      placeholder="如：数学、物理、化学等"
                      value={registerForm.subject}
                      onChange={(e) => setRegisterForm(prev => ({ ...prev, subject: e.target.value }))}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="register-institution">所在机构</Label>
                    <Input
                      id="register-institution"
                      type="text"
                      placeholder="如：XX大学、XX中学等"
                      value={registerForm.institution}
                      onChange={(e) => setRegisterForm(prev => ({ ...prev, institution: e.target.value }))}
                    />
                  </div>
                </>
              )}
              <div className="space-y-2">
                <Label htmlFor="register-password">密码</Label>
                <Input
                  id="register-password"
                  type="password"
                  placeholder="请设置密码（至少6位）"
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm(prev => ({ ...prev, password: e.target.value }))}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="register-confirm">确认密码</Label>
                <Input
                  id="register-confirm"
                  type="password"
                  placeholder="请再次输入密码"
                  value={registerForm.confirmPassword}
                  onChange={(e) => setRegisterForm(prev => ({ ...prev, confirmPassword: e.target.value }))}
                  required
                />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                注册账户
              </Button>
            </form>
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
};

export default LoginDialog;
