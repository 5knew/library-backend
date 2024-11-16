package com.aues.library.service;

import com.aues.library.dto.FilteredOrdersResponse;
import com.aues.library.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<Long> cartItemIds);
    Order getOrderById(Long id);
    FilteredOrdersResponse getAllOrders(Date startDate, Date endDate, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Page<Order> getOrdersByUserId(Long userId, Pageable pageable);
    Order updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);
    boolean cancelOrder(Long orderId);

    Page<Order> advancedSearch(String paymentStatus, BigDecimal minAmount, BigDecimal maxAmount,
                               LocalDate startDate, LocalDate endDate, Long bookId, Long bookCopyId,
                               List<Long> authorIds, List<Long> categoryIds, String sortField,
                               String sortDirection, Pageable pageable);
}

