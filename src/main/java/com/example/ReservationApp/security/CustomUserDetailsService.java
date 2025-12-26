package com.example.ReservationApp.security;



import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security用のUserDetailsService実装。
 * データベースからユーザー情報を取得し、AuthUserオブジェクトを返。
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
    
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("メールアドレスが間違っています。"));
        return new AuthUser(user);
    }

    
}
