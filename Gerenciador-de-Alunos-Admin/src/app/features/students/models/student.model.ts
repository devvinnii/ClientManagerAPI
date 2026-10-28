export interface Student {
  id: number;
  name: string;
  email: string | null;
  cpf: string;
  phone: string | null;
  photoUrl: string | null;
  active: boolean;
}

export interface StudentPage {
  content: Student[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  sortBy: StudentSortField;
  sortDirection: StudentSortDirection;
}

export type StudentSortField = 'id' | 'name' | 'cpf' | 'email' | 'active';
export type StudentSortDirection = 'ASC' | 'DESC';

export interface StudentQuery {
  name?: string;
  cpf?: string;
  active?: boolean;
  page: number;
  size: number;
  sortBy: StudentSortField;
  sortDirection: StudentSortDirection;
}

export interface StudentUpsert {
  name: string;
  email?: string;
  cpf: string;
  phone?: string;
  photo?: File;
}
