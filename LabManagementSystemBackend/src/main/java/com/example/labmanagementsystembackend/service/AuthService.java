package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.domain.entity.User;
import com.example.labmanagementsystembackend.dto.response.LoginResponse;
import com.example.labmanagementsystembackend.dto.response.UserResponse;
import com.example.labmanagementsystembackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public LoginResponse login(String username) {
        User user = userMapper.findByName(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        String token = UUID.randomUUID().toString();
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus());
        return new LoginResponse(token, "Bearer", 3600, response);
    }
}
