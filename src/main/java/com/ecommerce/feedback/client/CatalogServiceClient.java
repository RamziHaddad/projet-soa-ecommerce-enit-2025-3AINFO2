package com.ecommerce.feedback.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@Component
@Slf4j
public class CatalogServiceClient {
    
    private final RestTemplate restTemplate;
    private final String catalogServiceUrl;
    private final String productsEndpoint;
    
    public CatalogServiceClient(
            RestTemplate restTemplate,
            @Value("${catalog.service.url}") String catalogServiceUrl,
            @Value("${catalog.service.endpoint.products}") String productsEndpoint) {
        this.restTemplate = restTemplate;
        this.catalogServiceUrl = catalogServiceUrl;
        this.productsEndpoint = productsEndpoint;
    }
    
    /**
     * Vérifie si un produit existe dans le service Catalog
     * @param productId L'ID du produit à vérifier
     * @return true si le produit existe, false sinon
     */
    public boolean productExists(Long productId) {
        try {
            String url = catalogServiceUrl + productsEndpoint + "/" + productId;
            log.debug("Vérification de l'existence du produit {} via {}", productId, url);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Produit {} trouvé dans le service Catalog", productId);
                return true;
            }
            
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Produit {} non trouvé dans le service Catalog", productId);
            return false;
        } catch (ResourceAccessException e) {
            log.error("Erreur de connexion au service Catalog: {}", e.getMessage());
            // En cas d'erreur de connexion, on accepte quand même pour ne pas bloquer
            // Dans un environnement de production, on pourrait utiliser un circuit breaker
            return true; // Permet de continuer même si le service Catalog est indisponible
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du produit {}: {}", productId, e.getMessage());
            // En cas d'erreur, on accepte pour ne pas bloquer le service
            return true;
        }
    }
    
    /**
     * Récupère les informations d'un produit depuis le service Catalog
     * @param productId L'ID du produit
     * @return Les informations du produit ou null si non trouvé
     */
    public Map<String, Object> getProduct(Long productId) {
        try {
            String url = catalogServiceUrl + productsEndpoint + "/" + productId;
            log.debug("Récupération du produit {} depuis {}", productId, url);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            
            return null;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du produit {}: {}", productId, e.getMessage());
            return null;
        }
    }
}

