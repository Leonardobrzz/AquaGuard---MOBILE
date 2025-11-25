package com.aqua.guard.monitoramento.core.config;

import com.aqua.guard.monitoramento.api.filter.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir endpoints de autenticação sem token
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/verificar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/solicitar-reset-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-senha").permitAll()
                        
                        // Permitir acesso ao H2 console em desenvolvimento
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Permitir arquivos estáticos se necessário
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        
                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions().disable()) // Para H2 console
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}