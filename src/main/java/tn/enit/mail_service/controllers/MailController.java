package tn.enit.mail_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enit.mail_service.dto.MailDetailResponse;
import tn.enit.mail_service.dto.MailRequest;
import tn.enit.mail_service.dto.MailResponse;
import tn.enit.mail_service.services.interfaces.IMailService;

import java.util.List;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
public class MailController {

    private final IMailService mailService;

    @PostMapping
    public ResponseEntity<MailResponse> envoyerMail(@RequestBody MailRequest request) {
        MailResponse response = mailService.enregistrerMail(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<MailResponse> envoyerMailSync(@RequestBody MailRequest request) {
        MailResponse response = mailService.envoyerMailSync(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MailDetailResponse>> getAllMails(
            @RequestParam(required = false) Boolean traite) {
        List<MailDetailResponse> mails = mailService.getAllMails(traite);
        return ResponseEntity.ok(mails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MailDetailResponse> getMailById(@PathVariable Long id) {
        try {
            MailDetailResponse mail = mailService.getMailById(id);
            return ResponseEntity.ok(mail);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
