export type AuthMode = 'login' | 'register';
export type PendingAction = 'save' | 'chat' | null;

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface Comment {
  id: number;
  text: string;
  username: string;
  createdAt: string;
  own: boolean;
}

export interface Note {
  id: number;
  content: string;
  date: string;
  category: string;
  subCategory?: string;
  commentCount?: number;
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
  ownNotes: Note[];
  inputAccepted: boolean;
  validationMessage?: string;
}

