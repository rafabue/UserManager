package com.nayax.usermanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Configuração de segurança da aplicação.
 *
 * <p>São duas cadeias separadas para que o {@code permitAll()} dos arquivos estáticos fique
 * confinado a uma cadeia que nunca processa requisições da API.</p>
 */
@Configuration
public class SecurityConfig {

    private final String adminUsername;
    private final String adminPassword;

    public SecurityConfig(@Value("${app.admin.username}") String adminUsername,
                          @Value("${app.admin.password}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /**
     * Cadeia de filtros da API REST.
     *
     * @param http builder de configuração
     * @return cadeia aplicada a {@code /api/**}
     * @throws Exception se a configuração for inválida
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    /**
     * Cadeia de filtros da interface web e da documentação.
     *
     * @param http builder de configuração
     * @return cadeia aplicada a tudo que não seja {@code /api/**}
     * @throws Exception se a configuração for inválida
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/assets/**", "/static/**").permitAll()
                        .requestMatchers("/login", "/users/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .build();
    }

    /**
     * Usuário que autentica as chamadas da API.
     *
     * @param passwordEncoder codificador usado para armazenar a senha em memória
     * @return gerenciador de usuários em memória
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build());
    }

    /**
     * Codificador da senha da aplicação.
     *
     * @return instancia de BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
