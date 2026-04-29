package com.grievancehub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_audits")
public class ComplaintAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false)
    private String actionedBy; // Email of official

    @Column(nullable = false)
    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Default Constructor
    public ComplaintAudit() {}

    public ComplaintAudit(Complaint complaint, String actionedBy, String oldStatus, String newStatus) {
        this.complaint = complaint;
        this.actionedBy = actionedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
    public String getActionedBy() { return actionedBy; }
    public void setActionedBy(String actionedBy) { this.actionedBy = actionedBy; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
