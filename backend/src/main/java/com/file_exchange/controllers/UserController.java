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
    public String login(@CustomRequestBody UserDto dto) {
        System.out.println("Login request: email=" + dto.getEmail() + ", password=" + dto.getPassword());
        String token = userService.login(dto.getEmail(), dto.getPassword());
        System.out.println("Token generated: " + token);
        return token;
    }
}
