export const roles = ['ADMIN', 'CHEFE', 'FUNCIONARIO', 'OPERADOR'] as const;

export type Role = (typeof roles)[number];

export const roleLabels: Record<Role, string> = {
  ADMIN: 'Administrador',
  CHEFE: 'Chefia',
  FUNCIONARIO: 'Funcionário',
  OPERADOR: 'Operador (legado)',
};

export function isRole(value: string): value is Role {
  return roles.includes(value as Role);
}
