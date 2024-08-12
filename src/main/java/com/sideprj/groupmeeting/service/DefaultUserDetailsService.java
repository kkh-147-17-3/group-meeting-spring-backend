package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.DefaultUserDetails;
import com.sideprj.groupmeeting.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DefaultUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userId = Long.parseLong(username);
        return new DefaultUserDetails(
                userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다.")));

    }
}
