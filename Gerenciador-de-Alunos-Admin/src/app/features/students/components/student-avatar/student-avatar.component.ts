import { Component, Input, OnChanges, OnDestroy, signal, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { Student } from '../../models/student.model';
import { StudentService } from '../../services/student.service';

@Component({
  selector: 'app-student-avatar',
  template: `
    <div
      class="relative grid shrink-0 place-items-center overflow-hidden rounded-full bg-gradient-to-br from-sky-100 to-teal-100 font-black uppercase text-sky-800"
      [class.size-10]="size === 'sm'"
      [class.size-12]="size === 'md'"
      [class.size-24]="size === 'lg'"
    >
      @if (imageUrl()) {
        <img
          [src]="imageUrl()"
          [alt]="'Foto de ' + student.name"
          class="size-full object-cover"
          (error)="handleImageError()"
        />
      } @else {
        <span [class.text-xs]="size === 'sm'" [class.text-sm]="size === 'md'" [class.text-xl]="size === 'lg'">
          {{ initials() }}
        </span>
      }
      @if (loading()) {
        <span class="absolute inset-0 grid place-items-center bg-white/70" aria-hidden="true">
          <span class="size-4 animate-spin rounded-full border-2 border-sky-200 border-t-sky-700"></span>
        </span>
      }
    </div>
  `,
})
export class StudentAvatarComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) student!: Student;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';

  readonly imageUrl = signal<string | null>(null);
  readonly loading = signal(false);
  private subscription?: Subscription;

  constructor(private readonly students: StudentService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['student']) {
      this.loadImage();
    }
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.revokeImageUrl();
  }

  initials(): string {
    return this.student.name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0))
      .join('');
  }

  handleImageError(): void {
    this.revokeImageUrl();
    this.imageUrl.set(null);
  }

  private loadImage(): void {
    this.subscription?.unsubscribe();
    this.revokeImageUrl();
    this.imageUrl.set(null);

    if (!this.student.photoUrl) {
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.subscription = this.students.loadPhoto(this.student.photoUrl).subscribe({
      next: (blob) => {
        this.imageUrl.set(URL.createObjectURL(blob));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private revokeImageUrl(): void {
    const currentUrl = this.imageUrl();
    if (currentUrl) {
      URL.revokeObjectURL(currentUrl);
    }
  }
}
