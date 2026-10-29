import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { forkJoin, Observable, switchMap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmationDialogComponent } from '../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { ApiError } from '../../shared/models/api-error.model';
import { CpfPipe } from '../students/pipes/cpf.pipe';
import { StudentService } from '../students/services/student.service';
import { StudentAvatarComponent } from '../students/components/student-avatar/student-avatar.component';
import { StudentDetailsComponent } from '../students/components/student-details/student-details.component';
import { Student } from '../students/models/student.model';
import { ClassFormComponent } from './components/class-form/class-form.component';
import {
  ClassGroup,
  ClassGroupPage,
  ClassGroupUpsert,
  Enrollment,
  MODALITIES,
  Modality,
  StudentCandidate,
  modalityLabel,
  weekdayLabel,
} from './models/class-group.model';
import { ClassGroupService } from './services/class-group.service';

type PendingAction =
  | { type: 'status'; classGroup: ClassGroup }
  | { type: 'cancel'; classGroup: ClassGroup; enrollment: Enrollment };

@Component({
  selector: 'app-classes-page',
  imports: [ReactiveFormsModule, ClassFormComponent, ConfirmationDialogComponent, PageHeaderComponent, ToastComponent, CpfPipe, DatePipe, StudentAvatarComponent, StudentDetailsComponent],
  templateUrl: './classes.page.html',
})
export class ClassesPage implements OnInit {
  private readonly service = inject(ClassGroupService);
  private readonly studentService = inject(StudentService);
  private readonly auth = inject(AuthService);
  private readonly toasts = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly modalities = MODALITIES;
  readonly modalityLabel = modalityLabel;
  readonly weekdayLabel = weekdayLabel;
  readonly pageData = signal<ClassGroupPage | null>(null);
  readonly classes = computed(() => this.pageData()?.content ?? []);
  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly isAdmin = computed(() => this.auth.hasRole(['ADMIN']));
  readonly formOpen = signal(false);
  readonly formClass = signal<ClassGroup | null>(null);
  readonly formSaving = signal(false);
  readonly formMessage = signal<string | null>(null);
  readonly formFieldErrors = signal<Record<string, string>>({});
  readonly details = signal<ClassGroup | null>(null);
  readonly enrollments = signal<Enrollment[]>([]);
  readonly detailsLoading = signal(false);
  readonly candidateName = new FormControl('', { nonNullable: true });
  readonly candidates = signal<StudentCandidate[]>([]);
  readonly candidateLoading = signal(false);
  readonly actionLoading = signal(false);
  readonly pendingAction = signal<PendingAction | null>(null);
  readonly studentDetails = signal<Student | null>(null);

  readonly filters = new FormGroup({
    name: new FormControl('', { nonNullable: true }),
    modality: new FormControl<'all' | Modality>('all', { nonNullable: true }),
    active: new FormControl<'all' | 'true' | 'false'>('all', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.loadClasses();
  }

  loadClasses(): void {
    const filters = this.filters.getRawValue();
    this.loading.set(true);
    this.loadError.set(null);
    this.service
      .list({
        name: filters.name || undefined,
        modality: filters.modality === 'all' ? undefined : filters.modality,
        active: filters.active === 'all' ? undefined : filters.active === 'true',
        page: this.page(),
        size: this.size(),
        sortBy: 'name',
        sortDirection: 'ASC',
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.pageData.set(data);
          this.page.set(data.page);
          this.loading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          this.loadError.set(this.apiError(error).message ?? 'Não foi possível carregar as turmas.');
        },
      });
  }

  applyFilters(): void {
    this.page.set(0);
    this.loadClasses();
  }

  clearFilters(): void {
    this.filters.setValue({ name: '', modality: 'all', active: 'all' });
    this.page.set(0);
    this.loadClasses();
  }

  changePage(target: number): void {
    if (target < 0 || target >= (this.pageData()?.totalPages ?? 0)) return;
    this.page.set(target);
    this.loadClasses();
  }

  changeSize(event: Event): void {
    this.size.set(Number((event.target as HTMLSelectElement).value));
    this.page.set(0);
    this.loadClasses();
  }

  occupancyPercentage(classGroup: ClassGroup): number {
    if (classGroup.maxCapacity < 1) return 0;
    return Math.min(100, Math.round((classGroup.occupiedSpots / classGroup.maxCapacity) * 100));
  }

  openCreate(): void {
    this.formClass.set(null);
    this.resetFormErrors();
    this.formOpen.set(true);
  }

  openEdit(classGroup: ClassGroup): void {
    this.service.getById(classGroup.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (value) => {
        this.formClass.set(value);
        this.resetFormErrors();
        this.formOpen.set(true);
      },
      error: (error: HttpErrorResponse) => this.toasts.error('Erro ao carregar turma', this.apiError(error).message),
    });
  }

  saveClass(value: ClassGroupUpsert): void {
    const current = this.formClass();
    this.formSaving.set(true);
    this.resetFormErrors();
    const save = current ? this.service.update(current.id, value) : this.service.create(value);
    save
      .pipe(
        switchMap((saved) =>
          current && saved.active !== value.active
            ? this.service.updateStatus(saved.id, value.active)
            : [saved],
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (saved) => {
          this.formSaving.set(false);
          this.formOpen.set(false);
          this.toasts.success(current ? 'Turma atualizada' : 'Turma cadastrada', `${saved.name} foi salva com sucesso.`);
          this.loadClasses();
        },
        error: (error: HttpErrorResponse) => {
          const apiError = this.apiError(error);
          this.formSaving.set(false);
          this.formMessage.set(apiError.message ?? 'Não foi possível salvar a turma.');
          this.formFieldErrors.set(apiError.fieldErrors ?? {});
        },
      });
  }

  openDetails(classGroup: ClassGroup): void {
    this.detailsLoading.set(true);
    this.candidates.set([]);
    forkJoin({
      classGroup: this.service.getById(classGroup.id),
      enrollments: this.service.getEnrollments(classGroup.id),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ classGroup: detail, enrollments }) => {
          this.details.set(detail);
          this.enrollments.set(enrollments);
          this.detailsLoading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.detailsLoading.set(false);
          this.toasts.error('Erro ao carregar turma', this.apiError(error).message);
        },
      });
  }

  closeDetails(): void {
    if (!this.actionLoading()) {
      this.details.set(null);
      this.enrollments.set([]);
      this.candidates.set([]);
      this.candidateName.setValue('');
    }
  }

  searchCandidates(): void {
    const name = this.candidateName.value.trim();
    if (!name) {
      this.candidates.set([]);
      return;
    }
    this.candidateLoading.set(true);
    this.studentService
      .list({ name, active: true, page: 0, size: 20, sortBy: 'name', sortDirection: 'ASC' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          const activeIds = new Set(this.enrollments().filter((item) => item.status === 'ATIVA').map((item) => item.studentId));
          this.candidates.set(page.content.filter((student) => !activeIds.has(student.id)));
          this.candidateLoading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.candidateLoading.set(false);
          this.toasts.error('Erro ao buscar alunos', this.apiError(error).message);
        },
      });
  }

  enroll(student: StudentCandidate): void {
    const classGroup = this.details();
    if (!classGroup) return;
    this.actionLoading.set(true);
    this.service.enroll(classGroup.id, student.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.toasts.success('Matrícula realizada', `${student.name} foi matriculado(a) na turma.`);
        this.openDetails(classGroup);
        this.loadClasses();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading.set(false);
        this.toasts.error('Não foi possível matricular', this.apiError(error).message);
      },
    });
  }

  requestStatus(classGroup: ClassGroup): void {
    this.pendingAction.set({ type: 'status', classGroup });
  }

  requestCancel(enrollment: Enrollment): void {
    const classGroup = this.details();
    if (classGroup) this.pendingAction.set({ type: 'cancel', classGroup, enrollment });
  }

  confirmAction(): void {
    const pending = this.pendingAction();
    if (!pending) return;
    this.actionLoading.set(true);
    const request: Observable<unknown> = pending.type === 'status'
      ? this.service.updateStatus(pending.classGroup.id, !pending.classGroup.active)
      : this.service.cancelEnrollment(pending.classGroup.id, pending.enrollment.id);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.pendingAction.set(null);
        this.toasts.success('Ação concluída', pending.type === 'status' ? 'Status da turma atualizado.' : 'Matrícula cancelada.');
        if (pending.type === 'cancel') this.openDetails(pending.classGroup);
        this.loadClasses();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading.set(false);
        this.pendingAction.set(null);
        this.toasts.error('Não foi possível concluir a ação', this.apiError(error).message);
      },
    });
  }

  reenroll(enrollment: Enrollment): void {
    const classGroup = this.details();
    if (!classGroup) return;
    this.actionLoading.set(true);
    this.service.reenroll(classGroup.id, enrollment.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.toasts.success('Rematrícula realizada', `${enrollment.studentName} voltou à turma.`);
        this.openDetails(classGroup);
        this.loadClasses();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading.set(false);
        this.toasts.error('Não foi possível rematricular', this.apiError(error).message);
      },
    });
  }

  studentFromEnrollment(enrollment: Enrollment): Student {
    return {
      id: enrollment.studentId,
      name: enrollment.studentName,
      cpf: enrollment.studentCpf,
      email: null,
      phone: null,
      photoUrl: enrollment.studentPhotoUrl,
      active: enrollment.studentActive,
    };
  }

  viewStudent(enrollment: Enrollment): void {
    this.actionLoading.set(true);
    this.studentService.getById(enrollment.studentId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (student) => {
        this.actionLoading.set(false);
        this.details.set(null);
        this.studentDetails.set(student);
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading.set(false);
        this.toasts.error('Não foi possível carregar o aluno', this.apiError(error).message);
      },
    });
  }

  confirmationTitle(): string {
    return this.pendingAction()?.type === 'cancel' ? 'Cancelar matrícula?' : 'Alterar status da turma?';
  }

  confirmationMessage(): string {
    const pending = this.pendingAction();
    if (!pending) return '';
    return pending.type === 'cancel'
      ? `A matrícula de ${pending.enrollment.studentName} será cancelada, mas permanecerá no histórico.`
      : `${pending.classGroup.name} ficará ${pending.classGroup.active ? 'inativa' : 'ativa'}.`;
  }

  private apiError(error: HttpErrorResponse): Partial<ApiError> {
    return error.error && typeof error.error === 'object'
      ? (error.error as Partial<ApiError>)
      : { message: 'Ocorreu um erro de comunicação com o servidor.' };
  }

  private resetFormErrors(): void {
    this.formMessage.set(null);
    this.formFieldErrors.set({});
  }
}
