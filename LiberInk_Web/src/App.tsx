import { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import SidebarLayout from './components/layout/SidebarLayout';

const LibraryPage = lazy(() => import('./pages/LibraryPage'));
const SettingsPage = lazy(() => import('./pages/SettingsPage'));
const EditorPage = lazy(() => import('./pages/EditorPage'));
const LandingPage = lazy(() => import('./pages/LandingPage'));
const OnboardingPage = lazy(() => import('./pages/OnboardingPage'));
const AuthPage = lazy(() => import('./pages/AuthPage'));

function App() {
  return (
    <Router>
      {/* Suspense відображає цей контент, поки завантажується JS-код сторінки */}
      <Suspense fallback={<div className="h-screen w-full flex items-center justify-center font-serif text-[#4A0404]">Loading LiberInk...</div>}>
        <Routes>
          {/* Публічні маршрути */}
          <Route path="/LandingPage" element={<LandingPage />} />
          <Route path="/onboarding" element={<OnboardingPage />} />
          <Route path="/login" element={<AuthPage />} />
          <Route path="/register" element={<AuthPage />} />

          {/* Приватні маршрути всередині Layout */}
          <Route element={<SidebarLayout />}>
            <Route path="/library" element={<LibraryPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/editor/:id" element={<EditorPage />} />
            
            {/* Додаткова сторінка */}
            <Route path="/export" element={<div className="p-10 font-serif">Сторінка експорту в розробці...</div>} />
          </Route>

          {/* Редирект */}
          <Route path="*" element={<Navigate to="/LandingPage" replace />} />
        </Routes>
      </Suspense>
    </Router>
  );
}

export default App;