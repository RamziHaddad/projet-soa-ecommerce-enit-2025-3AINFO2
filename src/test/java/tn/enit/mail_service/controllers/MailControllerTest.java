package tn.enit.mail_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.enit.mail_service.dto.MailRequest;
import tn.enit.mail_service.dto.MailResponse;
import tn.enit.mail_service.services.interfaces.IMailService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MailController.class)
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IMailService mailService;

    @Test
    void envoyerMail_retourneSuccessResponse() throws Exception {
        MailRequest request = new MailRequest();
        request.setSenderEmail("sender@test.com");
        request.setRecipientEmail("recipient@test.com");
        request.setSubject("Test Subject");
        request.setBody("Test Body");

        MailResponse response = new MailResponse(true, "Mail enregistré avec succès", 1L);
        when(mailService.enregistrerMail(any(MailRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/mails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mail enregistré avec succès"))
                .andExpect(jsonPath("$.mailId").value(1));
    }
}
