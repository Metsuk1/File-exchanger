package com.file_exchange.unit.services;

import com.file_exchange.dto.UserDto;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserService tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void testRegisterSuccess() {
        // Arrange
        UserDto inputDto = new UserDto();
        inputDto.setName("John Doe");
        inputDto.setEmail("john@example.com");
        inputDto.setPassword("password123");

        UserDto savedDto = new UserDto();
        savedDto.setId(1L);
        savedDto.setName("John Doe");
        savedDto.setEmail("john@example.com");
        savedDto.setPassword("password123");

        when(userRepository.createUser(any(UserDto.class), anyString()))
                .thenReturn(savedDto);

        // Act
        UserDto result = userService.register(inputDto, "password123");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, times(1)).createUser(any(UserDto.class), eq("password123"));
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void testRegisterWithNullName() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setName(null);
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(dto, "password123");
        });
        verify(userRepository, never()).createUser(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when name is empty")
    void testRegisterWithEmptyName() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setName("   ");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(dto, "password123");
        });
        verify(userRepository, never()).createUser(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void testRegisterWithNullEmail() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail(null);
        dto.setPassword("password123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(dto, "password123");
        });
        verify(userRepository, never()).createUser(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when email format is invalid")
    void testRegisterWithInvalidEmail() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail("invalid-email");
        dto.setPassword("password123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(dto, "password123");
        });
        verify(userRepository, never()).createUser(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLoginUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findUserByEmail(email)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(email, password)
        );
        assertEquals("user null", exception.getMessage());
        verify(userRepository, times(1)).findUserByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testLoginIncorrectPassword() {
        // Arrange
        String email = "john@example.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail(email);
        userDto.setPassword(correctPassword);

        when(userRepository.findUserByEmail(email)).thenReturn(userDto);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(email, wrongPassword)
        );
        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository, times(1)).findUserByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void testLoginWithNullPassword() {
        // Arrange
        String email = "john@example.com";

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail(email);
        userDto.setPassword("password123");

        when(userRepository.findUserByEmail(email)).thenReturn(userDto);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(email, null);
        });
        verify(userRepository, times(1)).findUserByEmail(email);
    }
}
