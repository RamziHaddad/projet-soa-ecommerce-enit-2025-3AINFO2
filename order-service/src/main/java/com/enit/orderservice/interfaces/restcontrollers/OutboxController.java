package com.enit.orderservice.interfaces.restcontrollers;

import com.enit.orderservice.infrastructure.outbox.OutboxService;
import com.enit.orderservice.infrastructure.outbox.OutboxStats;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST endpoint for monitoring outbox pattern health and statistics
 */
@Path("/outbox")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Outbox Monitoring", description = "Monitor outbox pattern health and event publishing statistics")
public class OutboxController {

    @Inject
    OutboxService outboxService;

    @GET
    @Path("/stats")
    @Operation(
        summary = "Get outbox statistics",
        description = "Returns statistics about pending, published, and failed events in the outbox"
    )
    @APIResponse(
        responseCode = "200",
        description = "Outbox statistics retrieved successfully"
    )
    public OutboxStats getStats() {
        return outboxService.getStats();
    }
}
