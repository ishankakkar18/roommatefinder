// Seeds demo accounts + profiles. Safe to re-run: existing emails just log in.
const BASE = process.env.BASE ?? 'http://localhost:8080';

async function call(path, { method = 'GET', token, body } = {}) {
  const res = await fetch(BASE + path, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  return { status: res.status, data: text ? JSON.parse(text) : null };
}

async function tokenFor(user) {
  const reg = await call('/api/auth/register', {
    method: 'POST',
    body: {
      email: user.email,
      password: user.password,
      name: user.name,
      gender: user.gender,
    },
  });
  if (reg.status === 201) return reg.data.accessToken;

  const login = await call('/api/auth/login', {
    method: 'POST',
    body: { email: user.email, password: user.password },
  });
  if (login.status !== 200) throw new Error(`cannot auth ${user.email}: ${login.status}`);
  return login.data.accessToken;
}

const citiesRes = await call('/api/reference/cities');
const cityByName = new Map(citiesRes.data.map((c) => [c.name, c.id]));

const areaCache = new Map();
async function resolveArea(cityName, areaName) {
  const cityId = cityByName.get(cityName);
  if (!areaCache.has(cityId)) {
    const res = await call(`/api/reference/areas?cityId=${cityId}`);
    areaCache.set(cityId, res.data);
  }
  const area = areaCache.get(cityId).find((a) => a.name === areaName);
  if (!area) throw new Error(`no area ${areaName} in ${cityName}`);
  return { cityId, areaId: area.id };
}

const people = [
  {
    email: 'asha@example.com', password: 'secret123', name: 'Asha', gender: 'FEMALE',
    city: 'Chandigarh', area: 'Mohali', age: 21, education: 'B.Tech CSE, 3rd year',
    birthplace: 'Patiala', occupation: 'Student', budget: 2, sleep: 'H2200_2300',
    wake: 'H0600_0800', clean: 4, guests: 'SOMETIMES', social: 'BALANCED', food: 'NONVEG',
    cooking: 'SOMETIMES', about: 'Out most of the day, tidy kitchen, looking for a calm flat.',
  },
  {
    email: 'bilal@example.com', password: 'secret123', name: 'Bilal', gender: 'MALE',
    city: 'Chandigarh', area: 'Sector 17', age: 23, education: 'MCA, final year',
    birthplace: 'Ludhiana', occupation: 'Student', budget: 2, sleep: 'H2300_0000',
    wake: 'H0600_0800', clean: 4, guests: 'SOMETIMES', social: 'BALANCED', food: 'NONVEG',
    cooking: 'SOMETIMES', about: 'Night owl, keeps to myself, clean and quiet.',
  },
  {
    email: 'esha@example.com', password: 'secret123', name: 'Esha', gender: 'FEMALE',
    city: 'Chandigarh', area: 'Sector 22', age: 22, education: 'B.Des, 4th year',
    birthplace: 'Amritsar', occupation: 'Design intern', budget: 3, sleep: 'H2200_2300',
    wake: 'H0600_0800', clean: 5, guests: 'RARELY', social: 'QUIET', food: 'VEG',
    cooking: 'DAILY', about: 'Very tidy, vegetarian kitchen, early riser.',
  },
  {
    email: 'farhan@example.com', password: 'secret123', name: 'Farhan', gender: 'MALE',
    city: 'Chandigarh', area: 'Sector 35', age: 25, education: 'MBA',
    birthplace: 'Delhi', occupation: 'Product analyst', budget: 3, sleep: 'H2200_2300',
    wake: 'H0800_1000', clean: 3, guests: 'OFTEN', social: 'SOCIAL', food: 'NONVEG',
    cooking: 'RARELY', about: 'Social, often have friends over on weekends.',
  },
  {
    email: 'gita@example.com', password: 'secret123', name: 'Gita', gender: 'FEMALE',
    city: 'Chandigarh', area: 'Panchkula', age: 24, education: 'B.Com',
    birthplace: 'Jalandhar', occupation: 'Accountant', budget: 4, sleep: 'BEFORE_2200',
    wake: 'BEFORE_0600', clean: 5, guests: 'RARELY', social: 'QUIET', food: 'VEG',
    cooking: 'DAILY', about: 'Early to bed, early to rise. Prefer a peaceful flat.',
  },
  {
    email: 'chetan@example.com', password: 'secret123', name: 'Chetan', gender: 'MALE',
    city: 'Ludhiana', area: 'Model Town', age: 26, education: 'B.Tech ECE',
    birthplace: 'Ludhiana', occupation: 'Software engineer', budget: 2, sleep: 'H2300_0000',
    wake: 'H0800_1000', clean: 3, guests: 'SOMETIMES', social: 'BALANCED', food: 'NONVEG',
    cooking: 'RARELY', about: 'Work from home 3 days a week, need quiet hours.',
  },
];

for (const person of people) {
  const token = await tokenFor(person);
  const { cityId, areaId } = await resolveArea(person.city, person.area);

  const res = await call('/api/profile', {
    method: 'PUT',
    token,
    body: {
      lookingFor: 'BOTH',
      cityId,
      areaId,
      budgetBand: person.budget,
      moveIn: 'TWO_WEEKS',
      flatmateGenderPref: 'NO_PREF',
      sleepTime: person.sleep,
      wakeTime: person.wake,
      cleanliness: person.clean,
      guests: person.guests,
      social: person.social,
      food: person.food,
      cooking: person.cooking,
      workFromHome: 'NEVER',
      aboutMe: person.about,
      education: person.education,
      age: person.age,
      birthplace: person.birthplace,
      occupation: person.occupation,
      smoke: 'I_DONT',
      smokeTolerance: 'DONT_MIND',
      drink: 'SOCIALLY',
      drinkTolerance: 'DONT_MIND',
      pets: 'NONE',
      petTolerance: 'DONT_MIND',
    },
  });

  console.log(`${person.name.padEnd(8)} ${person.city}, ${person.area} -> ${res.status} ${res.data?.complete ? 'complete' : 'incomplete'}`);
}

const asha = await tokenFor({ email: 'asha@example.com', password: 'secret123', name: 'Asha', gender: 'FEMALE' });
const matches = await call('/api/matches', { token: asha });
console.log('\nAsha sees:');
for (const m of matches.data) {
  console.log(`  ${String(m.score).padStart(3)}  ${m.displayName}  ${m.areaName}  ${m.reasons.join(', ')}`);
}
console.log(`  (${matches.data.length} matches)`);
