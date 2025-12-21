package org.com.scheduler;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

@ApplicationScoped
public class OutboxScheduler {
    
    private static final Logger LOG = Logger.getLogger(OutboxScheduler.class);
    private static final int MAX_RETRIES = 3;
    
    @Inject
    OutboxService outboxService;
    
    @Inject
    ObjectMapper objectMapper;
    
    // à vérifier ce configuration
    @ConfigProperty(name = "indexation.service.url", defaultValue = "")
    String indexationUrl;
    
    private final Client httpClient = ClientBuilder.newClient();
    
    @Scheduled(every = "30s", concurrentExecution = ConcurrentExecution.SKIP)
    
    @Transactional
    public void processOutboxEvents() {
        LOG.info("Processing outbox events...");
        
        List<OutboxEvent> pendingEvents = outboxService.getPendingEvents(10);
        
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
            
            Response response = httpClient
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
            }
            
            response.close();
            
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