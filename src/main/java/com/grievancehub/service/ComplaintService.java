package com.grievancehub.service;

import com.grievancehub.entity.Complaint;
import com.grievancehub.entity.User;
import com.grievancehub.repository.ComplaintRepository;
import com.grievancehub.repository.UserRepository;
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
    private UserRepository userRepository;

    // Save a new complaint submitted by a logged-in user
    public void saveComplaint(String title, String description, String location, MultipartFile imageFile, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        String imagePath = null;
        if (!imageFile.isEmpty()) {
            try {
                // ✅ Save inside "uploads" directory inside /static folder


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
complaint.setImagePath(imagePath);
        complaint.setStatus("Pending");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setUser(user);

        complaintRepository.save(complaint);
    }

    // Get all complaints submitted by the logged-in user
    public List<Complaint> getUserComplaints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return complaintRepository.findByUser(user);
    }
}
