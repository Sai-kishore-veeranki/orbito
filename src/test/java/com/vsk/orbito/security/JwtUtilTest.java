package com.vsk.orbito.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // inject private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration",
                86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration",
                604800000L);

        testUser = new User(
                "sai@orbito.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"))
        );
    }

    @Test
    @DisplayName("generateToken — returns non-null token")
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("extractUsername — returns correct email")
    void extractUsername_returnsCorrectEmail() {
        String token = jwtUtil.generateToken(testUser);
        String email = jwtUtil.extractUsername(token);
        assertThat(email).isEqualTo("sai@orbito.com");
    }

    @Test
    @DisplayName("isTokenValid — valid token returns true")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid — wrong user returns false")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtUtil.generateToken(testUser);

        UserDetails otherUser = new User(
                "other@orbito.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"))
        );

        assertThat(jwtUtil.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("generateRefreshToken — different from access token")
    void generateRefreshToken_differentFromAccessToken() {
        String accessToken  = jwtUtil.generateToken(testUser);
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }
}