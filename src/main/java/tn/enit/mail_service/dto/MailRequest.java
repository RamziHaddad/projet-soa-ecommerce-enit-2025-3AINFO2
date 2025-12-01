package tn.enit.mail_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailRequest {
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String body;
    private List<String> attachments = new ArrayList<>();
}
