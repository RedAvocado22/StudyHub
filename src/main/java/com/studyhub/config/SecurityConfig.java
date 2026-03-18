package com.studyhub.config;

import com.studyhub.security.CustomAuthFailureHandler;
import com.studyhub.security.CustomAuthSuccessHandler;
import com.studyhub.security.CustomLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthFailureHandler authFailureHandler;
    private final CustomAuthSuccessHandler authSuccessHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/courses", "/courses/{id}",
                    "/blogs", "/blogs/{id}",
                    "/register", "/verify", "/resend-verification",
                    "/forgot-password", "/reset-password",
                    "/courses/{id}/enroll",
                        "/payment/payos/return", "/payment/payos/cancel",
                        "/payment/vnpay/return","/payment/payos/webhook",
                    "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()
                .requestMatchers("/admin/settings/**", "/admin/users/**")
                    .hasAuthority("ADMIN")
                .requestMatchers("/admin/posts/**")
                    .hasAnyAuthority("ADMIN", "MEMBER")
                .requestMatchers("/admin/enrollments/**")
                    .hasAnyAuthority("ADMIN", "MANAGER")
                .requestMatchers("/admin/**")
                    .hasAnyAuthority("ADMIN", "MANAGER")
                .requestMatchers(
                    "/profile/**", "/my-enrollments", "/my-enrollments/**",
                        "/enrollments/me", "/enrollments/me/**",
                    "/my-courses/**", "/learn/**", "/change-password"
                ).hasAnyAuthority("ADMIN", "MANAGER", "MEMBER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authSuccessHandler)
                .failureHandler(authFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
