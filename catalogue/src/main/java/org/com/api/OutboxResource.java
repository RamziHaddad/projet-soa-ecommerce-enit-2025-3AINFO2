package org.com.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.com.repository.OutboxRepository;

import java.util.UUID;

@Path("/api/outbox")
@Produces(MediaType.APPLICATION_JSON)
public class OutboxResource {

    @Inject
    OutboxRepository outboxRepository;

    // GET /api/outbox/pending - Voir les événements en attente
    @GET
    @Path("/pending")
    public Response getPendingEvents(@QueryParam("limit") @DefaultValue("20") int limit) {
        return Response.ok(outboxRepository.findPendingEvents(limit)).build();
    }

    // GET /api/outbox/failed - Voir les événements en échec
    @GET
    @Path("/failed")
    public Response getFailedEvents() {
        return Response.ok(outboxRepository.findByStatus("FAILED")).build();
    }

    // GET /api/outbox/stats - Statistiques rapides
    @GET
    @Path("/stats")
    public Response getStats() {
        return Response.ok(outboxRepository.getStats()).build();
    }

    // GET /api/outbox/processed - Voir tous les événements traités
    @GET
    @Path("/processed")
    public Response getProcessedEvents() {
        return Response.ok(outboxRepository.findByStatus("PROCESSED")).build();
    }

    // GET /api/outbox/aggregate/{aggregateId} - Voir les événements par aggregateId
    @GET
    @Path("/aggregate/{aggregateId}")
    public Response getByAggregateId(@PathParam("aggregateId") UUID aggregateId) {
        return Response.ok(outboxRepository.findByAggregateId(aggregateId)).build();
    }

    // POST /api/outbox/{id}/retry - Réessayer un événement failed
    @POST
    @Path("/{id}/retry")
    public Response retryEvent(@PathParam("id") UUID id) {
        outboxRepository.resetForRetry(id);
        return Response.ok("{\"message\":\"Event reset for retry\"}").build();
    }
}