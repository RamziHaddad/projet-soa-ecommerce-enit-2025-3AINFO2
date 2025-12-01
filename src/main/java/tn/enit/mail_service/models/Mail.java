package tn.enit.mail_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderEmail;
    private String recipientEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private boolean traite;

    @Column(unique = true, nullable = false)
    private String contentHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @ElementCollection
    @CollectionTable(name = "mail_attachments", joinColumns = @JoinColumn(name = "mail_id"))
    @Column(name = "attachment_url")
    private List<String> attachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
