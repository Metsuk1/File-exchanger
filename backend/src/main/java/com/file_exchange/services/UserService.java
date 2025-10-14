package com.file_exchange.services;

import com.file_exchange.dto.UserDto;
import com.file_exchange.entity.User;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.utils.JwtUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto register(UserDto dto, String password) {
        dto.validate();
        return userRepository.createUser(dto, password);
    }

    public String login(String email, String password) {
        UserDto user = userRepository.findUserByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return JwtUtil.generateToken(email);
    }
}
