package com.enit.orderservice.infrastructure.external;

import com.enit.orderservice.domaine.model.Order;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "inventory-service")
public interface InventoryClient {

    @POST
    @Path("/inventory/reserve")
    void reserveStock(Order order);

    @POST
    @Path("/inventory/release")
    void releaseStock(Order order);
}
