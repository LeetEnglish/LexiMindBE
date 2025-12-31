package com.lexienglish.controller;

import com.lexienglish.dto.user.ChangePasswordRequest;
import com.lexienglish.dto.user.UpdateProfileRequest;
import com.lexienglish.dto.user.UserProfileResponse;
import com.lexienglish.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
