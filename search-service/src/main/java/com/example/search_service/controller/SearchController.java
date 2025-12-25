package com.example.search_service.controller;
import com.example.search_service.entity.ProductDocument;
import com.example.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
 
    @GetMapping("/name")
    public ResponseEntity<List<ProductDocument>> searchByName(
            @RequestParam String q) {

        return ResponseEntity.ok(searchService.searchByName(q));
    }

    @GetMapping("/full")
    public ResponseEntity<List<ProductDocument>> searchFullText(
            @RequestParam String q) {

        return ResponseEntity.ok(searchService.searchFullText(q));
    }

    @GetMapping("/fuzzy")
    public ResponseEntity<List<ProductDocument>> searchFuzzy(
            @RequestParam String q) {

        return ResponseEntity.ok(searchService.searchFuzzy(q));
    }
}
