import { Component, inject } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div
      class="pointer-events-none fixed inset-x-4 top-4 z-[80] flex flex-col items-end gap-3 sm:left-auto sm:w-[24rem]"
      aria-live="polite"
      aria-atomic="false"
    >
      @for (toast of toasts.messages(); track toast.id) {
        <article
          class="pointer-events-auto w-full rounded-2xl border bg-white p-4 shadow-xl shadow-slate-900/10"
          [class.border-emerald-200]="toast.type === 'success'"
          [class.border-red-200]="toast.type === 'error'"
          role="status"
        >
          <div class="flex gap-3">
            <span
              class="grid size-9 shrink-0 place-items-center rounded-full text-lg font-black"
              [class.bg-emerald-100]="toast.type === 'success'"
              [class.text-emerald-700]="toast.type === 'success'"
              [class.bg-red-100]="toast.type === 'error'"
              [class.text-red-700]="toast.type === 'error'"
              aria-hidden="true"
            >
              {{ toast.type === 'success' ? '✓' : '!' }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="font-bold text-slate-900">{{ toast.title }}</p>
              @if (toast.message) {
                <p class="mt-1 text-sm leading-relaxed text-slate-600">{{ toast.message }}</p>
              }
            </div>
            <button
              type="button"
              (click)="toasts.remove(toast.id)"
              class="grid size-8 shrink-0 place-items-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-700"
              aria-label="Fechar mensagem"
            >
              ×
            </button>
          </div>
        </article>
      }
    </div>
  `,
})
export class ToastComponent {
  readonly toasts = inject(ToastService);
}
