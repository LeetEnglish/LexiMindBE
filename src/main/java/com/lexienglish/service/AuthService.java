package com.lexienglish.service;

import com.lexienglish.dto.auth.*;
import com.lexienglish.entity.User;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.repository.UserRepository;
import com.lexienglish.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already registered");
        }

        // Create verification token
        String verificationToken = UUID.randomUUID().toString();

        // Create new user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .emailVerified(false)
                .verificationToken(verificationToken)
                .provider(User.AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationToken);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail().toLowerCase())
                .ifPresent(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    user.setPasswordResetToken(resetToken);
                    user.setPasswordResetExpires(LocalDateTime.now().plusHours(24));
                    userRepository.save(user);

                    emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
                    log.info("Password reset email sent to: {}", user.getEmail());
                });
        // Don't reveal if email exists or not for security
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getPasswordResetExpires() == null ||
                user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newAccessToken = tokenProvider.generateAccessToken(email);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInSeconds())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .emailVerified(user.isEmailVerified())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .build();
    }
}
