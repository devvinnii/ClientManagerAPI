import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';

export const httpErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isLoginRequest = request.url === `${environment.apiBaseUrl}/auth/login`;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isLoginRequest) {
        auth.expireSession();
      } else if (error.status === 403 && !isLoginRequest) {
        void router.navigate(['/forbidden']);
      }
      return throwError(() => error);
    }),
  );
};
