package com.vsk.orbito.security;

import com.vsk.orbito.entity.User;
import com.vsk.orbito.enums.Role;
import com.vsk.orbito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId  = oAuth2User.getAttribute("sub");
        String email     = oAuth2User.getAttribute("email");
        String name      = oAuth2User.getAttribute("name");
        String picture   = oAuth2User.getAttribute("picture");

        log.info("OAuth2 login attempt for email: {}", email);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // update Google fields if they logged in with Google before
            user.setGoogleId(googleId);
            user.setProfilePicture(picture);
            user.setProvider("GOOGLE");
            userRepository.save(user);
            log.info("Existing user logged in via Google: {}", email);
        } else {
            // new user — auto register with DEVELOPER role
            User newUser = User.builder()
                    .name(name)
                    .email(email)
                    .googleId(googleId)
                    .profilePicture(picture)
                    .provider("GOOGLE")
                    .role(Role.DEVELOPER)
                    .isActive(true)
                    .build();
            userRepository.save(newUser);
            log.info("New user registered via Google: {}", email);
        }

        return oAuth2User;
    }
}