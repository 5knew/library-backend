package com.aues.library.service;

import com.aues.library.model.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<Long> cartItemIds);
    Order getOrderById(Long id);
    List<Order> getAllOrders();
    List<Order> getOrdersByUserId(Long userId);
    Order updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);
}
