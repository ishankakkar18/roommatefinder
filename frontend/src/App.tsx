import { Navigate, Route, Routes } from 'react-router-dom';
import type { ReactElement } from 'react';
import { useAuth } from './auth';
import AppShell from './components/AppShell';
import Login from './pages/Login';
import Matches from './pages/Matches';
import Requests from './pages/Requests';
import Chats from './pages/Chats';
import Safety from './pages/Safety';
import ProfilePage from './pages/Profile';
import Chat from './pages/Chat';

function Protected({ children }: { children: ReactElement }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="center-screen">
        <p className="muted">Loading…</p>
      </div>
    );
  }
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route
        element={
          <Protected>
            <AppShell />
          </Protected>
        }
      >
        <Route path="/" element={<Navigate to="/matches" replace />} />
        <Route path="/matches" element={<Matches />} />
        <Route path="/requests" element={<Requests />} />
        <Route path="/chats" element={<Chats />} />
        <Route path="/safety" element={<Safety />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/chat/:id" element={<Chat />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
