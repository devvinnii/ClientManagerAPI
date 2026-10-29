package com.devvinnii.clientmanager.api.configuration;

import com.devvinnii.clientmanager.api.security.JwtAuthenticationFilter;
import com.devvinnii.clientmanager.api.security.RestAccessDeniedHandler;
import com.devvinnii.clientmanager.api.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                            RestAuthenticationEntryPoint authenticationEntryPoint,
                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/clients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/clients/*/active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/turmas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/turmas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/turmas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/turmas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/turmas/**")
                        .hasAnyRole("ADMIN", "CHEFE", "FUNCIONARIO", "OPERADOR")
                        .requestMatchers("/api/clients/**", "/api/auth/me")
                        .hasAnyRole("ADMIN", "CHEFE", "FUNCIONARIO", "OPERADOR")
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
