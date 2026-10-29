package com.example.oauth.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.csrf(CsrfConfigurer<HttpSecurity> csrf-> csrf.disable())
                .cors((CorsConfigurer<HttpSecurity> cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(AuthorizationManagerRequestMatcherRegistry auth -> auth
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login((OAuth2LoginConfigurer<HttpSecurity> oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:5173/dashboard")
                        .failureUrl("http://localhost:5173/login?error = true")
                        .userInfoEndpoint((userInfoEndpointConfig userInfo -> userInfo
                                .userService(OAuth2UserService())))
        .logout(LogoutConfigurer<HttpSecurity> logout -> logout
                .logoutSuccessUrl("http://localhost:5173")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")):

        return http.build();

        }

        @Bean
        public corsConfigurationSource corsConfigurationSource(){

            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
            corsConfiguration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
            corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
            corsConfiguration.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

            source.registerCorsConfiguration(pattern: "/**" corsConfiguration);
            return source;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest , OAuth2User> OAuth2UserService(){
        return new DefaultOAuth2UserService();
    }









}
