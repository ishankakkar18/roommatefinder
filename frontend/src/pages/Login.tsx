import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiError, post } from '../api';
import { useAuth } from '../auth';
import type { AuthResult, Gender } from '../types';

export default function Login() {
  const { signIn } = useAuth();
  const navigate = useNavigate();

  const [mode, setMode] = useState<'signin' | 'signup'>('signin');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [gender, setGender] = useState<Gender>('UNDISCLOSED');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const result =
        mode === 'signin'
          ? await post<AuthResult>('/api/auth/login', { email: email.trim(), password })
          : await post<AuthResult>('/api/auth/register', {
              email: email.trim(),
              password,
              name: name.trim(),
              gender,
            });

      await signIn(result.accessToken, result.refreshToken);
      navigate(result.profileComplete ? '/matches' : '/profile', { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        const field = err.fieldErrors ? Object.values(err.fieldErrors)[0] : null;
        setError(field ?? err.message);
      } else {
        setError('Cannot reach the server. Is the API running?');
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth">
      <section className="auth-art">
        <div className="brand">
          <span className="brand-mark" />
          <span>flatmate</span>
        </div>

        <div>
          <h1 className="auth-headline">
            Find someone
            <br />
            you can actually
            <br />
            <em>live with.</em>
          </h1>
          <p className="dim" style={{ marginTop: 18, maxWidth: 380 }}>
            A compatibility score built from how you actually live — sleep, tidiness,
            guests, food — with the reasons shown, not hidden.
          </p>

          <div className="auth-points">
            <div className="auth-point">
              <span className="dotmark">1</span>
              <span>13 multiple-choice questions. No essays.</span>
            </div>
            <div className="auth-point">
              <span className="dotmark">2</span>
              <span>Every match explains itself, strengths and frictions.</span>
            </div>
            <div className="auth-point">
              <span className="dotmark">3</span>
              <span>Chat opens only after you both say yes.</span>
            </div>
          </div>
        </div>

        <p className="muted">Chandigarh · Ludhiana · Amritsar · Jalandhar · Delhi · Mumbai · Pune · Hyderabad</p>
      </section>

      <section className="auth-form-wrap">
        <form className="auth-card rise" onSubmit={submit}>
          <div className="seg">
            <button
              type="button"
              className={mode === 'signin' ? 'active' : ''}
              onClick={() => {
                setMode('signin');
                setError(null);
              }}
            >
              Sign in
            </button>
            <button
              type="button"
              className={mode === 'signup' ? 'active' : ''}
              onClick={() => {
                setMode('signup');
                setError(null);
              }}
            >
              Create account
            </button>
          </div>

          <div>
            <h2>{mode === 'signin' ? 'Welcome back' : 'Create your account'}</h2>
            <p className="muted" style={{ marginTop: 4 }}>
              {mode === 'signin'
                ? 'Pick up where you left off.'
                : 'Two minutes to set up. No phone number needed.'}
            </p>
          </div>

          {mode === 'signup' && (
            <div className="row" style={{ gap: 10 }}>
              <label className="field grow">
                <span>Name</span>
                <input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Asha"
                  autoComplete="name"
                  required
                />
              </label>
              <label className="field" style={{ width: 132 }}>
                <span>Gender</span>
                <select value={gender} onChange={(e) => setGender(e.target.value as Gender)}>
                  <option value="UNDISCLOSED">Skip</option>
                  <option value="FEMALE">Female</option>
                  <option value="MALE">Male</option>
                  <option value="OTHER">Other</option>
                </select>
              </label>
            </div>
          )}

          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
              required
            />
          </label>

          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={mode === 'signup' ? 'At least 8 characters' : '••••••••'}
              autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
              minLength={mode === 'signup' ? 8 : undefined}
              required
            />
          </label>

          {error && <div className="alert">{error}</div>}

          <button
            className="btn btn-primary btn-block"
            disabled={busy || !email || !password || (mode === 'signup' && name.trim().length < 2)}
          >
            {busy ? 'Please wait…' : mode === 'signin' ? 'Sign in' : 'Create account'}
          </button>

          <p className="muted" style={{ textAlign: 'center' }}>
            {mode === 'signin' ? 'New here? ' : 'Already have an account? '}
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() => {
                setMode(mode === 'signin' ? 'signup' : 'signin');
                setError(null);
              }}
            >
              {mode === 'signin' ? 'Create an account' : 'Sign in'}
            </button>
          </p>
        </form>
      </section>
    </div>
  );
}
