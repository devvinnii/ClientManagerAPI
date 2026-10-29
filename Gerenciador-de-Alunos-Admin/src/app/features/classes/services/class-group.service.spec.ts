import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ClassGroupService } from './class-group.service';

describe('ClassGroupService', () => {
  let service: ClassGroupService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ClassGroupService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists classes using filters and pagination', () => {
    service
      .list({
        name: 'Ballet',
        modality: 'BALLET',
        active: true,
        page: 1,
        size: 20,
        sortBy: 'name',
        sortDirection: 'ASC',
      })
      .subscribe();
    const request = http.expectOne((candidate) => candidate.url === '/api/turmas');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('modality')).toBe('BALLET');
    expect(request.request.params.get('active')).toBe('true');
    expect(request.request.params.get('page')).toBe('1');
    request.flush({ content: [], totalElements: 0, totalPages: 0, page: 1, size: 20 });
  });

  it('uses the enrollment lifecycle endpoints', () => {
    service.enroll(3, 7).subscribe();
    const create = http.expectOne('/api/turmas/3/matriculas');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({ studentId: 7 });
    create.flush({});

    service.cancelEnrollment(3, 11).subscribe();
    const cancel = http.expectOne('/api/turmas/3/matriculas/11');
    expect(cancel.request.method).toBe('DELETE');
    cancel.flush({});

    service.reenroll(3, 11).subscribe();
    const reenroll = http.expectOne('/api/turmas/3/matriculas/11/rematricula');
    expect(reenroll.request.method).toBe('POST');
    reenroll.flush({});
  });

  it('creates classes without sending presentation-only status in the body', () => {
    service
      .create({
        name: ' Natação Infantil ',
        modality: 'NATACAO',
        responsible: ' Professora Ana ',
        maxCapacity: 12,
        active: true,
        schedules: [{ dayOfWeek: 'SEGUNDA', startTime: '08:00', endTime: '09:00' }],
      })
      .subscribe();

    const request = http.expectOne('/api/turmas');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.name).toBe('Natação Infantil');
    expect(request.request.body.responsible).toBe('Professora Ana');
    expect(request.request.body.active).toBeUndefined();
    request.flush({});
  });

  it('loads history and updates status with the expected contracts', () => {
    service.getEnrollments(5).subscribe();
    const history = http.expectOne('/api/turmas/5/matriculas');
    expect(history.request.method).toBe('GET');
    history.flush([]);

    service.updateStatus(5, false).subscribe();
    const status = http.expectOne('/api/turmas/5/status');
    expect(status.request.method).toBe('PATCH');
    expect(status.request.body).toEqual({ active: false });
    status.flush({});
  });
});
