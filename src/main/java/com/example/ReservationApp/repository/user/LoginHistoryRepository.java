package com.example.ReservationApp.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ReservationApp.entity.user.LoginHistory;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(String userId);
}
