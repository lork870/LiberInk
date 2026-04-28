import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Layouts
import SidebarLayout from './components/layout/SidebarLayout';

// Pages (Контейнери для маршрутів)
import LibraryPage from './pages/LibraryPage';
import SettingsPage from './pages/SettingsPage';
import EditorPage from './pages/EditorPage';
import LandingPage from './pages/LandingPage';
import OnboardingPage from './pages/OnboardingPage';
import AuthPage from './pages/AuthPage';

function App() {
  return (
    <Router>
      <Routes>
        {/* Публічні маршрути без бокового меню */}
        <Route path="/LandingPage" element={<LandingPage />} />
        <Route path="/onboarding" element={<OnboardingPage />} />
        <Route path="/login" element={<AuthPage />} />
        <Route path="/register" element={<AuthPage />} />

        {/* Приватні маршрути всередині SidebarLayout */}
        <Route element={<SidebarLayout />}>
          <Route path="/library" element={<LibraryPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          
          {/* Сторінка експорту (можна буде винести в окрему фічу пізніше) */}
          <Route path="/export" element={<div className="p-10 font-serif">Сторінка експорту в розробці...</div>} />
          
          {/* Професійний редактор LiberInk */}
          <Route path="/editor/:id" element={<EditorPage />} />
        </Route>

        {/* Редирект за замовчуванням: якщо шлях не знайдено, ведемо на лендінг */}
        <Route path="*" element={<Navigate to="/LandingPage" replace />} />
      </Routes>
    </Router>
  );
}

export default App;