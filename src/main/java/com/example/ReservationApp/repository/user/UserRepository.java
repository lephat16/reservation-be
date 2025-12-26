package com.example.ReservationApp.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ReservationApp.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
