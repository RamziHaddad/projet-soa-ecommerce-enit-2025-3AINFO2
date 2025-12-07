package com.example.search_service.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.Data;

@Document(indexName = "products")
@Data
public class ProductDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private Double price;
    private String category;
}
