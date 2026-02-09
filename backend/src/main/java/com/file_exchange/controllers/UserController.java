package com.file_exchange.controllers;

import com.file_exchange.annotations.*;
import com.file_exchange.dto.UserDto;
import com.file_exchange.services.UserService;
import com.file_exchange.utils.JwtUtil;

@CustomRestController
@CustomRequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @CustomPostMapping("/register")
    public UserDto register(@CustomRequestBody UserDto dto) {
        return userService.register(dto, dto.getPassword());
    }

    @CustomPostMapping("/login")
    public String login(@CustomRequestBody UserDto dto) {
        String token = userService.login(dto.getEmail(), dto.getPassword());
        return token;
    }

    @CustomGetMapping("/profile")
    public UserDto getProfile(@CustomRequestHeader("Authorization") String auth) {
        Long userId = JwtUtil.extractUserId(auth);
        return userService.getProfile(userId);
    }

    @CustomPutMapping("/profile")
    public UserDto updateProfile(@CustomRequestHeader("Authorization") String auth, @CustomRequestBody UserDto dto) {
        Long userId = JwtUtil.extractUserId(auth);
        return userService.updateProfile(userId, dto.getName(), dto.getEmail());
    }
}
