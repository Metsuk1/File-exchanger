package com.file_exchange.unit.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.file_exchange.security.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PasswordValidator tests")
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"StrongPass1", "MyPassword123", "Test1234", "Abcdefg1"})
    @DisplayName("Should accept valid passwords")
    void testValidPasswords(String password) {
        assertThatCode(() -> passwordValidator.validate(password)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject null password")
    void testNullPassword() {
        assertThatThrownBy(() -> passwordValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Short1", "Abc123", "1234567"})
    @DisplayName("Should reject passwords shorter than 8 characters")
    void testShortPasswords(String password) {
        assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"password123", "alllowercase1"})
    @DisplayName("Should reject passwords without uppercase")
    void testNoUppercase(String password) {
        assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one uppercase letter");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASSWORD123", "ALLUPPERCASE1"})
    @DisplayName("Should reject passwords without lowercase")
    void testNoLowercase(String password) {
        assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one lowercase letter");
    }

    @ParameterizedTest
    @ValueSource(strings = {"StrongPassword", "NoDigitsHere"})
    @DisplayName("Should reject passwords without digits")
    void testNoDigits(String password) {
        assertThatThrownBy(() -> passwordValidator.validate(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one digit");
    }
}
