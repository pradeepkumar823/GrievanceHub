package com.grievancehub.controller;

import com.grievancehub.entity.Complaint;
import com.grievancehub.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/complaints")  // ✅ All routes start with /complaints
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    // Show the complaint form
    @GetMapping("/new")
    public String showComplaintForm() {
        return "create-complaint";  // Returns create-complaint.html
    }

    // Handle complaint form submission
    @PostMapping
    public String submitComplaint(@RequestParam String title,
                                  @RequestParam String description,
                                  @RequestParam String location,
                                  @RequestParam("image") MultipartFile image,
                                  Principal principal,
                                  Model model
                                  ) {


        try {
            complaintService.saveComplaint(title, description, location, image, principal.getName());
            model.addAttribute("success", "Complaint submitted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to submit complaint: " + e.getMessage());
        }
        // Redirect to "My Complaints" page
       // return "redirect:/complaints/my";

        // Redirect to "Create-Complaints" page
        return "create-complaint";
    }

    // Show all complaints submitted by the logged-in user
    @GetMapping("/my")
    public String viewMyComplaints(Model model, Principal principal) {
        List<Complaint> complaints = complaintService.getUserComplaints(principal.getName());
        model.addAttribute("complaints", complaints);
        return "my-complaints"; // Returns my-complaints.html
    }
}
