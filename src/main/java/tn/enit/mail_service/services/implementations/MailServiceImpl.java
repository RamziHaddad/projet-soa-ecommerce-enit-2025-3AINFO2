package tn.enit.mail_service.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.enit.mail_service.dto.MailDetailResponse;
import tn.enit.mail_service.dto.MailRequest;
import tn.enit.mail_service.dto.MailResponse;
import tn.enit.mail_service.models.Mail;
import tn.enit.mail_service.repositories.MailRepository;
import tn.enit.mail_service.services.interfaces.IEmailSender;
import tn.enit.mail_service.services.interfaces.IMailService;
import tn.enit.mail_service.utils.MailHashUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements IMailService {

    private final MailRepository mailRepository;
    private final IEmailSender emailSender;

    @Override
    public MailResponse enregistrerMail(MailRequest request) {
        String hash = MailHashUtil.generateHash(
            request.getSenderEmail(),
            request.getRecipientEmail(),
            request.getSubject(),
            request.getBody()
        );

        Optional<Mail> existingMail = mailRepository.findByContentHash(hash);
        
        if (existingMail.isPresent()) {
            log.info("Mail en double détecté (hash: {}), aucune action effectuée", hash);
            return new MailResponse(true, "Mail déjà enregistré", existingMail.get().getId());
        }

        Mail mail = new Mail();
        mail.setSenderEmail(request.getSenderEmail());
        mail.setRecipientEmail(request.getRecipientEmail());
        mail.setSubject(request.getSubject());
        mail.setBody(request.getBody());
        mail.setAttachments(request.getAttachments());
        mail.setContentHash(hash);
        mail.setTraite(false);

        Mail savedMail = mailRepository.save(mail);
        log.info("Mail enregistré avec succès (ID: {}, hash: {})", savedMail.getId(), hash);

        return new MailResponse(true, "Mail enregistré avec succès", savedMail.getId());
    }

    @Override
    public MailResponse envoyerMailSync(MailRequest request) {
        String hash = MailHashUtil.generateHash(
            request.getSenderEmail(),
            request.getRecipientEmail(),
            request.getSubject(),
            request.getBody()
        );

        Optional<Mail> existingMail = mailRepository.findByContentHash(hash);
        
        if (existingMail.isPresent()) {
            log.info("Mail en double détecté (hash: {}), aucune action effectuée", hash);
            return new MailResponse(true, "Mail déjà envoyé", existingMail.get().getId());
        }

        Mail mail = new Mail();
        mail.setSenderEmail(request.getSenderEmail());
        mail.setRecipientEmail(request.getRecipientEmail());
        mail.setSubject(request.getSubject());
        mail.setBody(request.getBody());
        mail.setAttachments(request.getAttachments());
        mail.setContentHash(hash);
        mail.setTraite(false);

        Mail savedMail = mailRepository.save(mail);

        try {
            emailSender.sendEmail(savedMail);
            savedMail.setTraite(true);
            savedMail.setSentAt(LocalDateTime.now());
            mailRepository.save(savedMail);
            log.info("Mail envoyé immédiatement avec succès (ID: {})", savedMail.getId());
            return new MailResponse(true, "Mail envoyé avec succès", savedMail.getId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi immédiat du mail (ID: {}): {}", savedMail.getId(), e.getMessage());
            return new MailResponse(false, "Erreur lors de l'envoi: " + e.getMessage(), savedMail.getId());
        }
    }

    @Override
    public List<MailDetailResponse> getAllMails(Boolean traite) {
        List<Mail> mails;
        
        if (traite == null) {
            mails = mailRepository.findAll();
        } else if (traite) {
            mails = mailRepository.findByTraiteTrue();
        } else {
            mails = mailRepository.findByTraiteFalse();
        }

        return mails.stream()
            .map(this::convertToDetailResponse)
            .collect(Collectors.toList());
    }

    @Override
    public MailDetailResponse getMailById(Long id) {
        Mail mail = mailRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mail non trouvé avec l'ID: " + id));
        return convertToDetailResponse(mail);
    }

    private MailDetailResponse convertToDetailResponse(Mail mail) {
        return new MailDetailResponse(
            mail.getId(),
            mail.getSenderEmail(),
            mail.getRecipientEmail(),
            mail.getSubject(),
            mail.getBody(),
            mail.isTraite(),
            mail.getCreatedAt(),
            mail.getSentAt(),
            mail.getAttachments()
        );
    }
}
