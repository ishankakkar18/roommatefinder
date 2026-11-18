import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ApiError, api, put } from '../api';
import type { Area, City, Profile } from '../types';

const BUDGETS = ['₹5-8k', '₹8-12k', '₹12-15k', '₹15-20k', '₹20-25k', '₹25-30k', '₹30-40k', '₹40k+'];
const TIDY = ['Very relaxed', 'Relaxed', 'Average', 'Tidy', 'Spotless'];

const LOOKING_FOR = [
  ['ROOM_SHARE', 'A room in a shared flat'],
  ['FLATMATE_FOR_MY_FLAT', 'A flatmate for my flat'],
  ['BOTH', 'Both'],
] as const;
const MOVE_IN = [
  ['IMMEDIATE', 'Immediately'],
  ['TWO_WEEKS', 'Within 2 weeks'],
  ['ONE_MONTH', 'Within 1 month'],
  ['TWO_MONTHS', 'Within 2 months'],
  ['EXPLORING', 'Just exploring'],
] as const;
const GENDER_PREF = [
  ['NO_PREF', 'No preference'],
  ['SAME_AS_MINE', 'Same as mine'],
  ['MALE_ONLY', 'Male only'],
  ['FEMALE_ONLY', 'Female only'],
] as const;
const SLEEP = [
  ['BEFORE_2200', 'Before 10pm'],
  ['H2200_2300', '10–11pm'],
  ['H2300_0000', '11pm–12am'],
  ['AFTER_MIDNIGHT', 'After midnight'],
] as const;
const WAKE = [
  ['BEFORE_0600', 'Before 6am'],
  ['H0600_0800', '6–8am'],
  ['H0800_1000', '8–10am'],
  ['AFTER_1000', 'After 10am'],
] as const;
const GUESTS = [
  ['RARELY', 'Rarely'],
  ['SOMETIMES', 'Sometimes'],
  ['OFTEN', 'Often'],
  ['VERY_OFTEN', 'Very often'],
] as const;
const SOCIAL = [
  ['QUIET', 'Quiet & private'],
  ['BALANCED', 'Balanced'],
  ['SOCIAL', 'Social & outgoing'],
] as const;
const FOOD = [
  ['VEG', 'Veg only'],
  ['EGG', 'Eggetarian'],
  ['NONVEG', 'Non-veg'],
  ['VEGAN', 'Vegan'],
] as const;
const COOKING = [
  ['NEVER', 'Never'],
  ['RARELY', 'Rarely'],
  ['SOMETIMES', 'Sometimes'],
  ['DAILY', 'Daily'],
] as const;
const WFH = [
  ['NEVER', 'Never'],
  ['ONE_TWO_DAYS', '1–2 days'],
  ['THREE_FOUR_DAYS', '3–4 days'],
  ['ALWAYS', 'Always'],
] as const;
const SMOKE = [
  ['I_DONT', "I don't smoke"],
  ['I_DO', 'I smoke'],
  ['QUIT', 'I quit'],
] as const;
const SMOKE_TOL = [
  ['DONT_MIND', "Don't mind"],
  ['OUTDOORS_ONLY', 'Only outdoors'],
  ['CANT', "Can't live with a smoker"],
] as const;
const DRINK = [
  ['NEVER', 'Never'],
  ['SOCIALLY', 'Socially'],
  ['REGULARLY', 'Regularly'],
] as const;
const DRINK_TOL = [
  ['DONT_MIND', "Don't mind"],
  ['RATHER_NOT', 'Rather not'],
] as const;
const PETS = [
  ['NONE', 'No pets'],
  ['HAVE', 'I have pets'],
  ['WANT', 'Want pets'],
] as const;
const PET_TOL = [
  ['DONT_MIND', "Don't mind pets"],
  ['NO_PETS', 'No pets please'],
] as const;

function fromInitial(initial?: Profile | null): Record<string, string> {
  return {
    lookingFor: initial?.lookingFor ?? 'BOTH',
    cityId: initial ? String(initial.cityId) : '',
    areaId: initial ? String(initial.areaId) : '',
    budgetBand: initial ? String(initial.budgetBand) : '2',
    moveIn: initial?.moveIn ?? 'TWO_WEEKS',
    flatmateGenderPref: initial?.flatmateGenderPref ?? 'NO_PREF',
    sleepTime: initial?.sleepTime ?? 'H2200_2300',
    wakeTime: initial?.wakeTime ?? 'H0600_0800',
    cleanliness: initial ? String(initial.cleanliness) : '4',
    guests: initial?.guests ?? 'SOMETIMES',
    social: initial?.social ?? 'BALANCED',
    food: initial?.food ?? 'NONVEG',
    cooking: initial?.cooking ?? 'SOMETIMES',
    workFromHome: initial?.workFromHome ?? 'NEVER',
    aboutMe: initial?.aboutMe ?? '',
    education: initial?.education ?? '',
    age: initial?.age ? String(initial.age) : '',
    birthplace: initial?.birthplace ?? '',
    occupation: initial?.occupation ?? '',
    smoke: initial?.smoke ?? 'I_DONT',
    smokeTolerance: initial?.smokeTolerance ?? 'DONT_MIND',
    drink: initial?.drink ?? 'NEVER',
    drinkTolerance: initial?.drinkTolerance ?? 'DONT_MIND',
    pets: initial?.pets ?? 'NONE',
    petTolerance: initial?.petTolerance ?? 'DONT_MIND',
  };
}

interface Props {
  initial?: Profile | null;
  submitLabel: string;
  onSaved: (profile: Profile) => void;
}

export default function ProfileForm({ initial, submitLabel, onSaved }: Props) {
  const [form, setForm] = useState<Record<string, string>>(() => fromInitial(initial));
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    setForm(fromInitial(initial));
  }, [initial?.userId]);

  const cities = useQuery({
    queryKey: ['cities'],
    queryFn: () => api<City[]>('/api/reference/cities'),
  });

  useEffect(() => {
    if (!form.cityId && cities.data?.length) {
      setForm((f) => ({ ...f, cityId: String(cities.data![0].id) }));
    }
  }, [cities.data, form.cityId]);

  const areas = useQuery({
    queryKey: ['areas', form.cityId],
    queryFn: () => api<Area[]>(`/api/reference/areas?cityId=${form.cityId}`),
    enabled: Boolean(form.cityId),
  });

  useEffect(() => {
    if (areas.data?.length && !areas.data.some((a) => String(a.id) === form.areaId)) {
      setForm((f) => ({ ...f, areaId: String(areas.data![0].id) }));
    }
  }, [areas.data, form.areaId]);

  const set = (key: string) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const saved = await put<Profile>('/api/profile', {
        lookingFor: form.lookingFor,
        cityId: Number(form.cityId),
        areaId: Number(form.areaId),
        budgetBand: Number(form.budgetBand),
        moveIn: form.moveIn,
        flatmateGenderPref: form.flatmateGenderPref,
        sleepTime: form.sleepTime,
        wakeTime: form.wakeTime,
        cleanliness: Number(form.cleanliness),
        guests: form.guests,
        social: form.social,
        food: form.food,
        cooking: form.cooking,
        workFromHome: form.workFromHome,
        aboutMe: form.aboutMe.trim() || undefined,
        education: form.education.trim() || undefined,
        age: form.age ? Number(form.age) : undefined,
        birthplace: form.birthplace.trim() || undefined,
        occupation: form.occupation.trim() || undefined,
        smoke: form.smoke,
        smokeTolerance: form.smokeTolerance,
        drink: form.drink,
        drinkTolerance: form.drinkTolerance,
        pets: form.pets,
        petTolerance: form.petTolerance,
      });
      onSaved(saved);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not save your profile');
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="stack" onSubmit={submit}>
      <section className="card stack">
        <div className="section-title">About you</div>
        <div className="form-grid">
          <label className="field">
            <span>Education</span>
            <input
              value={form.education}
              onChange={set('education')}
              maxLength={80}
              placeholder="B.Tech CSE, 3rd year"
            />
          </label>
          <label className="field">
            <span>Age</span>
            <input
              value={form.age}
              onChange={(e) =>
                setForm((f) => ({ ...f, age: e.target.value.replace(/\D/g, '').slice(0, 2) }))
              }
              inputMode="numeric"
              placeholder="21"
            />
          </label>
          <label className="field">
            <span>Birthplace</span>
            <input
              value={form.birthplace}
              onChange={set('birthplace')}
              maxLength={80}
              placeholder="Patiala"
            />
          </label>
          <label className="field">
            <span>Occupation</span>
            <input
              value={form.occupation}
              onChange={set('occupation')}
              maxLength={80}
              placeholder="Student / Product designer"
            />
          </label>
        </div>
        <label className="field">
          <span>About you ({form.aboutMe.length}/200)</span>
          <textarea
            rows={3}
            maxLength={200}
            value={form.aboutMe}
            onChange={set('aboutMe')}
            placeholder="Out most of the day, tidy kitchen, non-smoker."
          />
        </label>
      </section>

      <section className="card stack">
        <div className="section-title">Where and what</div>
        <div className="form-grid">
          <label className="field">
            <span>I'm looking for</span>
            <select value={form.lookingFor} onChange={set('lookingFor')}>
              {LOOKING_FOR.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Monthly budget</span>
            <select value={form.budgetBand} onChange={set('budgetBand')}>
              {BUDGETS.map((label, i) => (
                <option key={label} value={i}>{label}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>City</span>
            <select value={form.cityId} onChange={set('cityId')}>
              {cities.data?.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Area</span>
            <select value={form.areaId} onChange={set('areaId')}>
              {areas.data?.map((a) => (
                <option key={a.id} value={a.id}>{a.name}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Move-in</span>
            <select value={form.moveIn} onChange={set('moveIn')}>
              {MOVE_IN.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Flatmate gender preference</span>
            <select value={form.flatmateGenderPref} onChange={set('flatmateGenderPref')}>
              {GENDER_PREF.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
        </div>
      </section>

      <section className="card stack">
        <div className="section-title">Daily habits</div>
        <div className="form-grid">
          <label className="field">
            <span>Usual sleep time</span>
            <select value={form.sleepTime} onChange={set('sleepTime')}>
              {SLEEP.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Usual wake time</span>
            <select value={form.wakeTime} onChange={set('wakeTime')}>
              {WAKE.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>How tidy are you? ({TIDY[Number(form.cleanliness) - 1]})</span>
            <input type="range" min={1} max={5} value={form.cleanliness} onChange={set('cleanliness')} />
          </label>
          <label className="field">
            <span>How often do you have guests</span>
            <select value={form.guests} onChange={set('guests')}>
              {GUESTS.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Social style</span>
            <select value={form.social} onChange={set('social')}>
              {SOCIAL.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Food</span>
            <select value={form.food} onChange={set('food')}>
              {FOOD.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Cooking at home</span>
            <select value={form.cooking} onChange={set('cooking')}>
              {COOKING.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Work from home</span>
            <select value={form.workFromHome} onChange={set('workFromHome')}>
              {WFH.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
        </div>
      </section>

      <section className="card stack">
        <div className="section-title">Tolerance</div>
        <div className="form-grid">
          <label className="field">
            <span>Smoking</span>
            <select value={form.smoke} onChange={set('smoke')}>
              {SMOKE.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>A flatmate who smokes</span>
            <select value={form.smokeTolerance} onChange={set('smokeTolerance')}>
              {SMOKE_TOL.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Drinking</span>
            <select value={form.drink} onChange={set('drink')}>
              {DRINK.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>A flatmate who drinks</span>
            <select value={form.drinkTolerance} onChange={set('drinkTolerance')}>
              {DRINK_TOL.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Pets</span>
            <select value={form.pets} onChange={set('pets')}>
              {PETS.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>A flatmate with pets</span>
            <select value={form.petTolerance} onChange={set('petTolerance')}>
              {PET_TOL.map(([v, l]) => (
                <option key={v} value={v}>{l}</option>
              ))}
            </select>
          </label>
        </div>
      </section>

      {error && <div className="alert">{error}</div>}

      <button className="btn btn-primary btn-block" disabled={busy || !form.areaId}>
        {busy ? 'Saving…' : submitLabel}
      </button>
    </form>
  );
}
