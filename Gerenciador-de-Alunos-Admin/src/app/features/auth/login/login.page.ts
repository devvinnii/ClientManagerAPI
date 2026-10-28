import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { BrandMarkComponent } from '../../../shared/components/brand-mark/brand-mark.component';
import { ApiError } from '../../../shared/models/api-error.model';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, BrandMarkComponent],
  templateUrl: './login.page.html',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly showPassword = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly sessionExpired = this.route.snapshot.queryParamMap.get('reason') === 'session-expired';

  readonly form = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  togglePasswordVisibility(): void {
    this.showPassword.update((visible) => !visible);
  }

  submit(): void {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.auth
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          const requestedUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          const destination = this.isSafeInternalUrl(requestedUrl)
            ? requestedUrl
            : this.auth.defaultRoute();
          void this.router.navigateByUrl(destination);
        },
        error: (error: HttpErrorResponse) => {
          const apiError = error.error as Partial<ApiError> | null;
          this.errorMessage.set(
            apiError?.message || 'Não foi possível entrar. Verifique seus dados e tente novamente.',
          );
        },
      });
  }

  private isSafeInternalUrl(url: string | null): url is string {
    return Boolean(url?.startsWith('/') && !url.startsWith('//'));
  }
}
