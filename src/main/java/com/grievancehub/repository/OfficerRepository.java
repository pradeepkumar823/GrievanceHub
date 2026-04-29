package com.grievancehub.repository;

import com.grievancehub.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {
    List<Officer> findByEmail(String email);
}
