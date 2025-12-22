package org.com.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.com.DTO.ProductDTO;
import org.com.entities.OutboxEvent;
import org.com.exceptions.EntityAlreadyExistsException;
import org.com.service.OutboxService;
import org.com.service.ProductService;

@Path("/api/outbox")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OutboxResource {

    @Inject
    OutboxService outboxService;

    @Inject
    ProductService productService;

    @GET
    @Path("/pending")
    public List<OutboxEvent> getPendingEvents(@QueryParam("limit") @DefaultValue("20") int limit) {
        return outboxService.getPendingEvents(limit);
    }

    @GET
    @Path("/failed")
    public List<OutboxEvent> getFailedEvents() {
        return outboxService.getEventsByStatus("FAILED");
    }

    @GET
    @Path("/processed")
    public List<OutboxEvent> getProcessedEvents() {
        return outboxService.getEventsByStatus("PROCESSED");
    }

    @GET
    @Path("/stats")
    public Map<String, Object> getStats() {
        return outboxService.getStats();
    }

    @GET
    @Path("/aggregate/{aggregateId}")
    public List<OutboxEvent> getByAggregateId(@PathParam("aggregateId") UUID aggregateId) {
        return outboxService.getEventsByAggregateId(aggregateId);
    }

    @POST
    @Path("/{id}/retry")
    public Map<String, String> retryEvent(@PathParam("id") UUID id) {
        outboxService.resetForRetry(id);
        return Map.of("message", "Event reset for retry");
    }
   
    // ============================================
    // 🧪 SIMULATION OUTBOX
    // ============================================

    /**
     * Crée un produit de test et génère automatiquement un événement Outbox
     * Exemple: POST /api/outbox/simulate
     */
    @POST
    @Path("/simulate")
    public Response simulateOutboxEvent() {
        try {
            // Créer un produit de test
            ProductDTO dto = new ProductDTO();
            dto.setName("Test Product - " + System.currentTimeMillis());
            dto.setDescription("Produit créé pour tester le pattern Outbox");
            dto.setPriceCatalog(java.math.BigDecimal.valueOf(99.99));
            dto.setCategoryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

            var product = productService.createProduct(dto);

            // Récupérer l'événement Outbox créé
            List<OutboxEvent> events = outboxService.getEventsByAggregateId(product.getId());

            return Response.ok()
                .entity(Map.of(
                    "message", "✅ Produit créé avec succès et événement Outbox généré",
                    "productId", product.getId(),
                    "productName", product.getName(),
                    "outboxEvent", events.isEmpty() ? null : events.get(0),
                    "instructions", Map.of(
                        "step1", "Vérifiez les événements PENDING: GET /api/outbox/pending",
                        "step2", "Attendez 30 secondes pour que le scheduler traite l'événement",
                        "step3", "Vérifiez les événements PROCESSED: GET /api/outbox/processed"
                    )
                ))
                .build();

        } catch (EntityAlreadyExistsException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Erreur: " + e.getMessage()))
                .build();
        }
    }

   
}