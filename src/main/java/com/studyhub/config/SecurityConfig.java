package com.studyhub.config;

import com.studyhub.security.CustomAuthFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthFailureHandler authFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/courses", "/courses/{id}",
                    "/blogs", "/blogs/{id}",
                    "/register", "/verify", "/resend-verification",
                    "/forgot-password", "/reset-password",
                    "/courses/{id}/enroll",
                    "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()
                .requestMatchers("/admin/settings/**", "/admin/users/**")
                    .hasAuthority("ADMIN")
                .requestMatchers("/admin/posts/**")
                    .hasAnyAuthority("ADMIN", "MARKETING")
                .requestMatchers("/admin/**")
                    .hasAnyAuthority("ADMIN", "MANAGER")
                .requestMatchers(
                    "/profile/**", "/my-enrollments/**",
                    "/my-courses/**", "/learn/**", "/change-password"
                ).hasAnyAuthority("ADMIN", "MANAGER", "MARKETING", "MEMBER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler(authFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
