export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  password?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  role: string;
  userId: number;
  name: string;
  email: string;
}
