package com.grievancehub.config;

import com.grievancehub.entity.User;
import com.grievancehub.service.UserService;
import com.grievancehub.repository.ComplaintRepository;
import com.grievancehub.entity.Complaint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // If an admin user with this email doesn't exist, create one
        User existingAdmin = userService.findByEmail(adminEmail);
        if (existingAdmin == null) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setEnabled(true); // Ensured the admin account is active
            userService.save(admin);
            System.out.println("✅ Default Admin Created: " + adminEmail + " / [HIDDEN PASSWORD]");
        }

        // ── Auto-fix and Advanced Merge for Old Users ──
        java.util.List<User> allUsers = userService.getAllUsers();
        java.util.Set<String> seenEmails = new java.util.HashSet<>();

        for (User u : allUsers) {
            String originalEmail = u.getEmail();
            String normalizedEmail = userService.normalizeEmail(originalEmail);

            boolean isExactDuplicate = seenEmails.contains(normalizedEmail);
            seenEmails.add(normalizedEmail);

            if (isExactDuplicate || (originalEmail != null && !originalEmail.equals(normalizedEmail))) {
                try {
                    // Check if duplicate already exists or if we are the exact duplicate
                    User existingConflict = allUsers.stream()
                            .filter(other -> normalizedEmail.equals(userService.normalizeEmail(other.getEmail())) && !other.getId().equals(u.getId()))
                            .findFirst().orElse(null);

                    if (existingConflict != null) {
                        System.out.println("⚠️ Duplicate Identity Detected: " + originalEmail + " vs " + existingConflict.getEmail());
                        
                        // Decide which is the keeper (prioritize the one with the password from Form Registration)
                        User keeper = (u.getPassword() != null && !u.getPassword().isEmpty()) ? u : existingConflict;
                        User goner = (keeper.getId().equals(u.getId())) ? existingConflict : u;

                        // Transfer all complaints from the goner to the keeper to prevent data loss
                        java.util.List<Complaint> complaints = complaintRepository.findByUser(goner);
                        for (Complaint c : complaints) {
                            c.setUser(keeper);
                            complaintRepository.save(c);
                        }

                        // Delete the ghost/duplicate account
                        userService.deleteUserById(goner.getId());
                        System.out.println("✅ Identity Merged: Transferred data and deleted ghost account ID: " + goner.getId());

                        // Set the keeper to the correctly normalized email
                        keeper.setEmail(normalizedEmail);
                        userService.save(keeper);
                    } else if (!isExactDuplicate) {
                        // Safe to just rename
                        u.setEmail(normalizedEmail);
                        userService.save(u);
                        System.out.println("✅ Data Migration: Normalized old email: " + originalEmail + " -> " + normalizedEmail);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Data Migration Failed for " + originalEmail + ": " + e.getMessage());
                }
            }

            if (Boolean.FALSE.equals(u.getEnabled()) && !"ADMIN".equals(u.getRole())) {
                u.setEnabled(true);
                userService.save(u);
                System.out.println("✅ Auto-repaired old user account: " + u.getEmail());
            }
        }
    }
}
