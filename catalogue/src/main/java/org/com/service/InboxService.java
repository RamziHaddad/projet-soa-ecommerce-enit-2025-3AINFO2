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

        //  Vérifier si déjà traité 
        //  Garantir Idempotence 
        if (inboxRepository.alreadyProcessed(eventUUID)) {
            LOG.infof("Event %s already processed, skipping", eventId);
            throw new IllegalArgumentException("Event already processed"); // Lever une exception
        }

        // si n'est pas déjà traité , Enregistrer dans Inbox avant de traiter pour éviter les doublons en cas de crash
        InboxEvent inboxEvent = new InboxEvent(eventUUID, eventType, productUUID);
        inboxRepository.insert(inboxEvent);

        // Mettre à jour le prix du produit
        try {
            productService.updateProductPrice(productUUID, newPrice);
            LOG.infof("Price updated: Product %s -> %s", productId, newPrice);
        
        } // Produit introuvable L’événement reste en Inbox et considéré comme traité
        catch (EntityNotFoundException e) {
            LOG.errorf("Product %s not found, event recorded but not applied", productId);
           
        }
        
        LOG.infof("Event %s processed successfully", eventId);
    }
    
    @Transactional
    public boolean isAlreadyProcessed(String eventId) {
        return inboxRepository.alreadyProcessed(UUID.fromString(eventId));
    }
    
    @Transactional
    public List<InboxEvent> getAllEvents(int limit) {
        return inboxRepository.findAll(limit);
    }

    
    @Transactional
    public Map<String, Object> getStats() {
        return inboxRepository.getStats();
    }
}