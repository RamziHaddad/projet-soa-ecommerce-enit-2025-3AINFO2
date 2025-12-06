package tn.enit.mail_service.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enit.mail_service.models.Mail;
import tn.enit.mail_service.repositories.MailRepository;
import tn.enit.mail_service.services.interfaces.IEmailSender;
import tn.enit.mail_service.services.interfaces.IMailProcessor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailProcessorImpl implements IMailProcessor {

    private final MailRepository mailRepository;
    private final IEmailSender emailSender;

    @Override
    @Transactional
    public void traiterMailsNonEnvoyes() {
        List<Mail> mailsNonTraites = mailRepository.findByTraiteFalse();

        if (mailsNonTraites.isEmpty()) {
            log.debug("Aucun mail à traiter");
            return;
        }

        log.info("Traitement de {} mails non envoyés", mailsNonTraites.size());

        for (Mail mail : mailsNonTraites) {
            try {
                emailSender.sendEmail(mail);
                mail.setTraite(true);
                mail.setSentAt(LocalDateTime.now());
                mailRepository.save(mail);
                log.info("Mail traité avec succès (ID: {})", mail.getId());
            } catch (Exception e) {
                log.error("Échec du traitement du mail (ID: {}): {}", mail.getId(), e.getMessage());
            }
        }
    }
}
