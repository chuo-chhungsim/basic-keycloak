package com.example.keycloakdemo.repository;

import com.example.keycloakdemo.model.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {
    
    Optional<AppUser> findByUsername(String username);
    
    Optional<AppUser> findByEmail(String email);
    
    Optional<AppUser> findByKeycloakUserId(String keycloakUserId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByKeycloakUserId(String keycloakUserId);
}
