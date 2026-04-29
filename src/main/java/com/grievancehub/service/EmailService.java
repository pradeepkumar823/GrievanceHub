package com.grievancehub.service;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // --- HTML Email Templates ---
    private String getHtmlTemplate(String title, String contentHtml) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 0; }" +
               "  .wrapper { padding: 40px 20px; text-align: center; background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%); }" +
               "  .container { max-width: 600px; margin: 0 auto; background-color: rgba(30, 41, 59, 0.75); border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 16px; padding: 40px; text-align: left; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }" +
               "  .header { text-align: center; margin-bottom: 30px; }" +
               "  .logo { font-size: 24px; font-weight: 800; color: #f8fafc; text-decoration: none; }" +
               "  .logo span { background: linear-gradient(135deg, #FF6B6B 0%, #FFA07A 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }" +
               "  .title { font-size: 20px; font-weight: 700; color: #ffffff; margin-bottom: 20px; border-bottom: 2px solid #FF6B6B; padding-bottom: 10px; }" +
               "  .data-box { background: rgba(15, 23, 42, 0.5); border-radius: 12px; padding: 20px; margin: 20px 0; border: 1px solid rgba(255,255,255,0.05); }" +
               "  .data-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }" +
               "  .data-row:last-child { border-bottom: none; }" +
               "  .label { color: #94a3b8; font-weight: 600; font-size: 14px; }" +
               "  .value { color: #e2e8f0; font-weight: 700; font-size: 14px; text-align: right; }" +
               "  .footer { text-align: center; margin-top: 30px; color: #64748b; font-size: 12px; line-height: 1.5; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='wrapper'>" +
               "  <div class='container'>" +
               "    <div class='header'><a class='logo'>Grievance<span>Hub</span></a></div>" +
               "    <div class='title'>" + title + "</div>" +
               "    " + contentHtml + "" +
               "    <div class='footer'>This is an automated system email. Please do not reply directly.<br/>GrievanceHub Admin Portal</div>" +
               "  </div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    @Async
    public void sendComplaintCreatedEmail(User user, Complaint complaint) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String content = "<p>Dear " + user.getName() + ",</p>" +
                             "<p>Your grievance application has been recorded successfully. A security summary is provided below:</p>" +
                             "<div class='data-box'>" +
                             "  <div class='data-row'><div class='label'>Tracking ID</div><div class='value' style='color:#818cf8;'>" + complaint.getTrackingId() + "</div></div>" +
                             "  <div class='data-row'><div class='label'>Title</div><div class='value'>" + complaint.getTitle() + "</div></div>" +
                             "  <div class='data-row'><div class='label'>Department</div><div class='value'>" + complaint.getDepartment() + "</div></div>" +
                             "  <div class='data-row'><div class='label'>Priority</div><div class='value' style='color:#fbbf24;'>" + complaint.getPriority() + "</div></div>" +
                             "</div>" +
                             "<p>You will receive status alerts iteratively.</p>";

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Complaint Lodged Correctly: " + complaint.getTrackingId());
            helper.setText(getHtmlTemplate("Complaint Registered", content), true);

            mailSender.send(message);
            System.out.println("✅ HTML Email dispatched perfectly to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ SMTP delivery failure: " + e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdateEmail(User user, Complaint complaint) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String color = "#fbbf24"; // pending
            if ("RESOLVED".equalsIgnoreCase(complaint.getStatus())) color = "#34d399";
            if ("REJECTED".equalsIgnoreCase(complaint.getStatus())) color = "#f87171";

            String content = "<p>Dear " + user.getName() + ",</p>" +
                             "<p>The state of your submitted grievance has shifted.</p>" +
                             "<div class='data-box'>" +
                             "  <div class='data-row'><div class='label'>Tracking ID</div><div class='value'>" + complaint.getTrackingId() + "</div></div>" +
                             "  <div class='data-row'><div class='label'>Status</div><div class='value' style='color:" + color + "; text-transform:uppercase;'>" + complaint.getStatus() + "</div></div>" +
                             "</div>" +
                             "<p><strong>Administrator Note:</strong></p>" +
                             "<p style='background:rgba(255,255,255,0.05); padding:15px; border-radius:8px; color:#cbd5e1; font-style:italic;'>" + 
                             (complaint.getAdminReply() != null && !complaint.getAdminReply().isBlank() ? complaint.getAdminReply() : "No message provided.") + "</p>";

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Grievance Status Refreshed: " + complaint.getTrackingId());
            helper.setText(getHtmlTemplate("Status Update", content), true);

            mailSender.send(message);
            System.out.println("✅ Status upgrade email delivered to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Delivery breakdown: " + e.getMessage());
        }
    }

    @Async
    public void sendAdminEscalationAlert(String adminEmail, Complaint complaint) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String content = "<p style='color:#f87171;'><strong>URGENT SLA BREACH WARNING</strong></p>" +
                             "<p>A public query is over-indexed without closure.</p>" +
                             "<div class='data-box'>" +
                             "  <div class='data-row'><div class='label'>Tracking ID</div><div class='value'>" + complaint.getTrackingId() + "</div></div>" +
                             "  <div class='data-row'><div class='label'>Title</div><div class='value'>" + complaint.getTitle() + "</div></div>" +
                             "</div>";

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("🚨 Urgent Escalation: " + complaint.getTrackingId());
            helper.setText(getHtmlTemplate("SLA Violation Action Required", content), true);

            mailSender.send(message);
            System.out.println("✅ Operational warning posted correctly.");
        } catch (Exception e) {
            System.err.println("❌ Operational signal failure.");
        }
    }
}

