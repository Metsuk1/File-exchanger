package com.file_exchange.services;

import com.file_exchange.dto.UserDto;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.security.PasswordEncoder;
import com.file_exchange.security.PasswordValidator;
import com.file_exchange.utils.JwtUtil;

/**
 *  User business logic (registration, authentication)
 */
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public UserService(UserRepository userRepository) {
        this(userRepository, new PasswordEncoder(), new PasswordValidator());
    }

    public UserService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    public UserDto register(UserDto dto, String password) {
        dto.validate();
        passwordValidator.validate(password);

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        UserDto savedUser = userRepository.save(dto, hashedPassword);

        savedUser.setPassword(null);
        return savedUser;
    }

    public String login(String email, String password) {
        UserDto user = userRepository.findUserByEmail(email);
        String storedHash = (user != null) ? user.getPassword() : null;

        boolean passwordValid = passwordEncoder.matchesSafely(password, storedHash);

        if (!passwordValid) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return JwtUtil.generateToken(user.getId());
    }

    public UserDto getProfile(Long userId) {
        UserDto user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    public UserDto updateProfile(Long userId, String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        dto.validate();

        if (userRepository.existsByEmailAndNotId(email, userId)) {
            throw new IllegalArgumentException("Email is already taken by another user");
        }

        userRepository.updateUser(userId, name, email);
        return getProfile(userId);
    }
}
