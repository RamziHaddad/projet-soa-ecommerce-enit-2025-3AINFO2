package org.com.scheduler;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.com.entities.OutboxEvent;
import org.com.service.OutboxService;
import org.com.service.MessageBrokerService;

import java.util.List;

@ApplicationScoped
public class OutboxScheduler {

    private static final Logger LOG = Logger.getLogger(OutboxScheduler.class);
    private static final int MAX_RETRIES = 3;

    @Inject
    OutboxService outboxService;

    @Inject
    MessageBrokerService messageBrokerService;

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
            messageBrokerService.sendToKafka(event);
            outboxService.markAsProcessed(event.getId());
            LOG.infof("Event %s processed successfully", event.getId());

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