package com.grievancehub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaint_status",     columnList = "status"),
        @Index(name = "idx_complaint_department", columnList = "department"),
        @Index(name = "idx_complaint_tracking",   columnList = "trackingId", unique = true)
})
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

    // Geospatial Map Tracking
    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private Integer feedbackRating;

    // Upvoting & Auto-Escalation
    @Column(nullable = false)
    private int upvoteCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "complaint_upvotes",
        joinColumns = @JoinColumn(name = "complaint_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> upvoters = new HashSet<>();

    @ManyToOne
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @JoinColumn(name = "user_id")
    private User user;

    // Normalize status to uppercase on set to ensure consistency
    public void setStatus(String status) {
        this.status = status != null ? status.toUpperCase() : null;
    }
}
