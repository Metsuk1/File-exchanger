package com.file_exchange;

import com.file_exchange.dto.UserDto;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class  UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new UserRepository());
    }

    @Test
    void createUser() {
        UserDto dto = new UserDto();
        dto.setName("Conor");
        dto.setEmail("conor@mail.com");

        UserDto result = userService.createUser(dto);
        assertNotNull(result.getId());
        assertEquals("Conor",result.getName());
        assertEquals("conor@mail.com",result.getEmail());
    }

    @Test
    void testCreateUserInvalidEmail() {
        UserDto dto = new UserDto();
        dto.setName("John Jones");
        dto.setEmail("invalid");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        UserDto dto1 = new UserDto();
        dto1.setName("Conor");
        dto1.setEmail("conor@mail.com");
        userService.createUser(dto1);

        UserDto dto2 = new UserDto();
        dto2.setName("John Jones");
        dto2.setEmail("john@mail.com");
        userService.createUser(dto2);

        UserDto dto3 = new UserDto();
        dto3.setName("Khabib");
        dto3.setEmail("khabib@mail.com");
        userService.createUser(dto3);

        List<UserDto> users = userService.getAllUsers();

        assertEquals(3, users.size());
        assertEquals("Conor", users.get(0).getName());
        assertEquals("John Jones", users.get(1).getName());
    }

    @Test
    void testGetUserById() {
        UserDto dto = new UserDto();
        dto.setName("Conor");
        dto.setEmail("conor@mail.com");
        UserDto created = userService.createUser(dto);

        UserDto result = userService.getUserById(created.getId());
        assertEquals(created.getId(), result.getId());
        assertEquals("Conor", result.getName());
    }

    @Test
    void testGetUserByIdNotFound() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.getUserById(777L));
        assertEquals("User with id 777 not found", exception.getMessage());
    }

    @Test
    void testUpdateUser() {
        UserDto dto = new UserDto();
        dto.setName("Conor");
        dto.setEmail("conor@mail.com");
        UserDto created = userService.createUser(dto);

        UserDto updateDto = new UserDto();
        updateDto.setName("John");
        updateDto.setEmail("john@mail.com");

        UserDto updated = userService.updateUser(created.getId(), updateDto);
        assertEquals("John", updated.getName());
        assertEquals("john@mail.com", updated.getEmail());

        UserDto fetched = userService.getUserById(created.getId());
        assertEquals("John", fetched.getName());
    }

    @Test
    void testPatchUser() {
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        UserDto created = userService.createUser(dto);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched John");

        UserDto patched = userService.patchUser(created.getId(), updates);
        assertEquals("Patched John", patched.getName());
        assertEquals("john@example.com", patched.getEmail());

        UserDto fetched = userService.getUserById(created.getId());
        assertEquals("Patched John", fetched.getName());
    }

    @Test
    void testDeleteUser() {
        UserDto dto = new UserDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        UserDto created = userService.createUser(dto);

        userService.deleteUser(created.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.getUserById(created.getId()));
        assertEquals("User with id " + created.getId() + " not found", exception.getMessage());
    }
}
