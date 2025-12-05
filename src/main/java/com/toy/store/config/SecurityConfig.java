package com.toy.store.config;

import com.toy.store.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
        @Autowired
        UserDetailsServiceImpl userDetailsService;

        @Autowired
        com.toy.store.security.services.AdminUserDetailsService adminUserDetailsService;

        @Bean
        public DaoAuthenticationProvider userAuthenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public DaoAuthenticationProvider adminAuthenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(adminUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @org.springframework.core.annotation.Order(1)
        public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/admin/**")
                                .authenticationProvider(adminAuthenticationProvider()) // Use Admin Provider
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**")
                                                .permitAll()
                                                .anyRequest().hasRole("ADMIN"))
                                .formLogin(form -> form
                                                .loginPage("/admin/login")
                                                .loginProcessingUrl("/admin/login-submit")
                                                .defaultSuccessUrl("/admin", true)
                                                .failureUrl("/admin/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/admin/logout")
                                                .logoutSuccessUrl("/admin/login?logout")
                                                .permitAll())
                                .exceptionHandling(e -> e.accessDeniedPage("/admin/login"))
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        @org.springframework.core.annotation.Order(2)
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authenticationProvider(userAuthenticationProvider()) // Use User Provider
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/register", "/login", "/products/**",
                                                                "/mystery-box/**", "/css/**", "/js/**",
                                                                "/error", "/h2-console/**")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/")
                                                .permitAll())
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                                .headers(headers -> headers.frameOptions(frameOption -> frameOption.sameOrigin()));

                return http.build();
        }
}
