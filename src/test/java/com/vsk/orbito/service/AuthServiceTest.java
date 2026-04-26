package com.vsk.orbito.service;

import com.vsk.orbito.dto.request.LoginRequest;
import com.vsk.orbito.dto.request.RegisterRequest;
import com.vsk.orbito.dto.response.AuthResponse;
import com.vsk.orbito.entity.User;
import com.vsk.orbito.enums.Role;
import com.vsk.orbito.repository.UserRepository;
import com.vsk.orbito.security.CustomUserDetailsService;
import com.vsk.orbito.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Sai Kishore")
                .email("sai@orbito.com")
                .password("encoded_password")
                .role(Role.DEVELOPER)
                .provider("LOCAL")
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        testUserDetails = new org.springframework.security.core.userdetails.User(
                "sai@orbito.com",
                "encoded_password",
                List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"))
        );
    }

    // ─── REGISTER TESTS ───────────────────────────────────────

    @Test
    @DisplayName("register — success — returns JWT tokens")
    void register_success_returnsTokens() {
        // arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("Sai Kishore");
        request.setEmail("sai@orbito.com");
        request.setPassword("password123");
        request.setRole(Role.DEVELOPER);

        when(userRepository.existsByEmail("sai@orbito.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded_password");
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(userDetailsService.loadUserByUsername("sai@orbito.com"))
                .thenReturn(testUserDetails);
        when(jwtUtil.generateToken(testUserDetails))
                .thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(testUserDetails))
                .thenReturn("refresh_token");

        // act
        AuthResponse response = authService.register(request);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getEmail()).isEqualTo("sai@orbito.com");
        assertThat(response.getRole()).isEqualTo(Role.DEVELOPER);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("register — duplicate email — throws exception")
    void register_duplicateEmail_throwsException() {
        // arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("sai@orbito.com");

        when(userRepository.existsByEmail("sai@orbito.com"))
                .thenReturn(true);

        // act + assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        // verify save was never called
        verify(userRepository, never()).save(any());
    }

    // ─── LOGIN TESTS ──────────────────────────────────────────

    @Test
    @DisplayName("login — valid credentials — returns JWT tokens")
    void login_validCredentials_returnsTokens() {
        // arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("sai@orbito.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(null); // authentication succeeds
        when(userRepository.findByEmail("sai@orbito.com"))
                .thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("sai@orbito.com"))
                .thenReturn(testUserDetails);
        when(jwtUtil.generateToken(testUserDetails))
                .thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(testUserDetails))
                .thenReturn("refresh_token");

        // act
        AuthResponse response = authService.login(request);

        // assert
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getEmail()).isEqualTo("sai@orbito.com");
    }

    @Test
    @DisplayName("login — bad credentials — throws BadCredentialsException")
    void login_badCredentials_throwsException() {
        // arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("sai@orbito.com");
        request.setPassword("wrong_password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // act + assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}