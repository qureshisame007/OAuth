package com.example.oauth.service;

import com.example.oauth.entity.User;
import com.example.oauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getName();
        String email = "";
        String name = "";

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else if ("github".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }

        // First check by provider + providerId (most reliable unique identifier)
        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(registrationId, providerId);
        if (optionalUser.isPresent()) {
            return oAuth2User;
        }

        // Fallback: check by email if provider check didn't find a user
        if (email != null && !email.isEmpty()) {
            optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                // Update existing user with provider info
                User existingUser = optionalUser.get();
                existingUser.setProvider(registrationId);
                existingUser.setProviderId(providerId);
                userRepository.save(existingUser);
                return oAuth2User;
            }
        }

        // Create new user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setProvider(registrationId);
        user.setProviderId(providerId);
        userRepository.save(user);

        return oAuth2User;
    }
}
