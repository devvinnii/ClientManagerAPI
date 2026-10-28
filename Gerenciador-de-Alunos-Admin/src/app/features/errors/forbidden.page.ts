import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-forbidden-page',
  imports: [RouterLink],
  template: `
    <section class="flex min-h-[65dvh] items-center justify-center px-4 text-center">
      <div class="max-w-lg">
        <div class="mx-auto grid size-16 place-items-center rounded-2xl bg-amber-100 text-2xl font-black text-amber-800">403</div>
        <h1 class="mt-6 text-3xl font-black tracking-tight text-slate-900">Acesso não permitido</h1>
        <p class="mt-3 leading-relaxed text-slate-500">
          Seu perfil não possui permissão para acessar esta área. Se isso parecer incorreto, procure um administrador.
        </p>
        <a [routerLink]="auth.defaultRoute()" class="mt-7 inline-flex min-h-11 items-center rounded-xl bg-sky-700 px-5 font-bold text-white hover:bg-sky-800">
          Voltar ao início
        </a>
      </div>
    </section>
  `,
})
export class ForbiddenPage {
  readonly auth = inject(AuthService);
}
