package com.lexienglish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "OAuth2 authentication endpoints")
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id:}")
    private String facebookClientId;

    @GetMapping("/providers")
    @Operation(summary = "Get available OAuth2 providers")
    public ResponseEntity<Map<String, Object>> getProviders() {
        return ResponseEntity.ok(Map.of(
                "google", Map.of(
                        "enabled", !googleClientId.isEmpty(),
                        "authUrl", "/oauth2/authorization/google"),
                "facebook", Map.of(
                        "enabled", !facebookClientId.isEmpty(),
                        "authUrl", "/oauth2/authorization/facebook")));
    }
}
