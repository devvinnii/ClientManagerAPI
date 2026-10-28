import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { authGuard } from './auth.guard';
import { roleGuard } from './role.guard';

describe('route guards', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['isAuthenticated', 'hasRole']);
    router = jasmine.createSpyObj<Router>('Router', ['createUrlTree', 'parseUrl']);
    router.createUrlTree.and.returnValue({} as UrlTree);
    router.parseUrl.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('allows an authenticated user into the administrative shell', () => {
    auth.isAuthenticated.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/students' } as RouterStateSnapshot),
    );

    expect(result).toBeTrue();
  });

  it('redirects an anonymous user to login and preserves the requested URL', () => {
    auth.isAuthenticated.and.returnValue(false);

    TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/students' } as RouterStateSnapshot),
    );

    expect(router.createUrlTree).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/students' },
    });
  });

  it('allows a route when the user has one of its roles', () => {
    auth.hasRole.and.returnValue(true);
    const route = { data: { roles: ['ADMIN'] } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => roleGuard(route, {} as RouterStateSnapshot));

    expect(result).toBeTrue();
    expect(auth.hasRole).toHaveBeenCalledWith(['ADMIN']);
  });

  it('redirects to 403 when the role is not allowed', () => {
    auth.hasRole.and.returnValue(false);
    const route = { data: { roles: ['ADMIN'] } } as unknown as ActivatedRouteSnapshot;

    TestBed.runInInjectionContext(() => roleGuard(route, {} as RouterStateSnapshot));

    expect(router.parseUrl).toHaveBeenCalledWith('/forbidden');
  });
});
