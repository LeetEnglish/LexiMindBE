package com.lexienglish.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@lexienglish.com}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Verify your LexiMind account");
            message.setText(buildVerificationEmailBody(name, token));

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage());
            // In development, log the verification link
            log.info("DEV MODE - Verification link: {}/auth/verify?token={}", frontendUrl, token);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Reset your LexiMind password");
            message.setText(buildPasswordResetEmailBody(name, token));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", to, e.getMessage());
            // In development, log the reset link
            log.info("DEV MODE - Password reset link: {}/auth/reset-password?token={}", frontendUrl, token);
        }
    }

    private String buildVerificationEmailBody(String name, String token) {
        return String.format("""
                Hi %s,

                Welcome to LexiMind! Please verify your email address by clicking the link below:

                %s/auth/verify?token=%s

                This link will expire in 24 hours.

                If you didn't create an account, please ignore this email.

                Best regards,
                The LexiMind Team
                """, name, frontendUrl, token);
    }

    private String buildPasswordResetEmailBody(String name, String token) {
        return String.format("""
                Hi %s,

                We received a request to reset your password. Click the link below to set a new password:

                %s/auth/reset-password?token=%s

                This link will expire in 24 hours.

                If you didn't request a password reset, please ignore this email.

                Best regards,
                The LexiMind Team
                """, name, frontendUrl, token);
    }
}
