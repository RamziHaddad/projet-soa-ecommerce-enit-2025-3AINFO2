package tn.enit.mail_service.services.interfaces;

import tn.enit.mail_service.dto.MailDetailResponse;
import tn.enit.mail_service.dto.MailRequest;
import tn.enit.mail_service.dto.MailResponse;

import java.util.List;

public interface IMailService {
    MailResponse enregistrerMail(MailRequest request);
    
    MailResponse envoyerMailSync(MailRequest request);
    
    List<MailDetailResponse> getAllMails(Boolean traite);
    
    MailDetailResponse getMailById(Long id);
}
