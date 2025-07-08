package com.grievancehub.repository;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUser(User user);
}
