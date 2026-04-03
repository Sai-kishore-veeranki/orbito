package com.vsk.orbito.security;

import com.vsk.orbito.enums.Role;
import com.vsk.orbito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email    = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");
        String picture  = oAuth2User.getAttribute("picture");

        log.info("OAuth2 login attempt: {}", email);

        userRepository.findByEmail(email).ifPresentOrElse(
                existingUser -> {
                    // user exists — update Google fields only
                    existingUser.setGoogleId(googleId);
                    existingUser.setProfilePicture(picture);
                    existingUser.setProvider("GOOGLE");
                    userRepository.save(existingUser);
                    log.info("Existing user via Google: {}", email);
                },
                () -> {
                    // new user — set every field explicitly
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .password(null)             // no password for OAuth2
                            .googleId(googleId)
                            .profilePicture(picture)
                            .provider("GOOGLE")         // explicit
                            .role(Role.DEVELOPER)       // default role
                            .isActive(true)             // explicit
                            .failedLoginAttempts(0)     // explicit
                            .build();
                    userRepository.save(newUser);
                    log.info("New user via Google: {}", email);
                }
        );

        return oAuth2User;
    }
}