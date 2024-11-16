package com.aues.library.dto;

import com.aues.library.model.Order;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;


public class FilteredOrdersResponse {
    private Page<Order> orders;
    private BigDecimal totalSum;

    public FilteredOrdersResponse(Page<Order> orders, BigDecimal totalSum) {
        this.orders = orders;
        this.totalSum = totalSum;
    }

    public Page<Order> getOrders() {
        return orders;
    }

    public BigDecimal getTotalSum() {
        return totalSum;
    }
}

