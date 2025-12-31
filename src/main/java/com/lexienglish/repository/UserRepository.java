package com.lexienglish.repository;

import com.lexienglish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId);
}
