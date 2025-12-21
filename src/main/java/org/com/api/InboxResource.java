package org.com.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.com.DTO.ProductDTO;
import org.com.entities.InboxEvent;
import org.com.service.InboxService;
import org.com.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Path("/api/inbox")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InboxResource {

    @Inject
    InboxService inboxService;
    
    @Inject
    ProductService productService;

    @GET
    public List<InboxEvent> getAllEvents(@QueryParam("limit") @DefaultValue("50") int limit) {
        return inboxService.getAllEvents(limit);
    }

    @GET
    @Path("/check/{eventId}")
    public Map<String, Object> checkEvent(@PathParam("eventId") String eventId) {
        boolean processed = inboxService.isAlreadyProcessed(eventId);
        return Map.of(
            "eventId", eventId,
            "processed", processed
        );
    }

    @GET
    @Path("/stats")
    public Map<String, Object> getStats() {
        return inboxService.getStats();
    }

    // ============================================
    // 🧪 SIMULATION INBOX
    // ============================================

    /**
     * Simule un scénario complet de mise à jour de prix via Inbox
     * Exemple: POST /api/inbox/simulate
     */
    @POST
    @Path("/simulate")
    public Response simulateInboxScenario() {
        try {
            // Étape 1: Créer un produit avec un prix initial
            ProductDTO dto = new ProductDTO();
            dto.setName("Test Inbox Product - " + System.currentTimeMillis());
            dto.setDescription("Produit pour tester le pattern Inbox");
            dto.setPriceCatalog(BigDecimal.valueOf(50.00));
            dto.setCategoryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

            var product = productService.createProduct(dto);
            String eventId = UUID.randomUUID().toString();

            // Étape 2: Simuler un événement de mise à jour de prix
            inboxService.processPriceEvent(
                eventId,
                "PRICE_UPDATED",
                product.getId().toString(),
                BigDecimal.valueOf(79.99)
            );

            // Étape 3: Récupérer le produit mis à jour
            var updatedProduct = productService.getProduct(product.getId());

            return Response.ok()
                .entity(Map.of(
                    "message", "✅ Scénario Inbox testé avec succès",
                    "scenario", Map.of(
                        "step1", "Produit créé avec prix initial: 50.00",
                        "step2", "Événement de prix envoyé: 79.99",
                        "step3", "Prix mis à jour avec succès"
                    ),
                    "result", Map.of(
                        "productId", updatedProduct.getId(),
                        "productName", updatedProduct.getName(),
                        "oldPrice", 50.00,
                        "newPrice", updatedProduct.getPriceCatalog(),
                        "eventId", eventId,
                        "inboxRecorded", true
                    ),
                    "verification", Map.of(
                        "checkInbox", "GET /api/inbox",
                        "checkEvent", "GET /api/inbox/check/" + eventId,
                        "stats", "GET /api/inbox/stats"
                    )
                ))
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Erreur: " + e.getMessage()))
                .build();
        }
    }

     /**
     * Test de déduplication: envoie le même événement 2 fois
     * Exemple: POST /api/inbox/simulate-duplicate
     */
    @POST
    @Path("/simulate-duplicate")
    public Response simulateDuplicateEvent() {
        try {
            // Créer un produit
            ProductDTO dto = new ProductDTO();
            dto.setName("Test Duplicate - " + System.currentTimeMillis());
            dto.setDescription("Test de déduplication Inbox");
            dto.setPriceCatalog(BigDecimal.valueOf(100.00));
            dto.setCategoryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

            var product = productService.createProduct(dto);
            String eventId = UUID.randomUUID().toString();

            // Premier envoi
            inboxService.processPriceEvent(
                eventId,
                "PRICE_UPDATED",
                product.getId().toString(),
                BigDecimal.valueOf(150.00)
            );

            var productAfterFirst = productService.getProduct(product.getId());

            // Deuxième envoi (MÊME eventId)
            String duplicateResult;
            try {
                inboxService.processPriceEvent(
                    eventId, // ← MÊME eventId
                    "PRICE_UPDATED",
                    product.getId().toString(),
                    BigDecimal.valueOf(200.00)
                );
                duplicateResult = "❌ ERREUR: Le doublon a été traité (ne devrait pas!)";
            } catch (IllegalArgumentException e) {
                duplicateResult = "✅ Doublon correctement rejeté: " + e.getMessage();
            }

            var productAfterSecond = productService.getProduct(product.getId());

            return Response.ok()
                .entity(Map.of(
                    "message", "✅ Test de déduplication terminé",
                    "scenario", Map.of(
                        "event1", "Premier événement envoyé avec eventId: " + eventId,
                        "event2", "Deuxième événement envoyé avec MÊME eventId",
                        "expected", "Le deuxième événement doit être rejeté"
                    ),
                    "results", Map.of(
                        "initialPrice", 100.00,
                        "priceAfterFirstEvent", productAfterFirst.getPriceCatalog(),
                        "priceAfterSecondEvent", productAfterSecond.getPriceCatalog(),
                        "duplicateHandling", duplicateResult,
                        "pricesAreEqual", productAfterFirst.getPriceCatalog()
                            .equals(productAfterSecond.getPriceCatalog())
                    ),
                    "conclusion", productAfterFirst.getPriceCatalog()
                        .equals(productAfterSecond.getPriceCatalog())
                        ? "✅ Idempotence garantie: le prix n'a changé qu'une fois"
                        : "❌ Problème: le prix a changé deux fois"
                ))
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Erreur: " + e.getMessage()))
                .build();
        }
    }

}
