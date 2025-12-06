package tn.enit.mail_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.enit.mail_service.services.interfaces.IMailProcessor;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailScheduler {

    private final IMailProcessor mailProcessor;

    @Scheduled(fixedDelayString = "${mail.scheduler.fixed-delay:300000}")
    public void processUnsentMails() {
        log.info("Démarrage du traitement périodique des mails non envoyés");
        mailProcessor.traiterMailsNonEnvoyes();
        log.info("Fin du traitement périodique des mails non envoyés");
    }
}
