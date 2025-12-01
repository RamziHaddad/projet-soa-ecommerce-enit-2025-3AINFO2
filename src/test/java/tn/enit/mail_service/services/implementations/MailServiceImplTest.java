package tn.enit.mail_service.services.implementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.enit.mail_service.dto.MailRequest;
import tn.enit.mail_service.dto.MailResponse;
import tn.enit.mail_service.models.Mail;
import tn.enit.mail_service.repositories.MailRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private MailRepository mailRepository;

    @InjectMocks
    private MailServiceImpl mailService;

    private MailRequest mailRequest;

    @BeforeEach
    void setUp() {
        mailRequest = new MailRequest();
        mailRequest.setSenderEmail("sender@test.com");
        mailRequest.setRecipientEmail("recipient@test.com");
        mailRequest.setSubject("Test Subject");
        mailRequest.setBody("Test Body");
    }

    @Test
    void enregistrerMail_nouveauMail_retourneSuccess() {
        when(mailRepository.findByContentHash(anyString())).thenReturn(Optional.empty());
        
        Mail savedMail = new Mail();
        savedMail.setId(1L);
        when(mailRepository.save(any(Mail.class))).thenReturn(savedMail);

        MailResponse response = mailService.enregistrerMail(mailRequest);

        assertTrue(response.isSuccess());
        assertEquals("Mail enregistré avec succès", response.getMessage());
        assertEquals(1L, response.getMailId());
        verify(mailRepository, times(1)).save(any(Mail.class));
    }

    @Test
    void enregistrerMail_mailEnDouble_neCreePasDeNouveau() {
        Mail existingMail = new Mail();
        existingMail.setId(1L);
        when(mailRepository.findByContentHash(anyString())).thenReturn(Optional.of(existingMail));

        MailResponse response = mailService.enregistrerMail(mailRequest);

        assertTrue(response.isSuccess());
        assertEquals("Mail déjà enregistré", response.getMessage());
        assertEquals(1L, response.getMailId());
        verify(mailRepository, never()).save(any(Mail.class));
    }
}
