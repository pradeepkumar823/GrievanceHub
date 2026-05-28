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
        email = email.trim().toLowerCase();
        if (email.endsWith("@gmail.com") || email.endsWith("@googlemail.com")) {
            String[] parts = email.split("@");
            String localPart = parts[0].replace(".", "");
            return localPart + "@" + parts[1];
        }
        return email;
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
        return userRepository.findByEmail(normalizeEmail(email)).orElse(null);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
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
