import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, shareReplay, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Student, StudentPage, StudentQuery, StudentUpsert } from '../models/student.model';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiBaseUrl}/clients`;
  private readonly photoCache = new Map<string, Observable<Blob>>();

  list(query: StudentQuery): Observable<StudentPage> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sortBy', query.sortBy)
      .set('sortDirection', query.sortDirection);

    if (query.name?.trim()) {
      params = params.set('name', query.name.trim());
    }
    if (query.cpf?.trim()) {
      params = params.set('cpf', query.cpf.trim());
    }
    if (query.active !== undefined) {
      params = params.set('active', query.active);
    }

    return this.http.get<StudentPage>(`${this.endpoint}/page`, { params });
  }

  getById(id: number): Observable<Student> {
    return this.http.get<Student>(`${this.endpoint}/${id}`);
  }

  create(student: StudentUpsert): Observable<Student> {
    return this.http.post<Student>(this.endpoint, this.toFormData(student)).pipe(
      tap(() => this.photoCache.clear()),
    );
  }

  update(id: number, student: StudentUpsert): Observable<Student> {
    return this.http.put<Student>(`${this.endpoint}/${id}`, this.toFormData(student)).pipe(
      tap(() => this.photoCache.clear()),
    );
  }

  updateStatus(id: number, active: boolean): Observable<Student> {
    return this.http.patch<Student>(`${this.endpoint}/${id}/active`, { active });
  }

  delete(id: number): Observable<string> {
    return this.http.delete(`${this.endpoint}/${id}`, { responseType: 'text' }).pipe(
      tap(() => this.photoCache.clear()),
    );
  }

  loadPhoto(fileName: string): Observable<Blob> {
    const cached = this.photoCache.get(fileName);
    if (cached) {
      return cached;
    }

    const request = this.http
      .get(`${this.endpoint}/photos/${encodeURIComponent(fileName)}`, { responseType: 'blob' })
      .pipe(shareReplay({ bufferSize: 1, refCount: true }));
    this.photoCache.set(fileName, request);
    return request;
  }

  private toFormData(student: StudentUpsert): FormData {
    const formData = new FormData();
    formData.append('name', student.name.trim());
    formData.append('cpf', student.cpf.replace(/\D/g, ''));

    if (student.email?.trim()) {
      formData.append('email', student.email.trim());
    }
    if (student.phone?.trim()) {
      formData.append('phone', student.phone.replace(/\D/g, ''));
    }
    if (student.photo) {
      formData.append('photo', student.photo, student.photo.name);
    }

    return formData;
  }
}
