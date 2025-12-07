package com.example.search_service.controller;
import com.example.search_service.dto.ProductEventDto;
import com.example.search_service.service.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class IndexController {

    private final InboxService inboxService;

    @PostMapping
    public ResponseEntity<String> receiveEvent(@RequestBody ProductEventDto dto) {
        inboxService.receiveEvent(dto);
        return ResponseEntity.ok("Event accepted");
    }
}
