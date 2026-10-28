import { Role } from './role.model';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  username: string;
  roles: string[];
}

export interface AuthenticatedUserResponse {
  username: string;
  roles: string[];
}

export interface AuthenticatedUser {
  username: string;
  roles: Role[];
}

export interface StoredSession {
  token: string;
  expiresAt: number;
  user: AuthenticatedUser;
}
