package com.example.search_service.service;
import com.example.search_service.dto.ProductEventDto;
import com.example.search_service.entity.InboxEvent;
import com.example.search_service.repository.InboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxRepository inboxRepository;
    private final IndexService indexService;
    private final ObjectMapper mapper;

    @Transactional
    public void receiveEvent(ProductEventDto dto) {

        String eventId = dto.getEventId();

        // 1) idempotence : si l'event existe déjà on ne le traite pas
        if (inboxRepository.existsById(eventId)) {
            return;
        }

        // 2) on sauvegarde l'événement
        InboxEvent event = new InboxEvent();
        try {
            event.setEventId(eventId);
            event.setPayload(mapper.writeValueAsString(dto));
            event.setCreatedAt(LocalDateTime.now());
            event.setProcessed(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        inboxRepository.save(event);

        // 3) traitement business
        indexService.processEvent(event);

        // 4) marquer comme traité
        event.setProcessed(true);
        inboxRepository.save(event);
    }
}
