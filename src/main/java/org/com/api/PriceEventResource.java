package org.com.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.com.service.InboxService;

import java.math.BigDecimal;

@Path("/api/price-events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PriceEventResource {

    private static final Logger LOG = Logger.getLogger(PriceEventResource.class);

    @Inject
    InboxService inboxService;

    // Endpoint appelé par le microservice Pricing 
     
    @POST
    public Response receivePriceEvent(@Valid PriceEventRequest request) {
        
        
        LOG.infof("Received price event: eventId=%s, productId=%s, newPrice=%s", 
                  request.eventId, request.productId, request.newPrice);

        try {
            // Traiter l'événement via InboxService
            inboxService.processPriceEvent(
                request.eventId,
                request.eventType != null ? request.eventType : "PRICE_UPDATED",
                request.productId,
                request.newPrice
            );

            return Response.ok()
                .entity(new EventResponse("success", "Price event processed", request.eventId))
                .build();

        } catch (IllegalArgumentException e) {

            // Événement déjà traité
            LOG.infof("Event %s already processed", request.eventId);
            return Response.status(Response.Status.OK)
                .entity(new EventResponse("duplicate", "Event already processed", request.eventId))
                .build();

        } catch (Exception e) {
            LOG.errorf("Failed to process price event: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new EventResponse("error", e.getMessage(), request.eventId))
                .build();
        }
    }

    // DTO pour la requête
    public static class PriceEventRequest {
        @NotNull
        public String eventId;

        public String eventType;

        @NotNull
        public String productId;

        @NotNull
        public BigDecimal newPrice;
    }

    // DTO pour la réponse
    public static class EventResponse {
        public String status;
        public String message;
        public String eventId;

        public EventResponse() {}

        public EventResponse(String status, String message, String eventId) {
            this.status = status;
            this.message = message;
            this.eventId = eventId;
        }
    }
}