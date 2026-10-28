import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { TokenStorageService } from './token-storage.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  let storage: TokenStorageService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    storage = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('logs in, stores the token and confirms the user with /auth/me', async () => {
    const result = firstValueFrom(service.login({ username: 'admin', password: 'senha' }));

    const loginRequest = http.expectOne('/api/auth/login');
    expect(loginRequest.request.method).toBe('POST');
    loginRequest.flush({
      token: 'jwt-token',
      expiresIn: 3_600_000,
      username: 'admin',
      roles: ['ADMIN'],
    });

    const meRequest = http.expectOne('/api/auth/me');
    meRequest.flush({ username: 'admin', roles: ['ADMIN'] });

    expect(await result).toEqual({ username: 'admin', roles: ['ADMIN'] });
    expect(service.isAuthenticated()).toBeTrue();
    expect(storage.read()?.token).toBe('jwt-token');
  });

  it('restores a valid session and refreshes its user data', async () => {
    storage.save({
      token: 'stored-token',
      expiresAt: Date.now() + 60_000,
      user: { username: 'antigo', roles: ['FUNCIONARIO'] },
    });

    const initialization = service.initialize();
    const meRequest = http.expectOne('/api/auth/me');
    meRequest.flush({ username: 'atualizado', roles: ['CHEFE'] });
    await initialization;

    expect(service.user()).toEqual({ username: 'atualizado', roles: ['CHEFE'] });
  });

  it('discards an expired session without calling the backend', async () => {
    storage.save({
      token: 'expired-token',
      expiresAt: Date.now() - 1,
      user: { username: 'admin', roles: ['ADMIN'] },
    });

    await service.initialize();

    http.expectNone('/api/auth/me');
    expect(service.isAuthenticated()).toBeFalse();
    expect(storage.read()).toBeNull();
  });

  it('clears the session on logout', async () => {
    storage.save({
      token: 'jwt-token',
      expiresAt: Date.now() + 60_000,
      user: { username: 'admin', roles: ['ADMIN'] },
    });
    const initialization = service.initialize();
    http.expectOne('/api/auth/me').flush({ username: 'admin', roles: ['ADMIN'] });
    await initialization;
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(storage.read()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
