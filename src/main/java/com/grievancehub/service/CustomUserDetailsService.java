package com.grievancehub.service;

import com.grievancehub.entity.User;
import com.grievancehub.entity.Officer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private OfficerService officerService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Try to find a standard User
        User user = userService.findByEmail(email);
        if (user != null) {
            String role = user.getRole();
            if (role == null || role.isBlank()) {
                role = "USER";
            }
            
            String storedPassword = (user.getPassword() == null || user.getPassword().isBlank())
                    ? "{noop}OAUTH2_ACCOUNT_NO_PASSWORD"
                    : user.getPassword();

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(storedPassword)
                    .roles(role)
                    .disabled(user.getEnabled() == null ? false : !user.getEnabled())
                    .build();
        }

        // 2. Try to find an Officer
        Officer officer = officerService.findByEmail(email);
        if (officer != null) {
            String role = officer.getRole();
            if (role == null || role.isBlank()) {
                role = "OFFICER"; // Ensure the role doesn't get messed up if missing
            } else if (role.startsWith("ROLE_")) {
                role = role.substring(5); // Spring requires roles without ROLE_ prefix in the builder
            }

            String storedPassword = (officer.getPassword() == null || officer.getPassword().isBlank())
                    ? "{noop}OAUTH2_ACCOUNT_NO_PASSWORD"
                    : officer.getPassword();

            return org.springframework.security.core.userdetails.User.builder()
                    .username(officer.getEmail())
                    .password(storedPassword)
                    .roles(role)
                    .disabled(officer.getEnabled() == null ? false : !officer.getEnabled())
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }
}
