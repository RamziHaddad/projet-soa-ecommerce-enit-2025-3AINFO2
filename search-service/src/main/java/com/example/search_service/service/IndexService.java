package com.example.search_service.service;
import com.example.search_service.dto.ProductEventDto;
import com.example.search_service.entity.InboxEvent;
import com.example.search_service.repository.ProductSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final ProductSearchRepository repository;
    private final ObjectMapper mapper;

    public void processEvent(InboxEvent inboxEvent) {

        try {
            ProductEventDto dto =
                mapper.readValue(inboxEvent.getPayload(), ProductEventDto.class);

            switch (dto.getEventType()) {

                case "CREATED":
                case "UPDATED":
                    ProductDocument doc = new ProductDocument();
                    doc.setId(dto.getId());
                    doc.setName(dto.getName());
                    doc.setDescription(dto.getDescription());
                    doc.setPrice(dto.getPrice());
                    doc.setCategory(dto.getCategory());
                    repository.save(doc);
                    break;

                case "DELETED":
                    repository.deleteById(dto.getId());
                    break;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
