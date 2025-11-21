package org.com.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.com.entities.OutboxEvent;
import org.com.service.OutboxService;

@Path("/api/outbox")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OutboxResource {

    @Inject
    OutboxService outboxService;

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
}