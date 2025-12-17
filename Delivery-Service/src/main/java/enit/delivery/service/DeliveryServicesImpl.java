package enit.delivery.service;

import enit.delivery.dto.DeliveryRequestDTO;
import enit.delivery.entity.Delivery;
import enit.delivery.exception.ResourceNotFoundException;
import enit.delivery.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeliveryServicesImpl implements DeliveryServices {

    private final DeliveryRepository deliveryRepository;

    // ✅ EXPLICIT CONSTRUCTOR (NO LOMBOK, NO ERRORS)
    public DeliveryServicesImpl(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public void createDelivery(DeliveryRequestDTO request) {
        Delivery delivery = Delivery.create(
                request.getOrderId(),
                request.getUserId(),
                request.getAddress()
        );
        deliveryRepository.save(delivery);
    }

    @Override
    public void startDelivery(Long deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.start();
    }

    @Override
    public void completeDelivery(Long deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.complete();
    }

    @Override
    public List<Delivery> getDeliveriesByUser(Long userId) {
        return deliveryRepository.findByUserId(userId);
    }

    private Delivery getDelivery(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Delivery not found with id " + id));
    }
}
