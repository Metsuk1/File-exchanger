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
        System.out.println("Login attempt: " + email + " with password: " + password);
        UserDto user = userRepository.findUserByEmail(email);
        if (user == null) {
            System.out.println("User not found in DB");
            throw new IllegalArgumentException("user null");
        }
        System.out.println("User found: " + user.getId() + ", " + user.getEmail());
        if(!user.getPassword().equals(password)){
            System.out.println("Password mismatch");
            throw new IllegalArgumentException("Invalid password");
        }

        String token = JwtUtil.generateToken(user.getId());
        System.out.println("Token generated: " + token);
        return token;
    }
}
