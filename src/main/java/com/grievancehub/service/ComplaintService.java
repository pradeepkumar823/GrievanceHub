package com.grievancehub.service;
import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import com.grievancehub.entity.ComplaintAudit;
import com.grievancehub.repository.ComplaintRepository;
import com.grievancehub.repository.ComplaintAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ComplaintAuditRepository auditRepository;


    // Save a new complaint submitted by a logged-in user
    public void saveComplaint(String title, String description, String location, String state, String city, String department, String priority, Double latitude, Double longitude, MultipartFile imageFile, String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found: " + email);
        }

        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                //Save inside "uploads" directory inside /static folder


                //change with the new file where you want to store the image
                String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";

                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs(); // ✅ Create directory if not exists

                File destFile = new File(uploadDir + fileName);
                imageFile.transferTo(destFile);

                imagePath = "/uploads/" + fileName;  // this will be used in HTML <img>
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed: " + e.getMessage());
            }
        }
        Complaint complaint = new Complaint();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setLocation(location);
        complaint.setState(state);
        complaint.setCity(city);
        
        // Phase 6: Automated AI Keyword Triage
        if (department == null || department.equalsIgnoreCase("Auto-Detect (AI)") || department.trim().isEmpty()) {
            complaint.setDepartment(predictDepartment(title + " " + description));
        } else {
            complaint.setDepartment(department);
        }
        
        complaint.setPriority(priority);
        complaint.setLatitude(latitude);
        complaint.setLongitude(longitude);
        complaint.setImagePath(imagePath);
        
        // Generate Unique CPGRAMS style Tracking ID
        String uniqueHash = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String trackingId = "GRV-" + LocalDateTime.now().getYear() + "-" + uniqueHash;
        complaint.setTrackingId(trackingId);

        complaint.setStatus("Pending");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setUser(user);

        complaintRepository.save(complaint);
        
        
        // Trigger automated email silently in background
        emailService.sendComplaintCreatedEmail(user, complaint);
    }

    // Get all complaints submitted by the logged-in user
    public List<Complaint> getUserComplaints(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found: " + email);
        }
        return complaintRepository.findByUser(user);
    }

    // Phase 6: Core NLP Keyword Routing Algorithm
    private String predictDepartment(String textContext) {
        if (textContext == null || textContext.isBlank()) return "Other";
        
        String desc = textContext.toLowerCase();
        
        if (desc.contains("pothole") || desc.contains("road") || desc.contains("street") || desc.contains("asphalt") || desc.contains("highway") || desc.contains("traffic")) return "Roads & Highways";
        if (desc.contains("leak") || desc.contains("pipe") || desc.contains("water") || desc.contains("drain") || desc.contains("flood") || desc.contains("sewage")) return "Water Supply";
        if (desc.contains("garbage") || desc.contains("rubbish") || desc.contains("smell") || desc.contains("dump") || desc.contains("toilet") || desc.contains("waste") || desc.contains("trash") || desc.contains("sanitation")) return "Sanitation & Waste";
        if (desc.contains("power") || desc.contains("electricity") || desc.contains("outage") || desc.contains("wire") || desc.contains("shock") || desc.contains("pole") || desc.contains("light")) return "Electricity";
        if (desc.contains("bus") || desc.contains("train") || desc.contains("station") || desc.contains("fare") || desc.contains("transport")) return "Public Transport";
        if (desc.contains("hospital") || desc.contains("clinic") || desc.contains("doctor") || desc.contains("medicine") || desc.contains("health") || desc.contains("disease")) return "Health & Hospitals";
        if (desc.contains("school") || desc.contains("teacher") || desc.contains("student") || desc.contains("college") || desc.contains("education") || desc.contains("class")) return "Education";
        
        return "Other";
    }

    // Phase 4: Toggle Upvote logic implementation
    public void toggleUpvote(Long complaintId, String email) {
        User user = userService.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found: " + email);

        Complaint c = getComplaintById(complaintId);
        if (c.getUpvoters() == null) {
            c.setUpvoters(new java.util.HashSet<>());
        }

        boolean alreadyVoted = c.getUpvoters().stream().anyMatch(u -> u.getId().equals(user.getId()));

        if (alreadyVoted) {
            c.getUpvoters().removeIf(u -> u.getId().equals(user.getId()));
            c.setUpvoteCount(Math.max(0, c.getUpvoteCount() - 1));
        } else {
            c.getUpvoters().add(user);
            c.setUpvoteCount(c.getUpvoteCount() + 1);

            // Auto-Escalate Strategy: 10 unique citizens overrides SLA threshold instantly
            if (c.getUpvoteCount() >= 10 && !c.getStatus().equalsIgnoreCase("Resolved") && !c.getStatus().equalsIgnoreCase("Closed") && !c.getStatus().equalsIgnoreCase("Rejected")) {
                c.setPriority("Critical");
            }
        }
        complaintRepository.save(c);
    }

    // Return all complaints (for admin)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<Complaint> getComplaintsByDepartment(String department) {
        return complaintRepository.findByDepartment(department);
    }

    // Return single complaint by id
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(java.util.Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new RuntimeException("Complaint not found id: " + id));
    }

    // Update complaint status and optionally store admin reply
    public void updateComplaintStatus(Long id, String status, String adminReply, String adminEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found id: " + id));
        
        String oldStatus = complaint.getStatus() != null ? complaint.getStatus() : "Pending";
        
        complaint.setStatus(status);
        complaint.setUpdatedAt(LocalDateTime.now());
        if (adminReply != null && !adminReply.trim().isEmpty()) {
            complaint.setAdminReply(adminReply);
        }
        complaintRepository.save(complaint);
        
        // Phase 6: Core Immutable Audit Log Lock
        if (!oldStatus.equalsIgnoreCase(status)) {
            ComplaintAudit audit = new ComplaintAudit(complaint, adminEmail, oldStatus, status);
            auditRepository.save(audit);
        }

        // Notify user about the status update
        if (complaint.getUser() != null && complaint.getUser().getEmail() != null && !complaint.getUser().getEmail().isEmpty()) {
            emailService.sendStatusUpdateEmail(complaint.getUser(), complaint);
        }
    }
    
    // Fetch Audit history
    public List<ComplaintAudit> getAudits(Long complaintId) {
        return auditRepository.findByComplaintIdOrderByTimestampDesc(complaintId);
    }

    // Delete a complaint by ID (admin only)
    public void deleteComplaint(Long id) {
        complaintRepository.deleteById(java.util.Objects.requireNonNull(id));
    }

    // Submit user feedback
    public void submitFeedback(Long id, Integer rating) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found id: " + id));
        if ("Resolved".equalsIgnoreCase(complaint.getStatus()) || "Closed".equalsIgnoreCase(complaint.getStatus())) {
            complaint.setFeedbackRating(rating);
            complaintRepository.save(complaint);
        } else {
            throw new RuntimeException("Feedback can only be submitted for Resolved/Closed complaints.");
        }
    }

}
