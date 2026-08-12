package com.arcadia.user.service;

import com.arcadia.auth.dto.response.UserResponse;
import com.arcadia.auth.mapper.UserMapper;
import com.arcadia.common.exception.BadRequestException;
import com.arcadia.common.exception.NicknameAlreadyExistsException;
import com.arcadia.entity.User;
import com.arcadia.repository.UserRepository;
import com.arcadia.user.dto.request.ChangePasswordRequest;
import com.arcadia.user.dto.request.UpdateProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.nickname() != null && !request.nickname().isBlank()) {
            String nickname = request.nickname().trim();
            if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
                throw new NicknameAlreadyExistsException("Nickname already taken: " + nickname);
            }
            user.setNickname(nickname);
        }
        if (request.avatarUrl() != null) {
            String avatar = request.avatarUrl().isBlank() ? null : request.avatarUrl().trim();
            user.setAvatarUrl(avatar);
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
