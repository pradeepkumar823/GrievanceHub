package com.grievancehub.service;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendComplaintCreatedEmail(User user, Complaint complaint) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Complaint Received: " + complaint.getTitle());
            message.setText("Dear " + user.getName() + ",\n\n" +
                    "Your grievance has been successfully submitted. We are reviewing it and will get back to you soon.\n\n" +
                    "Complaint ID: " + complaint.getId() + "\n" +
                    "Title: " + complaint.getTitle() + "\n" +
                    "Department: " + complaint.getDepartment() + "\n\n" +
                    "Thank you,\n" +
                    "GrievanceHub Admin Team");

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email (Check SMTP config): " + e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdateEmail(User user, Complaint complaint) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Complaint Status Updated (#" + complaint.getId() + ")");
            message.setText("Dear " + user.getName() + ",\n\n" +
                    "The status of your grievance has been updated by the administration.\n\n" +
                    "Title: " + complaint.getTitle() + "\n" +
                    "New Status: " + complaint.getStatus() + "\n\n" +
                    "Admin Response: " + (complaint.getAdminReply() != null && !complaint.getAdminReply().isEmpty() ? complaint.getAdminReply() : "No message provided.") + "\n\n" +
                    "Login to GrievanceHub to view full details.\n\n" +
                    "Thank you,\n" +
                    "GrievanceHub Admin Team");

            mailSender.send(message);
            System.out.println("✅ Status update email sent to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send status email (Check SMTP config): " + e.getMessage());
        }
    }
    @Async
    public void sendAdminEscalationAlert(String adminEmail, Complaint complaint) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("🚨 URGENT SLA BREACH: Complaint #" + complaint.getId());
            message.setText("System Alert,\n\n" +
                    "A citizen grievance has been pending for over 15 days without resolution and has breached the Service Level Agreement (SLA).\n\n" +
                    "Action Required Immediately.\n\n" +
                    "Complaint ID: " + complaint.getId() + "\n" +
                    "Title: " + complaint.getTitle() + "\n" +
                    "Priority: CRITICAL\n\n" +
                    "Please log into the Admin Dashboard immediately to review.\n\n" +
                    "GrievanceHub Automated System");

            mailSender.send(message);
            System.out.println("✅ SLA Escalation warning sent to Admin: " + adminEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send SLA escalation email: " + e.getMessage());
        }
    }
}
