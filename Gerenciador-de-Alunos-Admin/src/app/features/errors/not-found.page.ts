import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink],
  template: `
    <main class="flex min-h-dvh items-center justify-center bg-slate-50 px-5 text-center">
      <div class="max-w-lg">
        <p class="text-sm font-black uppercase tracking-[0.25em] text-sky-700">Erro 404</p>
        <h1 class="mt-4 text-4xl font-black tracking-tight text-slate-900">Página não encontrada</h1>
        <p class="mt-4 leading-relaxed text-slate-500">O endereço informado não existe ou foi alterado.</p>
        <a [routerLink]="destination()" class="mt-8 inline-flex min-h-11 items-center rounded-xl bg-sky-700 px-5 font-bold text-white hover:bg-sky-800">
          {{ auth.isAuthenticated() ? 'Voltar ao painel' : 'Ir para o login' }}
        </a>
      </div>
    </main>
  `,
})
export class NotFoundPage {
  readonly auth = inject(AuthService);

  destination(): string {
    return this.auth.isAuthenticated() ? this.auth.defaultRoute() : '/login';
  }
}
