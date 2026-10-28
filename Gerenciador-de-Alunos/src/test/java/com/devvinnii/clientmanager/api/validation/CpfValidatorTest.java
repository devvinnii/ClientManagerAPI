package com.devvinnii.clientmanager.api.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void acceptsValidCpf() {
        assertThat(validator.isValid("52998224725", null)).isTrue();
    }

    @Test
    void rejectsInvalidCpfCheckDigits() {
        assertThat(validator.isValid("52998224724", null)).isFalse();
    }

    @Test
    void rejectsRepeatedDigits() {
        assertThat(validator.isValid("11111111111", null)).isFalse();
    }

    @Test
    void rejectsNonNumericCpf() {
        assertThat(validator.isValid("529.982.247-25", null)).isFalse();
    }
}
