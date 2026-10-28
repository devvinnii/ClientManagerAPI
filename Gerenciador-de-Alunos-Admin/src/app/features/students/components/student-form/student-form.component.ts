import { Component, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Student, StudentUpsert } from '../../models/student.model';
import { cpfValidator, digitsOnly } from '../../utils/student-validation';
import { StudentAvatarComponent } from '../student-avatar/student-avatar.component';

@Component({
  selector: 'app-student-form',
  imports: [ReactiveFormsModule, StudentAvatarComponent],
  templateUrl: './student-form.component.html',
})
export class StudentFormComponent implements OnInit, OnDestroy {
  @Input() student: Student | null = null;
  @Input() saving = false;
  @Input() apiMessage: string | null = null;
  @Input() apiFieldErrors: Record<string, string> = {};
  @Output() submitted = new EventEmitter<StudentUpsert>();
  @Output() closed = new EventEmitter<void>();

  readonly selectedPhoto = signal<File | null>(null);
  readonly photoPreview = signal<string | null>(null);
  readonly photoError = signal<string | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.email] }),
    cpf: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{11}$/), cpfValidator],
    }),
    phone: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern(/^$|^\d{10,11}$/)],
    }),
  });

  ngOnInit(): void {
    if (this.student) {
      this.form.setValue({
        name: this.student.name,
        email: this.student.email ?? '',
        cpf: this.student.cpf,
        phone: this.student.phone ?? '',
      });
    }
  }

  ngOnDestroy(): void {
    this.revokePreview();
  }

  normalizeCpf(): void {
    this.form.controls.cpf.setValue(digitsOnly(this.form.controls.cpf.value, 11));
  }

  normalizePhone(): void {
    this.form.controls.phone.setValue(digitsOnly(this.form.controls.phone.value, 11));
  }

  selectPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.photoError.set(null);
    this.revokePreview();
    this.selectedPhoto.set(null);

    if (!file) {
      this.photoPreview.set(null);
      return;
    }

    const allowedTypes = ['image/jpeg', 'image/png'];
    const allowedExtension = /\.(jpe?g|png)$/i.test(file.name);
    if (!allowedTypes.includes(file.type) || !allowedExtension) {
      this.photoError.set('Selecione uma imagem PNG, JPG ou JPEG.');
      input.value = '';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.photoError.set('A foto deve possuir no máximo 10 MB.');
      input.value = '';
      return;
    }

    this.selectedPhoto.set(file);
    this.photoPreview.set(URL.createObjectURL(file));
  }

  submit(): void {
    if (this.form.invalid || this.photoError()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.submitted.emit({
      name: value.name,
      email: value.email || undefined,
      cpf: value.cpf,
      phone: value.phone || undefined,
      photo: this.selectedPhoto() ?? undefined,
    });
  }

  fieldError(field: 'name' | 'email' | 'cpf' | 'phone'): string | null {
    if (this.apiFieldErrors[field]) {
      return this.apiFieldErrors[field];
    }
    const control = this.form.controls[field];
    if (!control.touched || !control.errors) {
      return null;
    }
    if (control.errors['required']) {
      return field === 'name' ? 'Informe o nome do aluno.' : 'Informe o CPF.';
    }
    if (control.errors['email']) {
      return 'Informe um e-mail válido.';
    }
    if (control.errors['pattern']) {
      return field === 'phone'
        ? 'O telefone deve possuir 10 ou 11 dígitos.'
        : 'O CPF deve possuir 11 dígitos.';
    }
    if (control.errors['cpf']) {
      return 'Informe um CPF válido.';
    }
    return 'Valor inválido.';
  }

  @HostListener('document:keydown.escape')
  closeOnEscape(): void {
    if (!this.saving) {
      this.closed.emit();
    }
  }

  private revokePreview(): void {
    const preview = this.photoPreview();
    if (preview) {
      URL.revokeObjectURL(preview);
    }
  }
}
