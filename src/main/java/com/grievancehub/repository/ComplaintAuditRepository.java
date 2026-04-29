package com.grievancehub.repository;

import com.grievancehub.entity.ComplaintAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintAuditRepository extends JpaRepository<ComplaintAudit, Long> {
    List<ComplaintAudit> findByComplaintIdOrderByTimestampDesc(Long complaintId);
}
