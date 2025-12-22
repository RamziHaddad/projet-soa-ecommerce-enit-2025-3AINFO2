package org.com.scheduler;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.com.entities.OutboxEvent;
import org.com.entities.Product;
import org.com.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class OutboxScheduler {
    
    private static final Logger LOG = Logger.getLogger(OutboxScheduler.class);
    private static final int MAX_RETRIES = 3;
    private static final int BATCH_SIZE = 10;
    // Timeout pour éviter les appels HTTP bloquants
    private static final int HTTP_TIMEOUT_SECONDS = 30;

    // Configuration du client HTTP avec timeout
    private final Client httpClient = ClientBuilder.newBuilder()
        .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build();
    
    @Inject
    OutboxService outboxService;
    
    @Inject
    ObjectMapper objectMapper;
    
    // à vérifier ce configuration
    @ConfigProperty(name = "indexation.service.url", defaultValue = "")
    String indexationUrl;
    
    
    @Scheduled(every = "30s", concurrentExecution = ConcurrentExecution.SKIP)
    
    public void processOutboxEvents() {
        
        
        List<OutboxEvent> pendingEvents = outboxService.getPendingEvents(BATCH_SIZE);
        
        for (OutboxEvent event : pendingEvents) {
            processEvent(event);
        }
    }
    
    
    private void processEvent(OutboxEvent event) {
        try {

            // Désérialiser le produit depuis le payload JSON
            Product product = objectMapper.readValue(event.getPayload(), Product.class);
            
            // Appeler le web service d'indexation via HTTP
            LOG.infof("Calling indexation service for product %s at %s", product.getId(), indexationUrl);
           
            Response response = null;
            try {
             response = httpClient
                .target(indexationUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(product));
            
            int status = response.getStatus();
            
            if (status >= 200 && status < 300) {
                LOG.infof("Product %s indexed successfully", product.getId());
                outboxService.markAsProcessed(event.getId());
            } else {
                String errorMsg = response.readEntity(String.class);
                throw new RuntimeException("Indexation failed with status " + status + ": " + errorMsg);
            } }
            finally {
                //  Fermeture de la réponse HTTP
                if (response != null) {
                    response.close();
                }
            }
            
        } catch (Exception e) {
            LOG.errorf("Failed to process event %s (retry %d): %s", 
                event.getId(), event.getRetryCount(), e.getMessage());
            
            outboxService.incrementRetryCount(event.getId());
            
            if (event.getRetryCount() + 1 >= MAX_RETRIES) {

                outboxService.markAsFailed(event.getId());

                LOG.warnf("Event %s marked as FAILED after %d retries", event.getId(), MAX_RETRIES);
            }
        }
    }
}