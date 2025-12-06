package tn.enit.mail_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.enit.mail_service.services.interfaces.IEmailSender;

@Configuration
public class EmailSenderConfig {

    @Value("${mail.sender.type:mailtrap}")
    private String senderType;

    @Bean
    public IEmailSender emailSender(
            @Qualifier("mailtrapSender") IEmailSender mailtrapSender,
            @Qualifier("smtpSender") IEmailSender smtpSender) {

        return "smtp".equalsIgnoreCase(senderType) ? smtpSender : mailtrapSender;
    }
}
