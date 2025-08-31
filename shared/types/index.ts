// Shared types between frontend and backend

export interface Note {
  id: number;
  text: string;
  date: string;
  subject: string;
  user: User;
}

export interface User {
  userId: number;
  username: string;
  email: string;
  notes?: Note[];
}

export interface AiInsight {
  text: string;
  loading: boolean;
}

export interface NoteCluster {
  label: string;
  description: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  error?: string;
}
