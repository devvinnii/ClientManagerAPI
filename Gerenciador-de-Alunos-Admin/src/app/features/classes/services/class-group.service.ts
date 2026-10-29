import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ClassGroup,
  ClassGroupPage,
  ClassGroupQuery,
  ClassGroupUpsert,
  Enrollment,
} from '../models/class-group.model';

@Injectable({ providedIn: 'root' })
export class ClassGroupService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiBaseUrl}/turmas`;

  list(query: ClassGroupQuery): Observable<ClassGroupPage> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sortBy', query.sortBy)
      .set('sortDirection', query.sortDirection);
    if (query.name?.trim()) params = params.set('name', query.name.trim());
    if (query.modality) params = params.set('modality', query.modality);
    if (query.active !== undefined) params = params.set('active', query.active);
    return this.http.get<ClassGroupPage>(this.endpoint, { params });
  }

  getById(id: number): Observable<ClassGroup> {
    return this.http.get<ClassGroup>(`${this.endpoint}/${id}`);
  }

  create(value: ClassGroupUpsert): Observable<ClassGroup> {
    return this.http.post<ClassGroup>(this.endpoint, this.toRequest(value));
  }

  update(id: number, value: ClassGroupUpsert): Observable<ClassGroup> {
    return this.http.put<ClassGroup>(`${this.endpoint}/${id}`, this.toRequest(value));
  }

  updateStatus(id: number, active: boolean): Observable<ClassGroup> {
    return this.http.patch<ClassGroup>(`${this.endpoint}/${id}/status`, { active });
  }

  getEnrollments(classGroupId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.endpoint}/${classGroupId}/matriculas`);
  }

  enroll(classGroupId: number, studentId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(`${this.endpoint}/${classGroupId}/matriculas`, { studentId });
  }

  cancelEnrollment(classGroupId: number, enrollmentId: number): Observable<Enrollment> {
    return this.http.delete<Enrollment>(
      `${this.endpoint}/${classGroupId}/matriculas/${enrollmentId}`,
    );
  }

  reenroll(classGroupId: number, enrollmentId: number): Observable<Enrollment> {
    return this.http.post<Enrollment>(
      `${this.endpoint}/${classGroupId}/matriculas/${enrollmentId}/rematricula`,
      {},
    );
  }

  private toRequest(value: ClassGroupUpsert): Omit<ClassGroupUpsert, 'active'> {
    return {
      name: value.name.trim(),
      modality: value.modality,
      description: value.description?.trim(),
      responsible: value.responsible.trim(),
      maxCapacity: value.maxCapacity,
      schedules: value.schedules,
    };
  }
}
