package com.example.oauth.controller;

import com.example.oauth.entity.User;
import com.example.oauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login-success")
    public Map<String, Object> loginSuccess(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String registrationId = getProviderName(authentication);
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            // Ensure user is saved in the database
            String providerId = oAuth2User.getName();
            Optional<User> optionalUser = userRepository.findByProviderAndProviderId(registrationId, providerId);
            if (optionalUser.isEmpty()) {
                // Fallback: check by email if provider check didn't find a user
                if (email != null && !email.isEmpty()) {
                    optionalUser = userRepository.findByEmail(email);
                    if (optionalUser.isPresent()) {
                        // Update existing user with provider info
                        User existingUser = optionalUser.get();
                        existingUser.setProvider(registrationId);
                        existingUser.setProviderId(providerId);
                        userRepository.save(existingUser);
                    } else {
                        // Create new user
                        User user = new User();
                        user.setName(name);
                        user.setEmail(email);
                        user.setProvider(registrationId);
                        user.setProviderId(providerId);
                        userRepository.save(user);
                    }
                } else {
                    // Create new user with null email (e.g., GitHub without public email)
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setProvider(registrationId);
                    user.setProviderId(providerId);
                    userRepository.save(user);
                }
            }

            response.put("authenticated", true);
            response.put("provider", registrationId);
            response.put("name", name);
            response.put("email", email);
            if ("google".equals(registrationId)) {
                response.put("picture", oAuth2User.getAttribute("picture"));
            } else if ("github".equals(registrationId)) {
                response.put("picture", oAuth2User.getAttribute("avatar_url"));
                response.put("username", oAuth2User.getAttribute("login"));
            }
            response.put("redirectUrl", "http://localhost:5173/dashboard");
        } else {
            response.put("authenticated", false);
        }
        return response;
    }

    @GetMapping("/getcurrentuser")
    public Map<String, Object> getCurrentUser(Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String registrationId = getProviderName(authentication);

            response.put("authenticated", true);
            response.put("provider", registrationId);

            if (Objects.equals(registrationId, "google")) {
                response.put("name", oAuth2User.getAttribute("name"));
                response.put("email", oAuth2User.getAttribute("email"));
                response.put("picture", oAuth2User.getAttribute("picture"));
            } else if (Objects.equals(registrationId, "github")) {

                response.put("name", oAuth2User.getAttribute("name"));
                response.put("email", oAuth2User.getAttribute("email"));
                response.put("picture", oAuth2User.getAttribute("avatar_url"));
                response.put("username", oAuth2User.getAttribute("login"));
                response.put("bio", oAuth2User.getAttribute("bio"));
                response.put("company", oAuth2User.getAttribute("company"));
                response.put("location", oAuth2User.getAttribute("location"));
                response.put("blog", oAuth2User.getAttribute("blog"));
                response.put("publicrepos", oAuth2User.getAttribute("publicrepos"));
                response.put("followers", oAuth2User.getAttribute("followers"));
                response.put("following", oAuth2User.getAttribute("following"));

            }
        } else {
            response.put("authenticated", false);
        }
        return response;

    }

    private String getProviderName(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
            return authToken.getAuthorizedClientRegistrationId();
        }
        return null;
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "logout successfully");
        return response;
    }
}
