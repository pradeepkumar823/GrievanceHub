package com.grievancehub.service;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import com.grievancehub.repository.ComplaintRepository;
import com.grievancehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaEscalationService {

    private static final Logger logger = LoggerFactory.getLogger(SlaEscalationService.class);

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Run every night at midnight (00:00:00)
    // For testing purposes during development, you could change this to run every minute: @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void escalateOverdueComplaints() {
        logger.info("============== SLA ESCALATION SYSTEM ==============");
        logger.info("Scanning database for pending grievances older than 15 days...");

        LocalDateTime slaThreshold = LocalDateTime.now().minusDays(15);
        List<Complaint> overdueComplaints = complaintRepository.findByStatusIgnoreCaseAndCreatedAtBefore("Pending", slaThreshold);

        if (overdueComplaints.isEmpty()) {
            logger.info("Great news! Zero SLA breaches detected tonight.");
            logger.info("==================================================");
            return;
        }

        logger.warn("🚨 ALERT: Found " + overdueComplaints.size() + " overdue complaints breaching the SLA.");

        // Fetch all platform admins to notify them
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();

        for (Complaint complaint : overdueComplaints) {
            // Force status bump to prevent further ignoring
            complaint.setPriority("Critical");
            complaint.setStatus("ESCALATED"); 
            complaint.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);
            
            logger.warn("ESCALATED Complaint #" + complaint.getId() + " - " + complaint.getTitle());

            // Notify all admins rapidly via background task
            for (User admin : admins) {
                if(admin.getEmail() != null) {
                    emailService.sendAdminEscalationAlert(admin.getEmail(), complaint);
                }
            }
        }

        logger.info("All " + overdueComplaints.size() + " complaints have been forcibly escalated to CRITICAL.");
        logger.info("==================================================");
    }
}
