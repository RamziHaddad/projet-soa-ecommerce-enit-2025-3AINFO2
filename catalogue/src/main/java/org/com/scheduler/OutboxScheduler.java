package org.com.scheduler;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import jakarta.transaction.Transactional;
import org.com.entities.OutboxEvent;
import org.com.repository.OutboxRepository;
import org.com.service.MessageBrokerService;


import java.util.List;

@ApplicationScoped
public class OutboxScheduler {

    private static final Logger LOG = Logger.getLogger(OutboxScheduler.class);
    private static final int MAX_RETRIES = 3;

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    MessageBrokerService messageBrokerService;

    
    @Scheduled(every = "30s", concurrentExecution = ConcurrentExecution.SKIP) // Évite les exécutions concurrentes
    
    @Transactional
    public void processOutboxEvents() {
        LOG.info("Processing outbox events...");
        
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(10);
        
        for (OutboxEvent event : pendingEvents) {
            // Verrouillage optimiste ou pessimiste dans findPendingEvents est recommandé
            try {
                // Envoyer au broker
                messageBrokerService.sendToKafka(event);
                
                // Marquer comme traité
                outboxRepository.markAsProcessed(event.getId());
                LOG.infof("Event %s processed successfully", event.getId());

            } catch (Exception e) {
                LOG.errorf("Failed to process event %s (retry %d): %s", event.getId(), event.getRetryCount(), e.getMessage());
                handleProcessingFailure(event);
            }
        }
    }

    private void handleProcessingFailure(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        if (event.getRetryCount() >= MAX_RETRIES) {
            outboxRepository.markAsFailed(event.getId());
        }
        // Si non, l'événement reste PENDING et sera réessayé à la prochaine exécution.
        // La mise à jour du retryCount sera persistée grâce à @Transactional
    }
}