import { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

import SidebarLayout from './components/layout/SidebarLayout';

const LibraryPage = lazy(() => import('./pages/LibraryPage'));
const SettingsPage = lazy(() => import('./pages/SettingsPage'));
const EditorPage = lazy(() => import('./pages/EditorPage'));
const LandingPage = lazy(() => import('./pages/LandingPage'));
const OnboardingPage = lazy(() => import('./pages/OnboardingPage'));
const AuthPage = lazy(() => import('./pages/AuthPage'));

const PageTransition = ({ children }: { children: React.ReactNode }) => (
  <motion.div
    initial={{ opacity: 0, y: 10 }}
    animate={{ opacity: 1, y: 0 }}
    exit={{ opacity: 0, y: -10 }}
    transition={{ duration: 0.3, ease: "easeInOut" }}
  >
    {children}
  </motion.div>
);

const AnimatedRoutes = () => {
  const location = useLocation();
  
  // СЕКРЕТ ТУТ: Якщо ми на auth-сторінках, даємо їм спільний ключ
  const isAuthRoute = location.pathname === '/login' || location.pathname === '/register';
  const animationKey = isAuthRoute ? 'auth-flow' : location.pathname;
  
  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={animationKey}>
        <Route path="/LandingPage" element={<PageTransition><LandingPage /></PageTransition>} />
        <Route path="/onboarding" element={<PageTransition><OnboardingPage /></PageTransition>} />
        
        {/* Auth сторінки залишаються як були */}
        <Route path="/login" element={<PageTransition><AuthPage /></PageTransition>} />
        <Route path="/register" element={<PageTransition><AuthPage /></PageTransition>} />

        <Route element={<SidebarLayout />}>
          <Route path="/library" element={<PageTransition><LibraryPage /></PageTransition>} />
          <Route path="/settings" element={<PageTransition><SettingsPage /></PageTransition>} />
          <Route path="/editor/:id" element={<PageTransition><EditorPage /></PageTransition>} />
        </Route>

        <Route path="*" element={<Navigate to="/LandingPage" replace />} />
      </Routes>
    </AnimatePresence>
  );
};

function App() {
  return (
    <Router>
      <Suspense fallback={<div className="h-screen w-full flex items-center justify-center font-serif text-[#4A0404]">Loading LiberInk...</div>}>
        <AnimatedRoutes />
      </Suspense>
    </Router>
  );
}

export default App;