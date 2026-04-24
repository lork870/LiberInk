import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import SidebarLayout from './components/layout/SidebarLayout';
import LibraryPage from './pages/LibraryPage';
import SettingsPage from './pages/SettingsPage';
import EditorPage from './pages/EditorPage';
import LandingPage from './pages/LandingPage';
import AuthPage from './pages/AuthPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/LandingPage" element={<LandingPage />} />
        <Route path="/login" element={<AuthPage />} />
        <Route path="/register" element={<AuthPage />} />

        {/* Всі сторінки тепер підпорядковуються SidebarLayout */}
        <Route element={<SidebarLayout />}>
          <Route path="/library" element={<LibraryPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/export" element={<div>Сторінка експорту</div>} />
          {/* ТЕПЕР РЕДАКТОР ТУТ */}
          <Route path="/editor/:id" element={<EditorPage />} />
        </Route>

        {/* Редирект за замовчуванням */}
        <Route path="*" element={<Navigate to="/LandingPage" />} />
      </Routes>
    </Router>
  );
}

export default App;