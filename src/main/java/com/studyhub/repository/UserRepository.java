package com.studyhub.repository;

import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import com.studyhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByUsernameAndIdNot(String username, Long id);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);

    List<User> findByRole(UserRole role);
}
