package org.com.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.com.entities.InboxEvent;
import org.com.service.InboxService;

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

    @POST
    @Path("/simulate")
    public Map<String, String> simulateEvent(SimulatePriceEventRequest request) {
        String eventId = request.eventId != null ? request.eventId : UUID.randomUUID().toString();
        String productId = request.productId != null ? request.productId : UUID.randomUUID().toString();
        
        inboxService.processPriceEvent(
            eventId,
            request.eventType != null ? request.eventType : "PRICE_UPDATED",
            productId,
            request.newPrice
        );
        return Map.of("status", "processed", "eventId", eventId);
    }

    public static class SimulatePriceEventRequest {
        public String eventId;
        public String eventType;
        public String productId;  // ← Renommé de aggregateId à productId
        public BigDecimal newPrice;
    }
}