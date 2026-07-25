export interface ApiResponse<T> {
  data: T;
  message: string;
  code: number;
}

export interface UserProfileResponse {
  id?: number;
  email: string;
  phone?: string;
  birthDate?: string;
  avatarUrl?: string;
  bio?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  user?: UserProfileResponse;
}

export interface AccountLoginRequest {
  email?: string;
  password?: string;
  remember?: boolean;
}

export interface RegisterRequest {
  email?: string;
  password?: string;
  confirmPassword?: string;
}
