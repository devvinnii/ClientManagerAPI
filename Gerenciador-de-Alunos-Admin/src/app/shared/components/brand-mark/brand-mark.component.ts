import { Component, Input, signal } from '@angular/core';

@Component({
  selector: 'app-brand-mark',
  template: `
    <div class="flex min-h-8 items-center" [class.text-white]="tone === 'light'">
      @if (!imageFailed()) {
        <img
          src="assets/brand/logo-talentos-capao.png"
          width="185"
          height="54"
          alt="Logo da Associação Esportiva Talentos do Capão"
          class="h-auto max-w-full object-contain"
          [class.w-24]="compact"
          [class.w-[11.5rem]]="!compact"
          (error)="imageFailed.set(true)"
        />
      } @else {
        <span class="text-sm font-extrabold leading-tight tracking-tight">
          Talentos do Capão
        </span>
      }
    </div>
  `,
})
export class BrandMarkComponent {
  @Input() tone: 'light' | 'dark' = 'dark';
  @Input() compact = false;

  readonly imageFailed = signal(false);
}
