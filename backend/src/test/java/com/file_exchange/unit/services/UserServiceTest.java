package com.file_exchange.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.file_exchange.dto.UserDto;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.security.PasswordEncoder;
import com.file_exchange.security.PasswordValidator;
import com.file_exchange.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("UserService tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, passwordValidator);
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void testRegisterSuccess() {
        // Arrange
        UserDto inputDto = new UserDto();
        inputDto.setName("John Doe");
        inputDto.setEmail("john@example.com");

        UserDto savedDto = new UserDto();
        savedDto.setId(1L);
        savedDto.setName("John Doe");
        savedDto.setEmail("john@example.com");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("hashedPassword");
        when(userRepository.save(any(UserDto.class), eq("hashedPassword"))).thenReturn(savedDto);
        doNothing().when(passwordValidator).validate(anyString());

        // Act
        UserDto result = userService.register(inputDto, "StrongPass123");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertNull(result.getPassword()); // Password should not be returned
        verify(passwordValidator).validate("StrongPass123");
        verify(passwordEncoder).encode("StrongPass123");
        verify(userRepository).save(any(UserDto.class), eq("hashedPassword"));
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void testRegisterWithNullName() {
        UserDto dto = new UserDto();
        dto.setName(null);
        dto.setEmail("john@example.com");

        assertThrows(IllegalArgumentException.class, () -> userService.register(dto, "StrongPass123"));
        verify(userRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void testRegisterWithEmptyName() {
        UserDto dto = new UserDto();
        dto.setName("   ");
        dto.setEmail("john@example.com");

        assertThrows(IllegalArgumentException.class, () -> userService.register(dto, "StrongPass123"));
        verify(userRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void testRegisterWithNullEmail() {
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail(null);

        assertThrows(IllegalArgumentException.class, () -> userService.register(dto, "StrongPass123"));
        verify(userRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when email format is invalid")
    void testRegisterWithInvalidEmail() {
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail("invalid-email");

        assertThrows(IllegalArgumentException.class, () -> userService.register(dto, "StrongPass123"));
        verify(userRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when password validation fails")
    void testRegisterWithInvalidPassword() {
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        doThrow(new IllegalArgumentException("Password must be at least 8 characters"))
                .when(passwordValidator)
                .validate("weak");

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> userService.register(dto, "weak"));
        assertEquals("Password must be at least 8 characters", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterWithExistingEmail() {
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail("existing@example.com");

        doNothing().when(passwordValidator).validate(anyString());
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> userService.register(dto, "StrongPass123"));
        assertEquals("User with this email already exists", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw generic error when user not found - prevents user enumeration")
    void testLoginUserNotFound() {
        String email = "nonexistent@example.com";
        String password = "StrongPass123";

        when(userRepository.findUserByEmail(email)).thenReturn(null);
        when(passwordEncoder.matchesSafely(password, null)).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> userService.login(email, password));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder).matchesSafely(password, null);
    }

    @Test
    @DisplayName("Should throw generic error when password is incorrect - prevents user enumeration")
    void testLoginIncorrectPassword() {
        String email = "john@example.com";
        String wrongPassword = "WrongPass456";
        String storedHash = "hashedPassword";

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail(email);
        userDto.setPassword(storedHash);

        when(userRepository.findUserByEmail(email)).thenReturn(userDto);
        when(passwordEncoder.matchesSafely(wrongPassword, storedHash)).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> userService.login(email, wrongPassword));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void testLoginWithNullPassword() {
        String email = "john@example.com";

        when(userRepository.findUserByEmail(email)).thenReturn(null);
        when(passwordEncoder.matchesSafely(null, null))
                .thenThrow(new IllegalArgumentException("Password cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> userService.login(email, null));
    }
}
