package com.ecommerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// "order-service" est le nom du microservice commande enregistré dans Eureka (si vous l'utilisez)
// Sinon, vous pouvez utiliser url = "http://localhost:8081" pour tester en local
@FeignClient(name = "order-service", url ="https://webhook.site/d7a28bf0-5651-46f6-9ea9-9969b1d01f36")
public interface OrderClient {

    @PutMapping("/api/orders/{orderId}/status")
    void updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);
}