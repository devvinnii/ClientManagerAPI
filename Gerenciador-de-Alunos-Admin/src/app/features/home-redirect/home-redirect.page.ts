import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-home-redirect',
  template: `
    <div class="flex min-h-64 items-center justify-center" role="status">
      <span class="size-8 animate-spin rounded-full border-4 border-sky-100 border-t-sky-700"></span>
      <span class="sr-only">Carregando</span>
    </div>
  `,
})
export class HomeRedirectPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    void this.router.navigateByUrl(this.auth.defaultRoute(), { replaceUrl: true });
  }
}
