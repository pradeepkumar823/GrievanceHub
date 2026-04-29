package com.grievancehub.controller;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import com.grievancehub.service.ComplaintService;
import com.grievancehub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.grievancehub.service.PdfService pdfService;

    @GetMapping("/new")
    public String showComplaintForm() {
        return "create-complaint";
    }

    @PostMapping
    public String submitComplaint(@RequestParam String title,
                                  @RequestParam String description,
                                  @RequestParam String location,
                                  @RequestParam String state,
                                  @RequestParam String city,
                                  @RequestParam String department,
                                  @RequestParam String priority,
                                  @RequestParam(required = false) Double latitude,
                                  @RequestParam(required = false) Double longitude,
                                  @RequestParam(value = "image", required = false) MultipartFile image,
                                  Principal principal,
                                  Model model) {

        try {
            String email = extractUserEmail(principal);
            complaintService.saveComplaint(title, description, location, state, city, department, priority, latitude, longitude, image, email);
            model.addAttribute("success", "Complaint submitted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to submit complaint: " + e.getMessage());
        }

        return "create-complaint";
    }

    // Phase 4: Public Transparency Board
    @GetMapping("/explore")
    public String explorePublicComplaints(Model model, Principal principal) {
        List<Complaint> all = complaintService.getAllComplaints();
        // Filter out sensitive/rejected/closed data. Order dynamically by upvotes.
        List<Complaint> publicBoard = all.stream()
                .filter(c -> c.getStatus() == null || (!c.getStatus().equalsIgnoreCase("Rejected") && !c.getStatus().equalsIgnoreCase("Closed")))
                .sorted((c1, c2) -> Integer.compare(c2.getUpvoteCount(), c1.getUpvoteCount())) // Highest upvotes first
                .collect(java.util.stream.Collectors.toList());
        
        // Authenticate reader context for detecting personal votes natively
        if (principal != null) {
            String email = extractUserEmail(principal);
            User currentUser = userService.findByEmail(email);
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
            }
        }
        
        model.addAttribute("complaints", publicBoard);
        return "explore";
    }

    @PostMapping("/{id}/upvote")
    public String upvoteComplaint(@PathVariable Long id, Principal principal, @RequestHeader(value = "Referer", required = false) String referer) {
        if (principal == null) return "redirect:/login"; // Must securely have account to prevent duplicate bots
        String email = extractUserEmail(principal);
        complaintService.toggleUpvote(id, email);
        return "redirect:" + (referer != null ? referer : "/complaints/explore");
    }

    @GetMapping("/my")
    public String viewMyComplaints(Model model, Principal principal) {
        String email = extractUserEmail(principal);
        List<Complaint> complaints = complaintService.getUserComplaints(email);
        model.addAttribute("complaints", complaints);
        return "my-complaints";
    }

    @PostMapping("/{id}/feedback")
    public String submitFeedback(@PathVariable Long id, @RequestParam Integer rating, Model model) {
        try {
            complaintService.submitFeedback(id, rating);
            return "redirect:/complaints/my?feedbackSuccess=true";
        } catch (Exception e) {
            return "redirect:/complaints/my?feedbackError=true";
        }
    }

    @GetMapping("/{id}/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadMyPdf(@PathVariable Long id, Principal principal) {
        try {
            String email = extractUserEmail(principal);
            Complaint c = complaintService.getComplaintById(id);
            
            // Security Check: Ensure the logged-in user owns this complaint
            if (!c.getUser().getEmail().equals(email)) {
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            
            byte[] pdfBytes = pdfService.generateComplaintPdf(c);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "My_Grievance_Report_" + c.getTrackingId() + ".pdf");
            
            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/rti-pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadRtiPdf(@PathVariable Long id, Principal principal) {
        try {
            String email = extractUserEmail(principal);
            Complaint c = complaintService.getComplaintById(id);
            
            if (!c.getUser().getEmail().equals(email)) {
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            
            byte[] pdfBytes = pdfService.generateRtiPdf(c);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "RTI_Application_" + c.getTrackingId() + ".pdf");
            
            return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //  Extract email from Google or normal login
    private String extractUserEmail(Principal principal) {
        String email;
        if (principal instanceof OAuth2AuthenticationToken oauth) {
            email = oauth.getPrincipal().getAttributes().get("email").toString();
        } else {
            email = principal.getName();
        }
        return userService.normalizeEmail(email);
    }
}
