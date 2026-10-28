import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, map, Observable, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthenticatedUser,
  AuthenticatedUserResponse,
  LoginRequest,
  LoginResponse,
  StoredSession,
} from '../../shared/models/auth.model';
import { isRole, Role } from '../../shared/models/role.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storage = inject(TokenStorageService);
  private readonly currentSession = signal<StoredSession | null>(null);
  private expirationTimer: ReturnType<typeof setTimeout> | null = null;

  readonly user = computed(() => this.currentSession()?.user ?? null);
  readonly isAuthenticated = computed(() => this.hasValidSession());
  readonly primaryRole = computed(() => this.user()?.roles[0] ?? null);

  async initialize(): Promise<void> {
    const storedSession = this.storage.read();
    if (!this.isSessionValid(storedSession)) {
      this.clearSession();
      return;
    }

    this.currentSession.set(storedSession);
    this.scheduleExpiration(storedSession.expiresAt);
    try {
      await firstValueFrom(this.refreshCurrentUser());
    } catch {
      this.clearSession();
    }
  }

  login(credentials: LoginRequest): Observable<AuthenticatedUser> {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials)
      .pipe(
        tap((response) => {
          const session: StoredSession = {
            token: response.token,
            expiresAt: Date.now() + response.expiresIn,
            user: this.toUser(response.username, response.roles),
          };
          this.persistSession(session);
        }),
        switchMap(() => this.refreshCurrentUser()),
        catchError((error) => {
          this.clearSession();
          return throwError(() => error);
        }),
      );
  }

  refreshCurrentUser(): Observable<AuthenticatedUser> {
    return this.http
      .get<AuthenticatedUserResponse>(`${environment.apiBaseUrl}/auth/me`)
      .pipe(
        map((response) => this.toUser(response.username, response.roles)),
        tap((user) => {
          const session = this.currentSession();
          if (session) {
            this.persistSession({ ...session, user });
          }
        }),
      );
  }

  token(): string | null {
    return this.hasValidSession() ? this.currentSession()?.token ?? null : null;
  }

  hasRole(allowedRoles: readonly Role[]): boolean {
    return this.user()?.roles.some((role) => allowedRoles.includes(role)) ?? false;
  }

  defaultRoute(): string {
    return this.hasRole(['ADMIN', 'CHEFE']) ? '/dashboard' : '/students';
  }

  expireSession(): void {
    this.clearSession();
    void this.router.navigate(['/login'], { queryParams: { reason: 'session-expired' } });
  }

  logout(): void {
    this.clearSession();
    void this.router.navigate(['/login']);
  }

  private hasValidSession(): boolean {
    return this.isSessionValid(this.currentSession());
  }

  private isSessionValid(session: StoredSession | null): session is StoredSession {
    return Boolean(session?.token && session.expiresAt > Date.now());
  }

  private toUser(username: string, receivedRoles: string[]): AuthenticatedUser {
    return {
      username,
      roles: receivedRoles.filter(isRole),
    };
  }

  private persistSession(session: StoredSession): void {
    this.currentSession.set(session);
    this.storage.save(session);
    this.scheduleExpiration(session.expiresAt);
  }

  private clearSession(): void {
    if (this.expirationTimer) {
      clearTimeout(this.expirationTimer);
      this.expirationTimer = null;
    }
    this.currentSession.set(null);
    this.storage.clear();
  }

  private scheduleExpiration(expiresAt: number): void {
    if (this.expirationTimer) {
      clearTimeout(this.expirationTimer);
    }
    const maximumTimeout = 2_147_483_647;
    const delay = Math.min(Math.max(expiresAt - Date.now(), 0), maximumTimeout);
    this.expirationTimer = setTimeout(() => this.expireSession(), delay);
  }
}
