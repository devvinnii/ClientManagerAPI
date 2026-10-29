import { Role } from '../../shared/models/role.model';

export type NavigationIcon =
  | 'dashboard'
  | 'students'
  | 'classes'
  | 'users'
  | 'audit'
  | 'settings'
  | 'reports';

export interface NavigationItem {
  label: string;
  route: string;
  icon: NavigationIcon;
  roles: readonly Role[];
  implemented: boolean;
}

export const navigationItems: readonly NavigationItem[] = [
  {
    label: 'Dashboard',
    route: '/dashboard',
    icon: 'dashboard',
    roles: ['ADMIN', 'CHEFE'],
    implemented: true,
  },
  {
    label: 'Alunos',
    route: '/students',
    icon: 'students',
    roles: ['ADMIN', 'CHEFE', 'FUNCIONARIO', 'OPERADOR'],
    implemented: true,
  },
  {
    label: 'Turmas',
    route: '/classes',
    icon: 'classes',
    roles: ['ADMIN', 'CHEFE', 'FUNCIONARIO', 'OPERADOR'],
    implemented: true,
  },
  {
    label: 'Usuários',
    route: '/users',
    icon: 'users',
    roles: ['ADMIN'],
    implemented: false,
  },
  {
    label: 'Funcionários',
    route: '/employees',
    icon: 'users',
    roles: ['CHEFE'],
    implemented: false,
  },
  {
    label: 'Auditoria',
    route: '/audit',
    icon: 'audit',
    roles: ['ADMIN'],
    implemented: false,
  },
  {
    label: 'Configurações',
    route: '/settings',
    icon: 'settings',
    roles: ['ADMIN'],
    implemented: false,
  },
  {
    label: 'Relatórios',
    route: '/reports',
    icon: 'reports',
    roles: ['CHEFE'],
    implemented: false,
  },
];
