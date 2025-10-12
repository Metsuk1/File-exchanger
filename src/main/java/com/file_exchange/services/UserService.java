package com.file_exchange.services;

import com.file_exchange.dto.UserDto;
import com.file_exchange.entity.User;
import com.file_exchange.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public  List<UserDto> getAllUsers() {
       return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public  UserDto getUserById(Long id) {
      User user = repository.findById(id);
      if (user == null){
          throw new IllegalArgumentException("User with id " + id  +  " not found");
      }

      return toDto(user);
    }

    public  UserDto createUser(UserDto dto) {
        dto.validate();
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        repository.save(user);
        return toDto(user);
    }

    public  UserDto updateUser(Long id, UserDto dto) {
        User exist = repository.findById(id);
        if (exist == null) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        dto.validate();
        exist.setName(dto.getName());
        exist.setEmail(dto.getEmail());
        repository.save(exist);
        return toDto(exist);
    }

    public  UserDto patchUser(Long id, Map<String, Object> updates) {
        User user = repository.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        if (updates.containsKey("name")) {
            String name = (String) updates.get("name");
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            user.setName(name);
        }
        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            user.setEmail(email);
        }
        repository.save(user);

        return toDto(user);
    }

    public  void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        repository.delete(id);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
