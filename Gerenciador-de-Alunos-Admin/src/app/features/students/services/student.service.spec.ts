import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Student, StudentPage } from '../models/student.model';
import { StudentService } from './student.service';

describe('StudentService', () => {
  let service: StudentService;
  let http: HttpTestingController;

  const student: Student = {
    id: 1,
    name: 'Ana Silva',
    email: 'ana@example.org',
    cpf: '52998224725',
    phone: '11999999999',
    photoUrl: 'foto.png',
    active: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(StudentService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists students with supported pagination, filtering and ordering parameters', () => {
    let response: StudentPage | undefined;
    service
      .list({
        name: 'Ana',
        cpf: '52998224725',
        active: true,
        page: 2,
        size: 20,
        sortBy: 'name',
        sortDirection: 'DESC',
      })
      .subscribe((page) => (response = page));

    const request = http.expectOne((candidate) => candidate.url === '/api/clients/page');
    expect(request.request.params.get('name')).toBe('Ana');
    expect(request.request.params.get('cpf')).toBe('52998224725');
    expect(request.request.params.get('active')).toBe('true');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.get('sortBy')).toBe('name');
    expect(request.request.params.get('sortDirection')).toBe('DESC');

    const page: StudentPage = {
      content: [student],
      totalElements: 41,
      totalPages: 3,
      page: 2,
      size: 20,
      sortBy: 'name',
      sortDirection: 'DESC',
    };
    request.flush(page);
    expect(response).toEqual(page);
  });

  it('loads one student by id', () => {
    service.getById(1).subscribe((result) => expect(result).toEqual(student));

    const request = http.expectOne('/api/clients/1');
    expect(request.request.method).toBe('GET');
    request.flush(student);
  });

  it('creates a student using multipart form data', () => {
    const photo = new File(['image'], 'ana.png', { type: 'image/png' });
    service
      .create({
        name: ' Ana Silva ',
        email: ' ana@example.org ',
        cpf: '529.982.247-25',
        phone: '(11) 99999-9999',
        photo,
      })
      .subscribe((result) => expect(result).toEqual(student));

    const request = http.expectOne('/api/clients');
    const body = request.request.body as FormData;
    expect(request.request.method).toBe('POST');
    expect(body.get('name')).toBe('Ana Silva');
    expect(body.get('email')).toBe('ana@example.org');
    expect(body.get('cpf')).toBe('52998224725');
    expect(body.get('phone')).toBe('11999999999');
    const uploadedPhoto = body.get('photo') as File;
    expect(uploadedPhoto.name).toBe('ana.png');
    expect(uploadedPhoto.type).toBe('image/png');
    expect(uploadedPhoto.size).toBe(photo.size);
    request.flush(student);
  });

  it('updates a student without sending absent optional fields', () => {
    service.update(1, { name: 'Ana', cpf: '52998224725' }).subscribe();

    const request = http.expectOne('/api/clients/1');
    const body = request.request.body as FormData;
    expect(request.request.method).toBe('PUT');
    expect(body.has('email')).toBeFalse();
    expect(body.has('phone')).toBeFalse();
    expect(body.has('photo')).toBeFalse();
    request.flush(student);
  });

  it('updates status and deletes using the existing endpoints', () => {
    service.updateStatus(1, false).subscribe();
    const statusRequest = http.expectOne('/api/clients/1/active');
    expect(statusRequest.request.method).toBe('PATCH');
    expect(statusRequest.request.body).toEqual({ active: false });
    statusRequest.flush({ ...student, active: false });

    service.delete(1).subscribe((message) => expect(message).toContain('excluído'));
    const deleteRequest = http.expectOne('/api/clients/1');
    expect(deleteRequest.request.method).toBe('DELETE');
    expect(deleteRequest.request.responseType).toBe('text');
    deleteRequest.flush('O aluno Ana Silva foi excluído com sucesso!');
  });

  it('loads protected photos as blobs and reuses the request', () => {
    let first: Blob | undefined;
    let second: Blob | undefined;
    service.loadPhoto('foto segura.png').subscribe((blob) => (first = blob));
    service.loadPhoto('foto segura.png').subscribe((blob) => (second = blob));

    const request = http.expectOne('/api/clients/photos/foto%20segura.png');
    expect(request.request.responseType).toBe('blob');
    const photo = new Blob(['photo'], { type: 'image/png' });
    request.flush(photo);

    expect(first).toBe(photo);
    expect(second).toBe(photo);
  });
});
