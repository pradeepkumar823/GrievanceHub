package com.grievancehub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "complaint_audits")
public class ComplaintAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false)
    private String actionedBy;

    @Column(nullable = false)
    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ComplaintAudit(Complaint complaint, String actionedBy, String oldStatus, String newStatus) {
        this.complaint = complaint;
        this.actionedBy = actionedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
    }
}
