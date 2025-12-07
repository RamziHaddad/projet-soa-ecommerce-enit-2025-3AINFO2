package com.example.search_service.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inbox")
@Data
public class InboxEvent {

    @Id
    private String eventId; // reçu depuis catalog-service, jamais regénéré

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime createdAt;

    private Boolean processed;
}
