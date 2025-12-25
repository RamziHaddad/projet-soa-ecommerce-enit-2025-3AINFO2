package com.example.search_service.service;

import com.example.search_service.entity.ProductDocument;
import com.example.search_service.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductSearchRepository repository;

    public List<ProductDocument> searchByName(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }

    public List<ProductDocument> searchFullText(String keyword) {
        return repository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword, keyword);
    }

    public List<ProductDocument> searchFuzzy(String keyword) {
        return repository.findFuzzy(keyword);
    }
}
