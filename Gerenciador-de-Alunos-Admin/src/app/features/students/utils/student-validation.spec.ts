import { FormControl } from '@angular/forms';
import { cpfValidator, digitsOnly } from './student-validation';

describe('student validation', () => {
  it('accepts a valid CPF', () => {
    expect(cpfValidator(new FormControl('52998224725'))).toBeNull();
  });

  it('rejects invalid and repeated CPFs', () => {
    expect(cpfValidator(new FormControl('52998224724'))).toEqual({ cpf: true });
    expect(cpfValidator(new FormControl('11111111111'))).toEqual({ cpf: true });
  });

  it('normalizes numeric fields and respects their maximum length', () => {
    expect(digitsOnly('(11) 99999-9999', 11)).toBe('11999999999');
    expect(digitsOnly('123.456.789-000', 11)).toBe('12345678900');
  });
});
