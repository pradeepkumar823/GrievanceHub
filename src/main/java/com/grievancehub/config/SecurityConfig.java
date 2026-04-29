package com.grievancehub.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.grievancehub.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    private static final String[] PUBLIC_MATCHERS = {
            "/", "/home", "/register", "/login", "/css/**", "/js/**", "/images/**", "/uploads/**", "/oauth2/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "OFFICER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler())
                        .permitAll()
                )
                // support OAuth2 / Google login using custom linkage
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(loginSuccessHandler()) // Exact same router as form login!
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isOfficer = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_OFFICER"));
                    
            if (isAdmin || isOfficer) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/home");
            }
        };
    }
}
