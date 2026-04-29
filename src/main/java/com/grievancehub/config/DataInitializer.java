package com.grievancehub.config;

import com.grievancehub.entity.User;
import com.grievancehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // Create default admin account if it doesn't already exist
        if (userService.findByEmail(adminEmail) == null) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userService.save(admin);
            System.out.println("✅ Default Admin Created: " + adminEmail);
        }
    }
}
