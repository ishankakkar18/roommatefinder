export type LookingFor = 'ROOM_SHARE' | 'FLATMATE_FOR_MY_FLAT' | 'BOTH';
export type MoveIn = 'IMMEDIATE' | 'TWO_WEEKS' | 'ONE_MONTH' | 'TWO_MONTHS' | 'EXPLORING';
export type FlatmateGenderPref = 'SAME_AS_MINE' | 'NO_PREF' | 'MALE_ONLY' | 'FEMALE_ONLY';
export type SleepTime = 'BEFORE_2200' | 'H2200_2300' | 'H2300_0000' | 'AFTER_MIDNIGHT';
export type WakeTime = 'BEFORE_0600' | 'H0600_0800' | 'H0800_1000' | 'AFTER_1000';
export type Guests = 'RARELY' | 'SOMETIMES' | 'OFTEN' | 'VERY_OFTEN';
export type Social = 'QUIET' | 'BALANCED' | 'SOCIAL';
export type Food = 'VEG' | 'EGG' | 'NONVEG' | 'VEGAN';
export type Cooking = 'NEVER' | 'RARELY' | 'SOMETIMES' | 'DAILY';
export type WorkFromHome = 'NEVER' | 'ONE_TWO_DAYS' | 'THREE_FOUR_DAYS' | 'ALWAYS';
export type Smoke = 'I_DONT' | 'I_DO' | 'QUIT';
export type SmokeTolerance = 'CANT' | 'OUTDOORS_ONLY' | 'DONT_MIND';
export type Drink = 'NEVER' | 'SOCIALLY' | 'REGULARLY';
export type DrinkTolerance = 'DONT_MIND' | 'RATHER_NOT';
export type Pets = 'NONE' | 'HAVE' | 'WANT';
export type PetTolerance = 'DONT_MIND' | 'NO_PETS';
export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'UNDISCLOSED';
export type RequestStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'WITHDRAWN' | 'EXPIRED';

export interface Tokens {
  accessToken: string;
  refreshToken: string;
}

export interface Me {
  id: number;
  email: string;
  phone: string | null;
  name: string | null;
  gender: Gender | null;
  profileComplete: boolean;
}

export interface Profile {
  userId: number;
  displayName: string | null;
  lookingFor: LookingFor;
  cityId: number;
  cityName: string | null;
  areaId: number;
  areaName: string | null;
  budgetBand: number;
  budgetLabel: string;
  moveIn: MoveIn;
  flatmateGenderPref: FlatmateGenderPref;
  sleepTime: SleepTime;
  wakeTime: WakeTime;
  cleanliness: number;
  guests: Guests;
  social: Social;
  food: Food;
  cooking: Cooking;
  workFromHome: WorkFromHome;
  aboutMe: string | null;
  education: string | null;
  age: number | null;
  birthplace: string | null;
  occupation: string | null;
  smoke: Smoke;
  smokeTolerance: SmokeTolerance;
  drink: Drink;
  drinkTolerance: DrinkTolerance;
  pets: Pets;
  petTolerance: PetTolerance;
  complete: boolean;
}

export interface MatchCard {
  userId: number;
  displayName: string | null;
  score: number;
  areaId: number;
  areaName: string | null;
  age: number | null;
  budgetBand: number;
  budgetLabel: string;
  aboutMe: string | null;
  reasons: string[];
  frictions: string[];
}

export interface RequestRow {
  id: number;
  fromUserId: number;
  fromName: string | null;
  toUserId: number;
  toName: string | null;
  status: RequestStatus;
  score: number;
  direction: 'INCOMING' | 'OUTGOING';
  createdAt: string;
  expiresAt: string;
}

export interface AcceptResponse {
  requestId: number;
  status: RequestStatus;
  conversationId: string;
}

export interface Conversation {
  id: string;
  otherUserId: number;
  otherName: string | null;
  lastMessage: string | null;
  lastMessageAt: string | null;
  unread: number;
}

export interface Message {
  id: number;
  clientMsgId: string;
  conversationId: string;
  senderId: number;
  senderName: string | null;
  body: string;
  createdAt: string;
}

export interface City {
  id: number;
  name: string;
  slug: string;
}

export interface Area {
  id: number;
  name: string;
  slug: string;
  cityId: number;
}

export interface AuthResult {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  profileComplete: boolean;
}
