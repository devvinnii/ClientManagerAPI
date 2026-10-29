import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ClassGroup,
  ClassGroupUpsert,
  ClassWeekday,
  MODALITIES,
  Modality,
  WEEKDAYS,
} from '../../models/class-group.model';

type ScheduleForm = FormGroup<{
  dayOfWeek: FormControl<ClassWeekday>;
  startTime: FormControl<string>;
  endTime: FormControl<string>;
}>;

@Component({
  selector: 'app-class-form',
  imports: [ReactiveFormsModule],
  templateUrl: './class-form.component.html',
})
export class ClassFormComponent implements OnInit {
  @Input() classGroup: ClassGroup | null = null;
  @Input() saving = false;
  @Input() apiMessage: string | null = null;
  @Input() apiFieldErrors: Record<string, string> = {};
  @Output() submitted = new EventEmitter<ClassGroupUpsert>();
  @Output() closed = new EventEmitter<void>();

  readonly modalities = MODALITIES;
  readonly weekdays = WEEKDAYS;
  readonly form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150)] }),
    modality: new FormControl<Modality>('NATACAO', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(2000)] }),
    responsible: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150)] }),
    maxCapacity: new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1)] }),
    active: new FormControl(true, { nonNullable: true }),
    schedules: new FormArray<ScheduleForm>([]),
  });

  ngOnInit(): void {
    if (this.classGroup) {
      this.form.patchValue({
        name: this.classGroup.name,
        modality: this.classGroup.modality,
        description: this.classGroup.description ?? '',
        responsible: this.classGroup.responsible,
        maxCapacity: this.classGroup.maxCapacity,
        active: this.classGroup.active,
      });
      this.classGroup.schedules.forEach((schedule) => this.schedules.push(this.scheduleForm(schedule)));
    }
    if (!this.schedules.length) this.addSchedule();
  }

  get schedules(): FormArray<ScheduleForm> {
    return this.form.controls.schedules;
  }

  addSchedule(): void {
    this.schedules.push(this.scheduleForm());
  }

  removeSchedule(index: number): void {
    if (this.schedules.length > 1) this.schedules.removeAt(index);
  }

  submit(): void {
    if (this.form.invalid || this.hasInvalidTime()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.submitted.emit({
      ...value,
      description: value.description || undefined,
    });
  }

  hasInvalidTime(): boolean {
    return this.schedules.controls.some((schedule) => {
      const { startTime, endTime } = schedule.getRawValue();
      return Boolean(startTime && endTime && endTime <= startTime);
    });
  }

  fieldError(field: 'name' | 'responsible' | 'maxCapacity'): string | null {
    if (this.apiFieldErrors[field]) return this.apiFieldErrors[field];
    const control = this.form.controls[field];
    if (!control.touched || !control.errors) return null;
    if (control.errors['required']) return 'Este campo é obrigatório.';
    if (control.errors['min']) return 'A capacidade deve ser maior que zero.';
    return 'Valor inválido.';
  }

  @HostListener('document:keydown.escape')
  closeOnEscape(): void {
    if (!this.saving) this.closed.emit();
  }

  private scheduleForm(value?: { dayOfWeek: ClassWeekday; startTime: string; endTime: string }): ScheduleForm {
    return new FormGroup({
      dayOfWeek: new FormControl<ClassWeekday>(value?.dayOfWeek ?? 'SEGUNDA', { nonNullable: true }),
      startTime: new FormControl(value?.startTime?.slice(0, 5) ?? '08:00', { nonNullable: true, validators: [Validators.required] }),
      endTime: new FormControl(value?.endTime?.slice(0, 5) ?? '09:00', { nonNullable: true, validators: [Validators.required] }),
    });
  }
}
