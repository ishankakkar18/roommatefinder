import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api';
import { useAuth } from '../auth';
import type { Conversation, RequestRow } from '../types';
import {
  IconChats,
  IconLogout,
  IconMatches,
  IconMoon,
  IconProfile,
  IconRequests,
  IconShield,
  IconSun,
} from './Icons';

function initials(name: string | null | undefined) {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]!.toUpperCase())
    .join('');
}

type Theme = 'dark' | 'light';

export default function AppShell() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const enabled = Boolean(user?.profileComplete);

  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem('flatmate.theme');
    return saved === 'light' ? 'light' : 'dark';
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('flatmate.theme', theme);
  }, [theme]);

  const incoming = useQuery({
    queryKey: ['requests', 'incoming'],
    queryFn: () => api<RequestRow[]>('/api/requests/incoming'),
    enabled,
  });

  const conversations = useQuery({
    queryKey: ['conversations'],
    queryFn: () => api<Conversation[]>('/api/conversations'),
    enabled,
  });

  const requestCount = incoming.data?.length ?? 0;
  const unread = conversations.data?.reduce((sum, c) => sum + c.unread, 0) ?? 0;

  async function handleSignOut() {
    await signOut();
    navigate('/login', { replace: true });
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <NavLink to="/matches" className="brand">
          <span className="brand-mark" />
          <span>flatmate</span>
        </NavLink>

        <nav className="nav">
          <NavLink to="/matches" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            <IconMatches />
            <span>Matches</span>
          </NavLink>
          <NavLink to="/requests" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            <IconRequests />
            <span>Requests</span>
            {requestCount > 0 && <em className="nav-badge">{requestCount}</em>}
          </NavLink>
          <NavLink to="/chats" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            <IconChats />
            <span>Chats</span>
            {unread > 0 && <em className="nav-badge">{unread}</em>}
          </NavLink>
          <NavLink to="/safety" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            <IconShield />
            <span>Safety</span>
          </NavLink>
          <NavLink to="/profile" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            <IconProfile />
            <span>Profile</span>
          </NavLink>
        </nav>

        <div className="sidebar-foot">
          <button
            type="button"
            className="user-chip"
            onClick={() => navigate('/profile')}
            title="Open your dashboard"
          >
            <span className="avatar">{initials(user?.name)}</span>
            <span className="who">
              <strong>{user?.name ?? 'You'}</strong>
              <span>{user?.email}</span>
            </span>
          </button>

          <button
            type="button"
            className="btn btn-ghost btn-sm theme-toggle"
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
          >
            {theme === 'dark' ? <IconSun /> : <IconMoon />}
            {theme === 'dark' ? 'Light mode' : 'Dark mode'}
          </button>

          <button className="btn btn-ghost btn-sm" onClick={() => void handleSignOut()}>
            <IconLogout style={{ width: 16, height: 16 }} />
            Sign out
          </button>
        </div>
      </aside>

      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
