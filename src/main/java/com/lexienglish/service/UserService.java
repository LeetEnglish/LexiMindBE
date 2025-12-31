package com.lexienglish.service;

import com.lexienglish.dto.user.ChangePasswordRequest;
import com.lexienglish.dto.user.UpdateProfileRequest;
import com.lexienglish.dto.user.UserProfileResponse;
import com.lexienglish.entity.User;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.exception.ResourceNotFoundException;
import com.lexienglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", email);

        return mapToProfileResponse(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);

        // OAuth users cannot change password
        if (user.getProvider() != User.AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot change password for OAuth accounts");
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", email);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.isEmailVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
