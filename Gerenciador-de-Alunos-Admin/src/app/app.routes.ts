import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.page').then((component) => component.LoginPage),
    title: 'Entrar | Talentos do Capão',
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/admin-layout/admin-layout.component').then(
        (component) => component.AdminLayoutComponent,
      ),
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/home-redirect/home-redirect.page').then(
            (component) => component.HomeRedirectPage,
          ),
      },
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'CHEFE'] },
        loadComponent: () =>
          import('./features/dashboard/dashboard.page').then(
            (component) => component.DashboardPage,
          ),
        title: 'Dashboard | Talentos do Capão',
      },
      {
        path: 'students',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'CHEFE', 'FUNCIONARIO', 'OPERADOR'] },
        loadComponent: () =>
          import('./features/students/students.page').then(
            (component) => component.StudentsPage,
          ),
        title: 'Alunos | Talentos do Capão',
      },
      {
        path: 'classes',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'CHEFE', 'FUNCIONARIO', 'OPERADOR'] },
        loadComponent: () =>
          import('./features/classes/classes.page').then(
            (component) => component.ClassesPage,
          ),
        title: 'Turmas | Talentos do Capão',
      },
      {
        path: 'forbidden',
        loadComponent: () =>
          import('./features/errors/forbidden.page').then(
            (component) => component.ForbiddenPage,
          ),
        title: 'Acesso negado | Talentos do Capão',
      },
    ],
  },
  {
    path: '**',
    loadComponent: () =>
      import('./features/errors/not-found.page').then((component) => component.NotFoundPage),
    title: 'Página não encontrada | Talentos do Capão',
  },
];
