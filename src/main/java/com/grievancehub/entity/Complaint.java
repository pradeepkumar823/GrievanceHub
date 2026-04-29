package com.grievancehub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingId;

    private String title;
    private String department;
    private String priority;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String location;
    private String state;
    private String city;

    @Column(name = "image_path")
    private String imagePath;

    // Feature 2: Geospatial Map Tracking
    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private Integer feedbackRating;

    // Phase 4: Upvoting & Auto-Escalation
    @Column(nullable = false)
    private int upvoteCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "complaint_upvotes",
        joinColumns = @JoinColumn(name = "complaint_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private java.util.Set<User> upvoters = new java.util.HashSet<>();

    @ManyToOne
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ Add this constructor
    public Complaint() {}

    // Optional: parameterized constructor if needed
    public Complaint(Long id,
                     User user,
                     String title,
                     String description,
                     String status,
                     String location,
                     String state,
                     String city,
                     String trackingId,
                     String department,
                     String priority,
                     Integer feedbackRating,
                     String imagePath,
                     LocalDateTime createdAt,
                     LocalDateTime updatedAt
                     ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.imagePath=imagePath;
        this.location=location;
        this.state = state;
        this.city = city;
        this.trackingId = trackingId;
        this.department = department;
        this.priority = priority;
        this.feedbackRating = feedbackRating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // + getter/setter
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public Integer getFeedbackRating() { return feedbackRating; }
    public void setFeedbackRating(Integer feedbackRating) { this.feedbackRating = feedbackRating; }

    public int getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(int upvoteCount) { this.upvoteCount = upvoteCount; }

    public java.util.Set<User> getUpvoters() { return upvoters; }
    public void setUpvoters(java.util.Set<User> upvoters) { this.upvoters = upvoters; }
}
