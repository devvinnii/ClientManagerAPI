import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmationDialogComponent } from '../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { ApiError } from '../../shared/models/api-error.model';
import { StudentAvatarComponent } from './components/student-avatar/student-avatar.component';
import { StudentDetailsComponent } from './components/student-details/student-details.component';
import { StudentFormComponent } from './components/student-form/student-form.component';
import {
  Student,
  StudentPage,
  StudentQuery,
  StudentSortDirection,
  StudentSortField,
  StudentUpsert,
} from './models/student.model';
import { CpfPipe } from './pipes/cpf.pipe';
import { PhonePipe } from './pipes/phone.pipe';
import { StudentService } from './services/student.service';
import { digitsOnly } from './utils/student-validation';

interface PendingAction {
  type: 'delete' | 'status';
  student: Student;
  targetActive?: boolean;
}

@Component({
  selector: 'app-students-page',
  imports: [
    ReactiveFormsModule,
    CpfPipe,
    PhonePipe,
    StudentAvatarComponent,
    StudentFormComponent,
    StudentDetailsComponent,
    ConfirmationDialogComponent,
    ToastComponent,
  ],
  templateUrl: './students.page.html',
})
export class StudentsPage implements OnInit, OnDestroy {
  private readonly studentService = inject(StudentService);
  private readonly auth = inject(AuthService);
  private readonly toasts = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private listSubscription?: Subscription;

  readonly pageData = signal<StudentPage | null>(null);
  readonly students = computed(() => this.pageData()?.content ?? []);
  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly sortBy = signal<StudentSortField>('name');
  readonly sortDirection = signal<StudentSortDirection>('ASC');
  readonly actionLoadingId = signal<number | null>(null);
  readonly formOpen = signal(false);
  readonly formStudent = signal<Student | null>(null);
  readonly formSaving = signal(false);
  readonly formApiMessage = signal<string | null>(null);
  readonly formFieldErrors = signal<Record<string, string>>({});
  readonly detailsStudent = signal<Student | null>(null);
  readonly pendingAction = signal<PendingAction | null>(null);
  readonly confirmationLoading = signal(false);
  readonly isAdmin = computed(() => this.auth.hasRole(['ADMIN']));
  readonly skeletonRows = [1, 2, 3, 4, 5];

  readonly filters = new FormGroup({
    name: new FormControl('', { nonNullable: true }),
    cpf: new FormControl('', { nonNullable: true }),
    active: new FormControl<'all' | 'true' | 'false'>('all', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.loadStudents();
  }

  ngOnDestroy(): void {
    this.listSubscription?.unsubscribe();
  }

  applyFilters(): void {
    this.page.set(0);
    this.loadStudents();
  }

  clearFilters(): void {
    this.filters.setValue({ name: '', cpf: '', active: 'all' });
    this.page.set(0);
    this.sortBy.set('name');
    this.sortDirection.set('ASC');
    this.loadStudents();
  }

  normalizeFilterCpf(): void {
    this.filters.controls.cpf.setValue(digitsOnly(this.filters.controls.cpf.value, 11));
  }

  changePage(targetPage: number): void {
    const totalPages = this.pageData()?.totalPages ?? 0;
    if (targetPage < 0 || targetPage >= totalPages || targetPage === this.page()) {
      return;
    }
    this.page.set(targetPage);
    this.loadStudents();
  }

  changePageSize(event: Event): void {
    this.size.set(Number((event.target as HTMLSelectElement).value));
    this.page.set(0);
    this.loadStudents();
  }

  changeSortField(event: Event): void {
    this.sortBy.set((event.target as HTMLSelectElement).value as StudentSortField);
    this.page.set(0);
    this.loadStudents();
  }

  changeSortDirection(event: Event): void {
    this.sortDirection.set((event.target as HTMLSelectElement).value as StudentSortDirection);
    this.page.set(0);
    this.loadStudents();
  }

  sort(field: StudentSortField): void {
    if (this.sortBy() === field) {
      this.sortDirection.update((direction) => (direction === 'ASC' ? 'DESC' : 'ASC'));
    } else {
      this.sortBy.set(field);
      this.sortDirection.set('ASC');
    }
    this.page.set(0);
    this.loadStudents();
  }

  ariaSort(field: StudentSortField): 'ascending' | 'descending' | 'none' {
    if (this.sortBy() !== field) {
      return 'none';
    }
    return this.sortDirection() === 'ASC' ? 'ascending' : 'descending';
  }

  openCreate(): void {
    this.resetFormErrors();
    this.formStudent.set(null);
    this.formOpen.set(true);
  }

  openDetails(student: Student): void {
    this.loadStudent(student.id, 'details');
  }

  openEdit(student: Student): void {
    this.loadStudent(student.id, 'edit');
  }

  editFromDetails(student: Student): void {
    this.detailsStudent.set(null);
    this.resetFormErrors();
    this.formStudent.set(student);
    this.formOpen.set(true);
  }

  closeForm(): void {
    if (!this.formSaving()) {
      this.formOpen.set(false);
      this.formStudent.set(null);
      this.resetFormErrors();
    }
  }

  saveStudent(value: StudentUpsert): void {
    this.formSaving.set(true);
    this.resetFormErrors();
    const currentStudent = this.formStudent();
    const request = currentStudent
      ? this.studentService.update(currentStudent.id, value)
      : this.studentService.create(value);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (student) => {
        this.formSaving.set(false);
        this.formOpen.set(false);
        this.formStudent.set(null);
        this.toasts.success(
          currentStudent ? 'Aluno atualizado' : 'Aluno cadastrado',
          `${student.name} foi ${currentStudent ? 'atualizado' : 'cadastrado'} com sucesso.`,
        );
        if (!currentStudent) {
          this.page.set(0);
        }
        this.loadStudents();
      },
      error: (error: HttpErrorResponse) => {
        const apiError = this.extractApiError(error);
        this.formSaving.set(false);
        this.formApiMessage.set(apiError.message ?? 'Não foi possível salvar o aluno.');
        this.formFieldErrors.set(apiError.fieldErrors ?? {});
      },
    });
  }

  requestStatusChange(student: Student): void {
    this.pendingAction.set({ type: 'status', student, targetActive: !student.active });
  }

  requestDelete(student: Student): void {
    this.pendingAction.set({ type: 'delete', student });
  }

  confirmAction(): void {
    const action = this.pendingAction();
    if (!action) {
      return;
    }

    this.confirmationLoading.set(true);
    const request: Observable<unknown> = action.type === 'delete'
      ? this.studentService.delete(action.student.id)
      : this.studentService.updateStatus(action.student.id, Boolean(action.targetActive));

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.confirmationLoading.set(false);
        this.pendingAction.set(null);
        if (action.type === 'delete') {
          this.toasts.success('Aluno excluído', `${action.student.name} foi excluído com sucesso.`);
          if (this.students().length === 1 && this.page() > 0) {
            this.page.update((page) => page - 1);
          }
        } else {
          this.toasts.success(
            action.targetActive ? 'Aluno ativado' : 'Aluno inativado',
            `${action.student.name} agora está ${action.targetActive ? 'ativo' : 'inativo'}.`,
          );
        }
        this.loadStudents();
      },
      error: (error: HttpErrorResponse) => {
        this.confirmationLoading.set(false);
        this.pendingAction.set(null);
        const apiError = this.extractApiError(error);
        this.toasts.error('Não foi possível concluir a ação', apiError.message);
      },
    });
  }

  confirmationTitle(): string {
    const action = this.pendingAction();
    if (action?.type === 'delete') {
      return 'Excluir aluno permanentemente?';
    }
    return action?.targetActive ? 'Ativar aluno?' : 'Inativar aluno?';
  }

  confirmationMessage(): string {
    const action = this.pendingAction();
    if (!action) {
      return '';
    }
    if (action.type === 'delete') {
      return `O cadastro de ${action.student.name} e sua foto serão removidos. Esta ação não pode ser desfeita.`;
    }
    return action.targetActive
      ? `${action.student.name} voltará a aparecer como aluno ativo.`
      : `${action.student.name} será mantido no sistema, mas ficará marcado como inativo.`;
  }

  confirmationLabel(): string {
    const action = this.pendingAction();
    if (action?.type === 'delete') {
      return 'Excluir definitivamente';
    }
    return action?.targetActive ? 'Ativar' : 'Inativar';
  }

  firstVisibleItem(): number {
    return this.pageData()?.totalElements ? this.page() * this.size() + 1 : 0;
  }

  lastVisibleItem(): number {
    const data = this.pageData();
    return data ? Math.min((this.page() + 1) * this.size(), data.totalElements) : 0;
  }

  loadStudents(): void {
    this.listSubscription?.unsubscribe();
    this.loading.set(true);
    this.loadError.set(null);

    this.listSubscription = this.studentService
      .list(this.currentQuery())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          this.pageData.set(page);
          this.page.set(page.page);
          this.loading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          const apiError = this.extractApiError(error);
          this.loading.set(false);
          this.loadError.set(apiError.message ?? 'Não foi possível carregar os alunos.');
        },
      });
  }

  private loadStudent(id: number, destination: 'details' | 'edit'): void {
    this.actionLoadingId.set(id);
    this.studentService
      .getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (student) => {
          this.actionLoadingId.set(null);
          if (destination === 'details') {
            this.detailsStudent.set(student);
          } else {
            this.resetFormErrors();
            this.formStudent.set(student);
            this.formOpen.set(true);
          }
        },
        error: (error: HttpErrorResponse) => {
          this.actionLoadingId.set(null);
          const apiError = this.extractApiError(error);
          this.toasts.error('Não foi possível carregar o aluno', apiError.message);
        },
      });
  }

  private currentQuery(): StudentQuery {
    const filters = this.filters.getRawValue();
    return {
      name: filters.name || undefined,
      cpf: filters.cpf || undefined,
      active: filters.active === 'all' ? undefined : filters.active === 'true',
      page: this.page(),
      size: this.size(),
      sortBy: this.sortBy(),
      sortDirection: this.sortDirection(),
    };
  }

  private extractApiError(error: HttpErrorResponse): Partial<ApiError> {
    if (error.error && typeof error.error === 'object') {
      return error.error as Partial<ApiError>;
    }
    if (typeof error.error === 'string') {
      try {
        return JSON.parse(error.error) as Partial<ApiError>;
      } catch {
        // A resposta não utiliza o contrato estruturado da API.
      }
    }
    return { message: 'Ocorreu um erro de comunicação com o servidor.' };
  }

  private resetFormErrors(): void {
    this.formApiMessage.set(null);
    this.formFieldErrors.set({});
  }
}
