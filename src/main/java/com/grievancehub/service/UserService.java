package com.grievancehub.service;

import com.grievancehub.entity.User;
import com.grievancehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void deleteUserById(Long id) {
        userRepository.deleteById(java.util.Objects.requireNonNull(id));
    }
}
