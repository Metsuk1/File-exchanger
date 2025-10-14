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

    @CustomPostMapping("/register")
    public UserDto register(@CustomRequestBody UserDto dto, @CustomRequestParam("password") String password) {
        return userService.register(dto, password);
    }

    @CustomPostMapping("/login")
    public Map<String, String> login(@CustomRequestParam("email") String email,
                                     @CustomRequestParam("password") String password) {
        return Map.of("token", userService.login(email, password));
    }
}
