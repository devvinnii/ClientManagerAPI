package com.devvinnii.clientmanager.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }

        return checkDigit(cpf, 9) == Character.getNumericValue(cpf.charAt(9))
                && checkDigit(cpf, 10) == Character.getNumericValue(cpf.charAt(10));
    }

    private int checkDigit(String cpf, int length) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += Character.getNumericValue(cpf.charAt(index)) * (length + 1 - index);
        }
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
