package com.file_exchange.controllers;

import com.file_exchange.dto.UserDto;
import com.file_exchange.services.UserService;
import com.file_exchange.annotations.*;

import java.util.List;
import java.util.Map;

@CustomRestController
@CustomRequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @CustomGetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @CustomGetMapping("/{id}")
    public UserDto getUserById(@CustomPathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    @CustomPostMapping
    public UserDto createUser(@CustomRequestBody UserDto user) {
        return userService.createUser(user);
    }

    @CustomPutMapping("/{id}")
    public UserDto updateUser(@CustomPathVariable("id") Long id, @CustomRequestBody UserDto user) {
        return userService.updateUser(id, user);
    }

    @CustomPatchMapping("/{id}")
    public UserDto patchUser(@CustomPathVariable("id") Long id, @CustomRequestBody Map<String, Object> updates) {
        return userService.patchUser(id, updates);
    }

    @CustomDeleteMapping("/{id}")
    public void deleteUser(@CustomPathVariable("id") Long id) {
        userService.deleteUser(id);
    }
}
