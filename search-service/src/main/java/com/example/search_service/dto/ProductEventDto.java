package com.example.search_service.dto;
import lombok.Data;


@Data
public class ProductEventDto {
    private String eventId;      // généré par catalog-service
    private String id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String eventType;    // CREATED / UPDATED / DELETED
}