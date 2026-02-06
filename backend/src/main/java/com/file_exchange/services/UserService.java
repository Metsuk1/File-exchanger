package com.file_exchange.services;

import com.file_exchange.dto.UserDto;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.utils.JwtUtil;

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
        if (user == null) {
            throw new IllegalArgumentException("user null");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = JwtUtil.generateToken(user.getId());
        return token;
    }
}
