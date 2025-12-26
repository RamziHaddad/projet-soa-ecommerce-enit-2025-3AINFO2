package com.enit.orderservice.interfaces.restcontrollers;

import com.enit.orderservice.application.service.OrderApplicationService;
import com.enit.orderservice.domaine.exception.OrderNotFoundException;
import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.interfaces.dto.OrderMapper;
import com.enit.orderservice.interfaces.dto.OrderRequestDTO;
import com.enit.orderservice.interfaces.dto.OrderResponseDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Order Management", description = "Endpoints for managing orders in the e-commerce system")
public class OrderController {

    @Inject
    OrderApplicationService service;

    @POST
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order and triggers the saga orchestration workflow (pricing, inventory, payment, delivery)"
    )
    @APIResponse(
        responseCode = "201",
        description = "Order created successfully",
        content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid request data (validation failed)"
    )
    public Response createOrder(@Valid OrderRequestDTO dto) {
        Order order = OrderMapper.toDomain(dto);
        Order saved = service.createOrder(order);
        return Response.status(Response.Status.CREATED)
                .entity(OrderMapper.toResponse(saved))
                .build();
    }

    @GET
    @Operation(
        summary = "Get all orders",
        description = "Retrieves a list of all orders in the system"
    )
    @APIResponse(
        responseCode = "200",
        description = "List of orders retrieved successfully",
        content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))
    )
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = service.getOrders();
        return orders.stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves a specific order by its unique identifier"
    )
    @APIResponse(
        responseCode = "200",
        description = "Order found and returned",
        content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found with the given ID"
    )
    public OrderResponseDTO getOrderById(
            @Parameter(description = "Order unique identifier", required = true)
            @PathParam("orderId") UUID orderId) {
        Order order = service.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderMapper.toResponse(order);
    }

    @PUT
    @Path("/{orderId}/cancel")
    @Operation(
        summary = "Cancel an order",
        description = "Cancels an existing order and triggers compensation saga if needed"
    )
    @APIResponse(
        responseCode = "200",
        description = "Order cancelled successfully",
        content = @Content(schema = @Schema(implementation = OrderResponseDTO.class))
    )
    @APIResponse(
        responseCode = "404",
        description = "Order not found with the given ID"
    )
    @APIResponse(
        responseCode = "409",
        description = "Order cannot be cancelled in its current state"
    )
    public OrderResponseDTO cancelOrder(
            @Parameter(description = "Order unique identifier", required = true)
            @PathParam("orderId") UUID orderId) {
        Order order = service.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        service.saveOrder(order);
        return OrderMapper.toResponse(order);
    }

    @DELETE
    @Operation(
        summary = "Delete all orders (ADMIN/TESTING ONLY)",
        description = "⚠️ WARNING: Deletes all orders from the system. This endpoint should be restricted to admin users only and disabled in production.",
        hidden = false  // Set to true to hide from Swagger in production
    )
    @APIResponse(
        responseCode = "204",
        description = "All orders deleted successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Forbidden - Admin access required"
    )
    public Response deleteAllOrders() {
        
        service.deleteAllOrders();
        return Response.noContent().build();
    }
}


