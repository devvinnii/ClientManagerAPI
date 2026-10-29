import { Student } from '../../students/models/student.model';

export type Modality = 'NATACAO' | 'TRIATHLON' | 'BALLET';
export type ClassWeekday =
  | 'SEGUNDA'
  | 'TERCA'
  | 'QUARTA'
  | 'QUINTA'
  | 'SEXTA'
  | 'SABADO'
  | 'DOMINGO';
export type EnrollmentStatus = 'ATIVA' | 'CANCELADA';
export type ClassSortField =
  | 'id'
  | 'name'
  | 'modality'
  | 'responsible'
  | 'maxCapacity'
  | 'active'
  | 'createdAt';
export type SortDirection = 'ASC' | 'DESC';

export interface ClassSchedule {
  id?: number;
  dayOfWeek: ClassWeekday;
  startTime: string;
  endTime: string;
}

export interface ClassGroup {
  id: number;
  name: string;
  modality: Modality;
  description: string | null;
  responsible: string;
  maxCapacity: number;
  occupiedSpots: number;
  availableSpots: number;
  active: boolean;
  schedules: ClassSchedule[];
  createdAt: string;
  updatedAt: string;
}

export interface ClassGroupPage {
  content: ClassGroup[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}

export interface ClassGroupQuery {
  name?: string;
  modality?: Modality;
  active?: boolean;
  page: number;
  size: number;
  sortBy: ClassSortField;
  sortDirection: SortDirection;
}

export interface ClassGroupUpsert {
  name: string;
  modality: Modality;
  description?: string;
  responsible: string;
  maxCapacity: number;
  active: boolean;
  schedules: ClassSchedule[];
}

export interface Enrollment {
  id: number;
  studentId: number;
  studentName: string;
  studentCpf: string;
  studentPhotoUrl: string | null;
  studentActive: boolean;
  classGroupId: number;
  classGroupName: string;
  modality: Modality;
  enrolledAt: string;
  status: EnrollmentStatus;
  cancelledAt: string | null;
  updatedAt: string;
}

export interface StudentCandidate extends Student {}

export const MODALITIES: readonly { value: Modality; label: string }[] = [
  { value: 'NATACAO', label: 'Natação' },
  { value: 'TRIATHLON', label: 'Triathlon' },
  { value: 'BALLET', label: 'Ballet' },
];

export const WEEKDAYS: readonly { value: ClassWeekday; label: string }[] = [
  { value: 'SEGUNDA', label: 'Segunda-feira' },
  { value: 'TERCA', label: 'Terça-feira' },
  { value: 'QUARTA', label: 'Quarta-feira' },
  { value: 'QUINTA', label: 'Quinta-feira' },
  { value: 'SEXTA', label: 'Sexta-feira' },
  { value: 'SABADO', label: 'Sábado' },
  { value: 'DOMINGO', label: 'Domingo' },
];

export function modalityLabel(modality: Modality): string {
  return MODALITIES.find((item) => item.value === modality)?.label ?? modality;
}

export function weekdayLabel(day: ClassWeekday): string {
  return WEEKDAYS.find((item) => item.value === day)?.label ?? day;
}
