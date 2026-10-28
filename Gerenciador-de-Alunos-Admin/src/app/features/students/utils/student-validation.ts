import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const cpfValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const cpf = String(control.value ?? '').replace(/\D/g, '');
  if (!cpf) {
    return null;
  }
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return { cpf: true };
  }

  const digits = cpf.split('').map(Number);
  const calculateDigit = (length: number): number => {
    const sum = digits.slice(0, length).reduce((total, digit, index) => {
      return total + digit * (length + 1 - index);
    }, 0);
    const remainder = (sum * 10) % 11;
    return remainder === 10 ? 0 : remainder;
  };

  return calculateDigit(9) === digits[9] && calculateDigit(10) === digits[10]
    ? null
    : { cpf: true };
};

export function digitsOnly(value: string, maximumLength: number): string {
  return value.replace(/\D/g, '').slice(0, maximumLength);
}
