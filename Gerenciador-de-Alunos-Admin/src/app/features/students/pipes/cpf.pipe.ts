import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'cpf' })
export class CpfPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    const digits = value?.replace(/\D/g, '') ?? '';
    return digits.length === 11
      ? digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
      : value || 'Não informado';
  }
}
