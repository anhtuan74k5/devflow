import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AppLayout from './components/AppLayout';
import IntroAnimation from './components/IntroAnimation';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ProjectListPage from './pages/ProjectListPage';
import CreateProjectPage from './pages/CreateProjectPage';
import ProjectDetailPage from './pages/ProjectDetailPage';
import CreateTaskPage from './pages/CreateTaskPage';
import TaskDetailPage from './pages/TaskDetailPage';
import ActivityFeedPage from './pages/ActivityFeedPage';
import SystemLogsPage from './pages/SystemLogsPage';
import ProfilePage from './pages/ProfilePage';

function AppContent() {
  const location = useLocation();
  const { user } = useAuth();
  const [showIntro, setShowIntro] = useState(() => {
    // Only show intro on the home page for unauthenticated users, and only once per session
    return !user && location.pathname === '/' && !sessionStorage.getItem('introShown');
  });

  const handleIntroComplete = () => {
    sessionStorage.setItem('introShown', 'true');
    setShowIntro(false);
  };

  // Reset intro flag when user logs out (path changes to /login)
  useEffect(() => {
    if (location.pathname === '/login' || location.pathname === '/register') {
      sessionStorage.removeItem('introShown');
    }
  }, [location.pathname]);

  return (
    <>
      {showIntro && <IntroAnimation onComplete={handleIntroComplete} />}
      <Routes>
        {/* Public routes */}
        <Route path="/" element={user ? <Navigate to="/dashboard" replace /> : <LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected routes (require authentication) */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/projects" element={<ProjectListPage />} />
            <Route path="/projects/new" element={<CreateProjectPage />} />
            <Route path="/projects/:id" element={<ProjectDetailPage />} />
            <Route path="/projects/:id/tasks/new" element={<CreateTaskPage />} />
            <Route path="/projects/:id/tasks/:taskId" element={<TaskDetailPage />} />
            <Route path="/projects/:id/activities" element={<ActivityFeedPage />} />
            <Route path="/admin/logs" element={<SystemLogsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
          </Route>
        </Route>
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" />
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}
