import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError, api } from '../api';
import { useAuth } from '../auth';
import ProfileForm from '../components/ProfileForm';
import type { Profile } from '../types';

const LOOKING = {
  ROOM_SHARE: 'A room in a shared flat',
  FLATMATE_FOR_MY_FLAT: 'A flatmate for my flat',
  BOTH: 'Room or flatmate',
} as const;

const SLEEP = {
  BEFORE_2200: 'Before 10pm',
  H2200_2300: '10–11pm',
  H2300_0000: '11pm–12am',
  AFTER_MIDNIGHT: 'After midnight',
} as const;

const WAKE = {
  BEFORE_0600: 'Before 6am',
  H0600_0800: '6–8am',
  H0800_1000: '8–10am',
  AFTER_1000: 'After 10am',
} as const;

const MOVE_IN = {
  IMMEDIATE: 'Immediately',
  TWO_WEEKS: 'Within 2 weeks',
  ONE_MONTH: 'Within 1 month',
  TWO_MONTHS: 'Within 2 months',
  EXPLORING: 'Just exploring',
} as const;

const FOOD = { VEG: 'Veg only', EGG: 'Eggetarian', NONVEG: 'Non-veg', VEGAN: 'Vegan' } as const;

const GUESTS = {
  RARELY: 'Rarely',
  SOMETIMES: 'Sometimes',
  OFTEN: 'Often',
  VERY_OFTEN: 'Very often',
} as const;

const SOCIAL = { QUIET: 'Quiet & private', BALANCED: 'Balanced', SOCIAL: 'Social & outgoing' } as const;

const TIDY = ['Very relaxed', 'Relaxed', 'Average', 'Tidy', 'Spotless'];

function initials(name: string | null | undefined) {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]!.toUpperCase())
    .join('');
}

function Group({ title, rows }: { title: string; rows: Array<[string, string]> }) {
  return (
    <section className="card">
      <div className="section-title" style={{ marginBottom: 8 }}>
        {title}
      </div>
      <dl className="detail-list">
        {rows.map(([label, value]) => (
          <div className="detail-item" key={label}>
            <dt>{label}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

export default function ProfilePage() {
  const { user, reload } = useAuth();
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);

  const profileQuery = useQuery({
    queryKey: ['profile'],
    queryFn: async () => {
      try {
        return await api<Profile>('/api/profile');
      } catch (err) {
        if (err instanceof ApiError && err.status === 404) return null;
        throw err;
      }
    },
  });

  const profile = profileQuery.data ?? null;

  useEffect(() => {
    if (profileQuery.isSuccess && !profile) setEditing(true);
  }, [profileQuery.isSuccess, profile]);

  function onSaved(saved: Profile) {
    queryClient.setQueryData(['profile'], saved);
    setEditing(false);
    void reload();
    void queryClient.invalidateQueries({ queryKey: ['matches'] });
  }

  const personal: Array<[string, string]> = profile
    ? [
        ['Age', profile.age ? String(profile.age) : '—'],
        ['Education', profile.education || '—'],
        ['Birthplace', profile.birthplace || '—'],
        ['Occupation', profile.occupation || '—'],
      ]
    : [];

  const living: Array<[string, string]> = profile
    ? [
        ['City', profile.cityName || '—'],
        ['Area', profile.areaName || '—'],
        ['Budget', profile.budgetLabel],
        ['Move-in', MOVE_IN[profile.moveIn] ?? profile.moveIn],
        ['Looking for', LOOKING[profile.lookingFor] ?? profile.lookingFor],
        ['Flatmate preference', profile.flatmateGenderPref.replace(/_/g, ' ').toLowerCase()],
      ]
    : [];

  const habits: Array<[string, string]> = profile
    ? [
        ['Sleep', SLEEP[profile.sleepTime] ?? profile.sleepTime],
        ['Wake', WAKE[profile.wakeTime] ?? profile.wakeTime],
        ['Tidiness', TIDY[profile.cleanliness - 1] ?? String(profile.cleanliness)],
        ['Food', FOOD[profile.food] ?? profile.food],
        ['Guests', GUESTS[profile.guests] ?? profile.guests],
        ['Social style', SOCIAL[profile.social] ?? profile.social],
      ]
    : [];

  return (
    <>
      <header className="page-head">
        <div>
          <h1>{profile?.complete ? 'Your dashboard' : 'Set up your profile'}</h1>
          <p className="sub">
            {profile?.complete
              ? 'Everything here feeds the match score, except your name and email.'
              : 'Answer these once — you will not be asked again.'}
          </p>
        </div>
        <div className="row">
          <Link to="/safety" className="btn btn-ghost btn-sm">
            Safety
          </Link>
          {profile?.complete && !editing && (
            <button className="btn" onClick={() => setEditing(true)}>
              Edit details
            </button>
          )}
        </div>
      </header>

      {profile && (
        <div className="stack" style={{ marginBottom: 20 }}>
          <div className="profile-hero rise">
            <span className="avatar">{initials(profile.displayName ?? user?.name)}</span>
            <div className="grow">
              <div className="row" style={{ gap: 9, flexWrap: 'wrap' }}>
                <h2>{profile.displayName ?? user?.name ?? 'You'}</h2>
                {profile.complete ? (
                  <span className="chip chip-good">Profile complete</span>
                ) : (
                  <span className="chip chip-warn">Incomplete</span>
                )}
              </div>
              <p className="dim" style={{ marginTop: 3 }}>
                {[profile.occupation, profile.education, [profile.areaName, profile.cityName].filter(Boolean).join(', ')]
                  .filter(Boolean)
                  .join(' · ') || user?.email}
              </p>
              <p className="muted" style={{ marginTop: 2 }}>
                {user?.email}
              </p>
            </div>
          </div>

          {!editing && (
            <>
              <Group title="Personal" rows={personal} />
              <Group title="Location & budget" rows={living} />
              <Group title="Daily habits" rows={habits} />

              {profile.aboutMe && (
                <div className="card">
                  <div className="section-title" style={{ marginBottom: 7 }}>
                    About
                  </div>
                  <p className="dim">{profile.aboutMe}</p>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {(editing || !profile) && (
        <ProfileForm
          initial={profile}
          submitLabel={profile ? 'Save changes' : 'Save and start matching'}
          onSaved={onSaved}
        />
      )}
    </>
  );
}
