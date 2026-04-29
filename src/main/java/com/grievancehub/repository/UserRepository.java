package com.grievancehub.repository;

import com.grievancehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    java.util.List<User> findByEmail(String email);
}
