import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  template: `
    <header class="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
      <div class="min-w-0">
        <p class="text-xs font-extrabold uppercase tracking-[0.16em] text-sky-700">{{ eyebrow }}</p>
        <h1 [id]="titleId" class="mt-2 text-3xl font-black tracking-[-0.025em] text-slate-950 sm:text-[2rem]">
          {{ title }}
        </h1>
        <p class="mt-2 max-w-2xl text-sm leading-6 text-slate-600 sm:text-base">{{ description }}</p>
      </div>
      <div class="flex shrink-0 flex-wrap items-center gap-2">
        <ng-content />
      </div>
    </header>
  `,
})
export class PageHeaderComponent {
  @Input({ required: true }) title = '';
  @Input({ required: true }) description = '';
  @Input() eyebrow = 'Painel administrativo';
  @Input() titleId = 'page-title';
}
