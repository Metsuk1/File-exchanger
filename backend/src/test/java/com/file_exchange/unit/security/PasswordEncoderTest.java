package com.file_exchange.unit.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PasswordEncoder tests")
class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
    }

    @Test
    @DisplayName("Should encode password")
    void testEncode() {
        String rawPassword = "MySecurePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(encoded).startsWith("$2a$"); // BCrypt prefix
    }

    @Test
    @DisplayName("Should match correct password")
    void testMatchesCorrectPassword() {
        String rawPassword = "MySecurePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Should not match incorrect password")
    void testMatchesIncorrectPassword() {
        String rawPassword = "MySecurePassword123";
        String wrongPassword = "WrongPassword456";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches(wrongPassword, encoded)).isFalse();
    }

    @Test
    @DisplayName("Should safely match when user exists and password correct")
    void testMatchesSafelyCorrect() {
        String rawPassword = "MySecurePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matchesSafely(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Should safely return false when user exists but password wrong")
    void testMatchesSafelyWrongPassword() {
        String rawPassword = "MySecurePassword123";
        String wrongPassword = "WrongPassword456";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matchesSafely(wrongPassword, encoded)).isFalse();
    }

    @Test
    @DisplayName("Should safely return false when user does not exist (null hash)")
    void testMatchesSafelyUserNotFound() {
        String rawPassword = "MySecurePassword123";

        // Should not throw exception and should return false
        assertThat(passwordEncoder.matchesSafely(rawPassword, null)).isFalse();
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void testDifferentHashesForSamePassword() {
        String rawPassword = "MySecurePassword123";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertThat(encoded1).isNotEqualTo(encoded2); // Different salts
        assertThat(passwordEncoder.matches(rawPassword, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, encoded2)).isTrue();
    }
}
