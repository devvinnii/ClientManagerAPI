import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { httpErrorInterceptor } from './http-error.interceptor';

describe('httpErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['expireSession']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    router.navigate.and.resolveTo(true);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([httpErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('expires the session when a protected request returns 401', () => {
    httpClient.get('/api/clients').subscribe({ error: () => undefined });
    httpTesting.expectOne('/api/clients').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.expireSession).toHaveBeenCalled();
  });

  it('redirects to the forbidden page on 403', () => {
    httpClient.delete('/api/clients/1').subscribe({ error: () => undefined });
    httpTesting.expectOne('/api/clients/1').flush({}, { status: 403, statusText: 'Forbidden' });

    expect(router.navigate).toHaveBeenCalledWith(['/forbidden']);
  });

  it('keeps authentication errors on the login page', () => {
    httpClient.post('/api/auth/login', {}).subscribe({ error: () => undefined });
    httpTesting
      .expectOne('/api/auth/login')
      .flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.expireSession).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
