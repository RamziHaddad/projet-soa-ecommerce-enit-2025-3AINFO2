package com.example.search_service.repository;
import com.example.search_service.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByNameContainingIgnoreCase(String keyword);
    List<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String descriptio);
    // recherche  qui tolère les fautes de frappe ou variation
    List<ProductDocument> findFuzzy(String keyword);

}
