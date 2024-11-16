package com.aues.library.controller;

import com.aues.library.dto.FilteredOrdersResponse;
import com.aues.library.exceptions.OrderCreationException;
import com.aues.library.exceptions.OrderNotFoundException;
import com.aues.library.model.Order;
import com.aues.library.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestParam Long userId,
            @RequestBody List<Long> cartItemIds) { // Expecting a JSON array
        try {
            Order order = orderService.createOrder(userId, cartItemIds);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (OrderCreationException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping
    public FilteredOrdersResponse getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @PageableDefault(size = 10) Pageable pageable) {
        return orderService.getAllOrders(startDate, endDate, minAmount, maxAmount, pageable);
    }



    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Order>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderService.getOrdersByUserId(userId, pageable);

        return ordersPage.isEmpty() ?
                new ResponseEntity<>(HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(ordersPage, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        try {
            Order order = orderService.updateOrder(id, updatedOrder);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            boolean isCanceled = orderService.cancelOrder(orderId);
            if (isCanceled) {
                return new ResponseEntity<>("Order canceled successfully.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Order cannot be canceled.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error canceling order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<Page<Order>> advancedSearch(
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long bookCopyId,
            @RequestParam(required = false) List<Long> authorIds, // changed to List<Long>
            @RequestParam(required = false) List<Long> categoryIds, // changed to List<Long>
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            Pageable pageable
    ) {
        Page<Order> orders = orderService.advancedSearch(
                paymentStatus, minAmount, maxAmount, startDate, endDate,
                bookId, bookCopyId, authorIds, categoryIds, // passing lists
                sortField, sortDirection, pageable
        );
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


}
