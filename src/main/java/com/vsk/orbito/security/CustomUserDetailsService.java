package com.vsk.orbito.security;

import com.vsk.orbito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // OAuth2 users have no password — use empty string
        String password = user.getPassword() != null
                ? user.getPassword()
                : "";

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                user.isActive(),
                true, true, true,
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()))
        );
    }
}
//```
//
//        ---
//
//        ## Step 9 — Run and test
//
//**1.** Run `OrbitoApplication` — watch console, should start with no errors.
//
//        **2.** Open this URL directly in your browser:
//        ```
//http://localhost:8080/oauth2/authorization/google