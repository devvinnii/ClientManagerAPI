import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirmation-dialog',
  template: `
    <div class="fixed inset-0 z-[70] flex items-center justify-center p-4" role="presentation">
      <button
        type="button"
        class="absolute inset-0 bg-slate-950/60 backdrop-blur-[2px]"
        aria-label="Cancelar ação"
        (click)="cancel.emit()"
      ></button>
      <section
        class="relative w-full max-w-md rounded-3xl bg-white p-6 shadow-2xl"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirmation-title"
        aria-describedby="confirmation-message"
      >
        <div
          class="grid size-12 place-items-center rounded-2xl text-xl font-black"
          [class.bg-red-100]="danger"
          [class.text-red-700]="danger"
          [class.bg-amber-100]="!danger"
          [class.text-amber-700]="!danger"
          aria-hidden="true"
        >
          !
        </div>
        <h2 id="confirmation-title" class="mt-5 text-xl font-black text-slate-900">{{ title }}</h2>
        <p id="confirmation-message" class="mt-2 leading-relaxed text-slate-600">{{ message }}</p>

        <div class="mt-7 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            (click)="cancel.emit()"
            [disabled]="loading"
            class="min-h-11 rounded-xl border border-slate-300 px-5 font-bold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            (click)="confirm.emit()"
            [disabled]="loading"
            class="flex min-h-11 items-center justify-center rounded-xl px-5 font-bold text-white disabled:opacity-60"
            [class.bg-red-700]="danger"
            [class.bg-amber-600]="!danger"
          >
            @if (loading) {
              <span class="mr-2 size-4 animate-spin rounded-full border-2 border-white/30 border-t-white"></span>
            }
            {{ confirmLabel }}
          </button>
        </div>
      </section>
    </div>
  `,
})
export class ConfirmationDialogComponent {
  @Input({ required: true }) title = '';
  @Input({ required: true }) message = '';
  @Input() confirmLabel = 'Confirmar';
  @Input() danger = false;
  @Input() loading = false;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (!this.loading) {
      this.cancel.emit();
    }
  }
}
