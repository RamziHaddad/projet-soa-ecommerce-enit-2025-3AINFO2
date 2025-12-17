package enit.delivery.service;

import enit.delivery.dto.DeliveryRequestDTO;
import enit.delivery.entity.Delivery;

import java.util.List;

public interface DeliveryServices {

    void createDelivery(DeliveryRequestDTO request);

    void startDelivery(Long deliveryId);

    void completeDelivery(Long deliveryId);

    List<Delivery> getDeliveriesByUser(Long userId);
}
