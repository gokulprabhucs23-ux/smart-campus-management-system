package com.meridian.smartcampus.repository;

import com.meridian.smartcampus.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    java.util.List<User> findByRole(String role);
    Optional<User> findFirstByRole(String role);
    Optional<User> findFirstByRoleIgnoreCase(String role);
    Optional<User> findByEmail(String email);
    Optional<User> findByNumericId(Long numericId);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
}
