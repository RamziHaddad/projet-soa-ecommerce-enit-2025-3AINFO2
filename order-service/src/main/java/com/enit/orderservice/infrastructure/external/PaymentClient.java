package com.enit.orderservice.infrastructure.external;


import com.enit.orderservice.domaine.model.Order;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "payment-service")
public interface PaymentClient {

    @POST
    @Path("/payment/process")
    void processPayment(Order order);
}
