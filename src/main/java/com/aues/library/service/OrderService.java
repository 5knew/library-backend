package com.aues.library.service;

import com.aues.library.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<Long> cartItemIds);
    Order getOrderById(Long id);
    Page<Order> getAllOrders(Pageable pageable);
    List<Order> getOrdersByUserId(Long userId);
    Order updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);
}
