package com.grievancehub.service;

import com.grievancehub.entity.User;
import com.grievancehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String normalizeEmail(String email) {
        if (email == null)
            return null;
        email = email.trim().toLowerCase();
        if (email.endsWith("@gmail.com") || email.endsWith("@googlemail.com")) {
            String[] parts = email.split("@");
            String localPart = parts[0].replace(".", "");
            return localPart + "@" + parts[1];
        }
        return email;
    }

    // ✅ Register a new user manually (via form)
    public void registerUser(String name, String email, String password, String mobileNumber) {
        User user = new User();
        user.setName(name);
        user.setEmail(normalizeEmail(email));
        user.setPassword(passwordEncoder.encode(password)); // Encrypt password
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            user.setMobileNumber(mobileNumber.trim());
        }
        user.setRole("USER"); // Default role
        userRepository.save(user);
    }

    // ✅ Find user by email
    public User findByEmail(String email) {
        java.util.List<User> users = userRepository.findByEmail(normalizeEmail(email));
        return users.isEmpty() ? null : users.get(0);
    }

    // ✅ Check if email exists
    public boolean emailExists(String email) {
        java.util.List<User> users = userRepository.findByEmail(normalizeEmail(email));
        return !users.isEmpty();
    }

    // ✅ Save method (used by Google login auto-registration)
    public void save(User user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        // Note: Password encoding has been removed from this generic save method
        // to prevent double-encoding. Passwords must be encoded BEFORE calling save()
        // or handle entirely through registerUser()!

        // Assign default role if missing
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        userRepository.save(user);
    }



    // ✅ Get all users (for admin management)
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Delete a user by ID (admin only)
    public void deleteUserById(Long id) {
        userRepository.deleteById(java.util.Objects.requireNonNull(id));
    }

}
