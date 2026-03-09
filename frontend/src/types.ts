export type AuthMode = 'login' | 'register';
export type PendingAction = 'save' | 'reflection' | null;

export interface Note {
  id: number;
  content: string;
  date: string;
  category: string;
  subCategory?: string;
}

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  authority: string;
}

export interface AuthResponse {
  message: string;
  user: AuthUser;
}

export interface AiInsight {
  text: string;
  loading: boolean;
}

export interface SimilarThoughtsResponse {
  categoryMessage: string;
  notes: Note[];
  inputAccepted: boolean;
  validationMessage?: string;
}

