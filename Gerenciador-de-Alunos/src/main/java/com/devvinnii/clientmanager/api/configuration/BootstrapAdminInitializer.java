package com.devvinnii.clientmanager.api.configuration;

import com.devvinnii.clientmanager.api.model.AppUser;
import com.devvinnii.clientmanager.api.model.Role;
import com.devvinnii.clientmanager.api.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class BootstrapAdminInitializer {

    @Bean
    @ConditionalOnProperty(name = "app.security.bootstrap-admin.enabled", havingValue = "true")
    CommandLineRunner bootstrapAdmin(AppUserRepository repository, PasswordEncoder passwordEncoder,
                                    @Value("${app.security.bootstrap-admin.username}") String username,
                                    @Value("${app.security.bootstrap-admin.password}") String password) {
        return args -> {
            if (username.isBlank() || password.isBlank()) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_USERNAME e BOOTSTRAP_ADMIN_PASSWORD são obrigatórios");
            }
            if (repository.findByUsername(username).isEmpty()) {
                repository.save(AppUser.builder()
                        .username(username)
                        .passwordHash(passwordEncoder.encode(password))
                        .active(true)
                        .roles(Set.of(Role.ADMIN))
                        .build());
            }
        };
    }
}
