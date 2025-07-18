
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "@/hooks/useAuth";
import MainLayout from "@/components/layout/MainLayout";
import Dashboard from "./pages/Dashboard";
import CreateWizard from "./pages/CreateWizard";
import ProjectDetail from "./pages/ProjectDetail";
import Analytics from "./pages/Analytics";
import KnowledgeBase from "./pages/KnowledgeBase";
import MaterialUpload from "./pages/MaterialUpload";
import AIGeneration from "./pages/AIGeneration";
import SmartChat from "./pages/SmartChat";
import NotFound from "./pages/NotFound";
import ResourceCenter from "@/components/ResourceCenter";
import Login from "./pages/Login";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<MainLayout />}>
              <Route index element={<Dashboard />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="create" element={<CreateWizard />} />
              <Route path="project/:id" element={<ProjectDetail />} />
              <Route path="project/demo" element={<ProjectDetail />} />
              <Route path="analytics" element={<Analytics />} />
              <Route path="knowledge" element={<KnowledgeBase />} />
              <Route path="upload" element={<MaterialUpload />} />
              <Route path="ai-generation" element={<AIGeneration />} />
              <Route path="resource-center" element={<ResourceCenter />} />
              <Route path="chat" element={<SmartChat />} />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
