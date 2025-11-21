package org.com.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.com.entities.InboxEvent;
import org.com.exceptions.EntityNotFoundException;
import org.com.repository.InboxRepository;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class InboxService {

    private static final Logger LOG = Logger.getLogger(InboxService.class);

    @Inject
    InboxRepository inboxRepository;

    @Inject
    ProductService productService;

    @Transactional
    public void processPriceEvent(String eventId, String eventType, String productId, BigDecimal newPrice) {
        
        UUID eventUUID = UUID.fromString(eventId);
        UUID productUUID = UUID.fromString(productId);

        // 1. Vérifier si déjà traité
        if (inboxRepository.alreadyProcessed(eventUUID)) {
            LOG.infof("Event %s already processed, skipping", eventId);
            return;
        }

        // 2. Mettre à jour le prix du produit
        try {
            productService.updateProductPrice(productUUID, newPrice);  // ← Utilise la nouvelle méthode
            LOG.infof("Price updated: Product %s -> %s", productId, newPrice);
        } catch (EntityNotFoundException e) {
            LOG.errorf("Product %s not found, rejecting event", productId);
            throw new RuntimeException("Cannot process event: product not found");
        }

        // 3. Enregistrer dans Inbox
        InboxEvent inboxEvent = new InboxEvent(eventUUID, eventType, productUUID);
        inboxRepository.insert(inboxEvent);
        
        LOG.infof("Event %s processed successfully", eventId);
    }

    public boolean isAlreadyProcessed(String eventId) {
        return inboxRepository.alreadyProcessed(UUID.fromString(eventId));
    }

    public List<InboxEvent> getAllEvents(int limit) {
        return inboxRepository.findAll(limit);
    }

    public Map<String, Object> getStats() {
        return inboxRepository.getStats();
    }
}