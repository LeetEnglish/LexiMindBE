package com.lexienglish.security.oauth2;

import com.lexienglish.entity.User;
import com.lexienglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user with OAuth info if they registered via email
            if (user.getProvider() == User.AuthProvider.LOCAL) {
                user.setProvider(User.AuthProvider.valueOf(registrationId.toUpperCase()));
                user.setProviderId(userInfo.getId());
                user.setEmailVerified(true); // OAuth providers verify email
            }
            user.setProfileImageUrl(userInfo.getImageUrl());
            user = userRepository.save(user);
        } else {
            // Register new user
            user = registerNewUser(registrationId, userInfo);
        }

        return new CustomOAuth2User(oAuth2User, user);
    }

    private User registerNewUser(String registrationId, OAuth2UserInfo userInfo) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .password("") // OAuth users don't have password
                .fullName(userInfo.getName())
                .emailVerified(true) // OAuth providers verify email
                .provider(User.AuthProvider.valueOf(registrationId.toUpperCase()))
                .providerId(userInfo.getId())
                .profileImageUrl(userInfo.getImageUrl())
                .build();

        return userRepository.save(user);
    }
}
