import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER, Observable, of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { StudentService } from '../students/services/student.service';
import { ClassesPage } from './classes.page';
import { ClassGroupPage } from './models/class-group.model';
import { ClassGroupService } from './services/class-group.service';

describe('ClassesPage', () => {
  const page: ClassGroupPage = {
    content: [
      {
        id: 1,
        name: 'Natação Infantil A',
        modality: 'NATACAO',
        description: 'Turma infantil',
        responsible: 'Professora Ana',
        maxCapacity: 12,
        occupiedSpots: 9,
        availableSpots: 3,
        active: true,
        schedules: [{ id: 1, dayOfWeek: 'SEGUNDA', startTime: '09:00:00', endTime: '10:00:00' }],
        createdAt: '2026-10-28T12:00:00Z',
        updatedAt: '2026-10-28T12:00:00Z',
      },
    ],
    totalElements: 1,
    totalPages: 1,
    page: 0,
    size: 10,
    sortBy: 'name',
    sortDirection: 'ASC',
  };

  function createFixture(listResult: Observable<ClassGroupPage>, admin = true): ComponentFixture<ClassesPage> {
    const classService = jasmine.createSpyObj<ClassGroupService>('ClassGroupService', [
      'list',
      'getById',
      'getEnrollments',
      'create',
      'update',
      'updateStatus',
      'enroll',
      'cancelEnrollment',
      'reenroll',
    ]);
    classService.list.and.returnValue(listResult);
    const studentService = jasmine.createSpyObj<StudentService>('StudentService', ['list', 'getById', 'loadPhoto']);
    const auth = { hasRole: () => admin };

    TestBed.configureTestingModule({
      imports: [ClassesPage],
      providers: [
        { provide: ClassGroupService, useValue: classService },
        { provide: StudentService, useValue: studentService },
        { provide: AuthService, useValue: auth },
      ],
    });
    const fixture = TestBed.createComponent(ClassesPage);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('renders classes, official modality, schedules and available spots from the API', () => {
    const fixture = createFixture(of(page));
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Natação Infantil A');
    expect(text).toContain('Natação');
    expect(text).toContain('Segunda-feira');
    expect(text).toContain('3 disponíveis');
    expect(text).toContain('Cadastrar turma');
    expect(fixture.nativeElement.querySelector('[role="progressbar"]')?.getAttribute('aria-valuenow')).toBe('75');
  });

  it('keeps the loading state visible while the API is pending', () => {
    const fixture = createFixture(NEVER);
    expect(fixture.nativeElement.textContent).toContain('Carregando turmas');
  });

  it('shows the API error and hides write actions from non-admin users', () => {
    const error = new HttpErrorResponse({
      status: 500,
      error: { message: 'Falha controlada ao consultar turmas' },
    });
    const fixture = createFixture(throwError(() => error), false);
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Falha controlada ao consultar turmas');
    expect(text).not.toContain('Cadastrar turma');
  });
});
