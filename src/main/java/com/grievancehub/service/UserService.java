package com.grievancehub.service;

import com.grievancehub.entity.User;
import com.grievancehub.entity.Complaint;
import com.grievancehub.repository.UserRepository;
import com.grievancehub.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public boolean emailsConceptuallyMatch(String email1, String email2) {
        if (email1 == null || email2 == null) return false;
        
        email1 = email1.trim().toLowerCase();
        email2 = email2.trim().toLowerCase();
        
        if (email1.equals(email2)) return true;
        
        // Gmail dot-equivalence and plus-addressing (+) check
        boolean isGmail1 = email1.endsWith("@gmail.com") || email1.endsWith("@googlemail.com");
        boolean isGmail2 = email2.endsWith("@gmail.com") || email2.endsWith("@googlemail.com");
        
        if (isGmail1 && isGmail2) {
            String local1 = email1.split("@")[0];
            String local2 = email2.split("@")[0];
            
            // Ignore everything after '+' symbol (Gmail sub-addressing alias)
            if (local1.contains("+")) {
                local1 = local1.split("\\+")[0];
            }
            if (local2.contains("+")) {
                local2 = local2.split("\\+")[0];
            }
            
            // Ignore all dots
            local1 = local1.replace(".", "");
            local2 = local2.replace(".", "");
            
            return local1.equals(local2);
        }
        
        return false;
    }

    public void registerUser(String name, String email, String password, String mobileNumber) {
        User user = new User();
        user.setName(name);
        user.setEmail(normalizeEmail(email));
        user.setPassword(passwordEncoder.encode(password));
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            user.setMobileNumber(mobileNumber.trim());
        }
        user.setRole("USER");
        userRepository.save(user);

        // Trigger welcome email in the background
        try {
            emailService.sendUserRegistrationEmail(user);
        } catch (Exception e) {
            System.err.println("❌ Welcome email dispatch failed: " + e.getMessage());
        }
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        String normalizedSearch = normalizeEmail(email);
        
        // First try to find by exact match (extremely fast)
        User exactMatch = userRepository.findByEmail(normalizedSearch).orElse(null);
        if (exactMatch != null) return exactMatch;
        
        // If not found, check for Gmail dot-equivalent matches
        if (normalizedSearch.endsWith("@gmail.com") || normalizedSearch.endsWith("@googlemail.com")) {
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                if (emailsConceptuallyMatch(u.getEmail(), normalizedSearch)) {
                    return u;
                }
            }
        }
        
        return null;
    }

    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    public void save(User user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new RuntimeException("User not found id: " + id));

        // 1. Nullify user field on all complaints created by this user
        List<Complaint> complaints = complaintRepository.findByUser(user);
        if (complaints != null) {
            for (Complaint c : complaints) {
                c.setUser(null);
                complaintRepository.save(c);
            }
        }

        // 2. Remove user from all upvotes sets across complaints
        List<Complaint> upvotedComplaints = complaintRepository.findAll();
        for (Complaint c : upvotedComplaints) {
            if (c.getUpvoters() != null) {
                boolean removed = c.getUpvoters().removeIf(u -> u.getId().equals(id));
                if (removed) {
                    c.setUpvoteCount(Math.max(0, c.getUpvoteCount() - 1));
                    complaintRepository.save(c);
                }
            }
        }

        complaintRepository.flush();

        // 3. Delete the user itself
        userRepository.delete(user);
    }
}
