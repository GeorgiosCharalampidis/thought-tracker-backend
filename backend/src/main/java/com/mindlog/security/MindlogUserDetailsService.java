package com.mindlog.security;

import com.mindlog.model.User;
import com.mindlog.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MindlogUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MindlogUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        User user = userRepository.findByUsername(normalizedIdentifier)
                .or(() -> userRepository.findByEmail(normalizedIdentifier.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getAuthority())))
                .build();
    }
}

