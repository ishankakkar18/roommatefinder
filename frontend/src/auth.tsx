import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { api, getTokens, setTokens } from './api';
import type { Me } from './types';

interface AuthState {
  user: Me | null;
  loading: boolean;
  signIn: (accessToken: string, refreshToken: string) => Promise<void>;
  signOut: () => Promise<void>;
  reload: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    if (!getTokens()) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      setUser(await api<Me>('/api/auth/me'));
    } catch {
      setTokens(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  const signIn = useCallback(
    async (accessToken: string, refreshToken: string) => {
      setTokens({ accessToken, refreshToken });
      setLoading(true);
      await reload();
    },
    [reload],
  );

  const signOut = useCallback(async () => {
    const current = getTokens();
    if (current?.refreshToken) {
      try {
        await api('/api/auth/logout', {
          method: 'POST',
          body: JSON.stringify({ refreshToken: current.refreshToken }),
        });
      } catch {
        // logging out locally is enough
      }
    }
    setTokens(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, signIn, signOut, reload }),
    [user, loading, signIn, signOut, reload],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
