package enit.delivery.controller;

import enit.delivery.dto.DeliveryRequestDTO;
import enit.delivery.entity.Delivery;
import enit.delivery.service.DeliveryServices;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryServices deliveryServices;

    // ✅ EXPLICIT CONSTRUCTOR — NO LOMBOK
    public DeliveryController(DeliveryServices deliveryServices) {
        this.deliveryServices = deliveryServices;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody DeliveryRequestDTO request) {
        deliveryServices.createDelivery(request);
    }

    @PutMapping("/{id}/start")
    public void start(@PathVariable Long id) {
        deliveryServices.startDelivery(id);
    }

    @PutMapping("/{id}/complete")
    public void complete(@PathVariable Long id) {
        deliveryServices.completeDelivery(id);
    }

    @GetMapping("/user/{userId}")
    public List<Delivery> byUser(@PathVariable Long userId) {
        return deliveryServices.getDeliveriesByUser(userId);
    }
}
