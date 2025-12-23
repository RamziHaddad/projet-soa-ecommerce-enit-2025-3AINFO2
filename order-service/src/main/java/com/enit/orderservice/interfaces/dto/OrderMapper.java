package com.enit.orderservice.interfaces.dto;



import com.enit.orderservice.domaine.model.Order;
import com.enit.orderservice.domaine.model.OrderItem;
import com.enit.orderservice.domaine.model.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toDomain(OrderRequestDTO dto) {
        Order order = new Order();
        order.setCustomerId(dto.customerId());
        order.setDeliveryAddress(dto.deliveryAddress());

        List<OrderItem> items = dto.items() == null ? List.of() :
                dto.items().stream()
                        .map(i -> {
                            OrderItem item = new OrderItem();
                            item.setProductId(i.productId());
                            item.setQuantity(i.quantity());
                            item.setPrice(i.price());
                            item.setOrder(order); // important for bidirectional
                            return item;
                        })
                        .collect(Collectors.toList());


        order.setItems(items);
        order.recalculateTotal();
        order.setStatus(OrderStatus.CREATED);

        return order;
    }

    public static OrderResponseDTO toResponse(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(i -> new OrderItemDTO(
                        i.getProductId(),
                        i.getQuantity(),
                        i.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getOrderId(),
                order.getCustomerId(),
                order.getDeliveryAddress(),
                order.getTotalMoney(),
                order.getStatus().name(),
                items
        );
    }
}
