package tn.enit.mail_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailDetailResponse {
    private Long id;
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String body;
    private boolean traite;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private List<String> attachments;
}
