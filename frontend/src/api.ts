import type { Tokens } from './types';

const STORAGE_KEY = 'flatmate.tokens';
const API_KEY = 'flatmate.apiBase';

/**
 * Where the API lives, resolved at runtime so one build works everywhere:
 *   1. ?api=https://xyz.trycloudflare.com   (remembered, for deployed demos)
 *   2. ?api=local                            (clears the override)
 *   3. a previously remembered override
 *   4. VITE_API_BASE baked at build time
 *   5. http://<current host>:8080            (laptop / LAN testing)
 */
function resolveApiBase(): string {
  if (typeof window === 'undefined') return 'http://localhost:8080';

  const fromQuery = new URLSearchParams(window.location.search).get('api');
  if (fromQuery === 'local') {
    localStorage.removeItem(API_KEY);
  } else if (fromQuery) {
    localStorage.setItem(API_KEY, fromQuery);
    return fromQuery.replace(/\/$/, '');
  }

  const stored = localStorage.getItem(API_KEY);
  if (stored) return stored;

  const fromEnv = import.meta.env.VITE_API_BASE;
  if (fromEnv) return String(fromEnv).replace(/\/$/, '');

  // Served from the Vite dev server -> talk to the API on :8080.
  // Served from the API itself (or a tunnel in front of it) -> same origin,
  // which keeps https/wss and the host correct automatically.
  const devPort = window.location.port;
  if (devPort === '5173' || devPort === '3000') {
    return `http://${window.location.hostname}:8080`;
  }
  return window.location.origin;
}

export const API_BASE = resolveApiBase();

/** http -> ws, https -> wss. Mixed content blocks the other direction. */
export const WS_URL = `${API_BASE.replace(/^http/, 'ws')}/ws`;

let tokens: Tokens | null = readTokens();

function readTokens(): Tokens | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Tokens) : null;
  } catch {
    return null;
  }
}

export function setTokens(next: Tokens | null) {
  tokens = next;
  if (next) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
}

export function getTokens(): Tokens | null {
  return tokens;
}

export function getAccessToken(): string | null {
  return tokens?.accessToken ?? null;
}

export class ApiError extends Error {
  status: number;
  code: string;
  fieldErrors?: Record<string, string>;

  constructor(status: number, code: string, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

async function tryRefresh(): Promise<boolean> {
  if (!tokens?.refreshToken) return false;
  const res = await fetch(`${API_BASE}/api/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken: tokens.refreshToken }),
  });
  if (!res.ok) {
    setTokens(null);
    return false;
  }
  const data = await res.json();
  setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken });
  return true;
}

/**
 * One request wrapper for every call.
 * 401 -> refresh once -> replay. Everything else surfaces as ApiError.
 */
export async function api<T>(path: string, init: RequestInit = {}, replay = true): Promise<T> {
  const headers: Record<string, string> = { ...(init.headers as Record<string, string>) };
  if (init.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  if (tokens) {
    headers.Authorization = `Bearer ${tokens.accessToken}`;
  }

  const res = await fetch(API_BASE + path, { ...init, headers });

  if (res.status === 401 && replay && (await tryRefresh())) {
    return api<T>(path, init, false);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new ApiError(
      res.status,
      data?.code ?? 'REQUEST_FAILED',
      data?.message ?? `Request failed (${res.status})`,
      data?.fieldErrors,
    );
  }
  return data as T;
}

export const post = <T,>(path: string, body?: unknown) =>
  api<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) });

export const put = <T,>(path: string, body: unknown) =>
  api<T>(path, { method: 'PUT', body: JSON.stringify(body) });
