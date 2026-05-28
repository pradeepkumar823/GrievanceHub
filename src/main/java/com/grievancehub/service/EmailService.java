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

    @Value("${app.resend.api.key:}")
    private String resendApiKey;

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

    // Master Dispatcher (Switches automatically between local SMTP and production Resend HTTP API)
    private void dispatchEmail(String to, String subject, String contentHtml) {
        if (resendApiKey != null && !resendApiKey.trim().isEmpty() && !resendApiKey.equalsIgnoreCase("placeholder")) {
            sendEmailViaResend(to, subject, contentHtml);
        } else {
            sendEmailViaSmtp(to, subject, contentHtml);
        }
    }

    // Method A: Direct HTTP POST over port 443 (Never blocked by cloud firewalls/free-tiers!)
    private void sendEmailViaResend(String to, String subject, String contentHtml) {
        try {
            java.net.URL url = new java.net.URL("https://api.resend.com/emails");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + resendApiKey.trim());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Escape strings for JSON safely
            String escapedSubject = subject.replace("\"", "\\\"");
            String escapedHtml = contentHtml.replace("\"", "\\\"").replace("\n", "").replace("\r", "");

            String jsonPayload = "{"
                    + "\"from\":\"GrievanceHub <onboarding@resend.dev>\","
                    + "\"to\":[\"" + to + "\"],"
                    + "\"subject\":\"" + escapedSubject + "\","
                    + "\"html\":\"" + escapedHtml + "\""
                    + "}";

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == 200 || code == 201) {
                System.out.println("✅ Email dispatched perfectly via Resend HTTP API to " + to);
            } else {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.err.println("❌ Resend API response error (status " + code + "): " + response.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Resend API delivery failure: " + e.getMessage());
        }
    }

    // Method B: Standard SMTP Mail Sender (Best for Local testing where ports are open)
    private void sendEmailViaSmtp(String to, String subject, String contentHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(contentHtml, true);

            mailSender.send(message);
            System.out.println("✅ Email dispatched perfectly via SMTP to " + to);
        } catch (Exception e) {
            System.err.println("❌ SMTP delivery failure: " + e.getMessage());
        }
    }

    @Async
    public void sendComplaintCreatedEmail(User user, Complaint complaint) {
        String content = "<p>Dear " + user.getName() + ",</p>" +
                         "<p>Your grievance application has been recorded successfully. A security summary is provided below:</p>" +
                         "<div class='data-box'>" +
                         "  <div class='data-row'><div class='label'>Tracking ID</div><div class='value' style='color:#818cf8;'>" + complaint.getTrackingId() + "</div></div>" +
                         "  <div class='data-row'><div class='label'>Title</div><div class='value'>" + complaint.getTitle() + "</div></div>" +
                         "  <div class='data-row'><div class='label'>Department</div><div class='value'>" + complaint.getDepartment() + "</div></div>" +
                         "  <div class='data-row'><div class='label'>Priority</div><div class='value' style='color:#fbbf24;'>" + complaint.getPriority() + "</div></div>" +
                         "</div>" +
                         "<p>You will receive status alerts iteratively.</p>";

        String html = getHtmlTemplate("Complaint Registered", content);
        dispatchEmail(user.getEmail(), "Complaint Lodged Correctly: " + complaint.getTrackingId(), html);
    }

    @Async
    public void sendStatusUpdateEmail(User user, Complaint complaint) {
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

        String html = getHtmlTemplate("Status Update", content);
        dispatchEmail(user.getEmail(), "Grievance Status Refreshed: " + complaint.getTrackingId(), html);
    }

    @Async
    public void sendAdminEscalationAlert(String adminEmail, Complaint complaint) {
        String content = "<p style='color:#f87171;'><strong>URGENT SLA BREACH WARNING</strong></p>" +
                         "<p>A public query is over-indexed without closure.</p>" +
                         "<div class='data-box'>" +
                         "  <div class='data-row'><div class='label'>Tracking ID</div><div class='value'>" + complaint.getTrackingId() + "</div></div>" +
                         "  <div class='data-row'><div class='label'>Title</div><div class='value'>" + complaint.getTitle() + "</div></div>" +
                         "</div>";

        String html = getHtmlTemplate("SLA Violation Action Required", content);
        dispatchEmail(adminEmail, "🚨 Urgent Escalation: " + complaint.getTrackingId(), html);
    }

    @Async
    public void sendUserRegistrationEmail(User user) {
        String content = "<p>Dear " + user.getName() + ",</p>" +
                         "<p>Welcome to GrievanceHub! Your account has been registered successfully.</p>" +
                         "<div class='data-box'>" +
                         "  <div class='data-row'><div class='label'>Username / Email</div><div class='value'>" + user.getEmail() + "</div></div>" +
                         "  <div class='data-row'><div class='label'>Role</div><div class='value' style='color:#34d399;'>CITIZEN</div></div>" +
                         "</div>" +
                         "<p>You can now log in, lodge grievances, and track updates in real-time.</p>";

        String html = getHtmlTemplate("Registration Confirmed", content);
        dispatchEmail(user.getEmail(), "Welcome to GrievanceHub - Registration Successful!", html);
    }
}
