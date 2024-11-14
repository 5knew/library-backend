package com.aues.library.service.impl;

import com.aues.library.exceptions.OrderCreationException;
import com.aues.library.exceptions.OrderNotFoundException;
import com.aues.library.model.CartItem;
import com.aues.library.model.Order;
import com.aues.library.model.Payment;
import com.aues.library.model.User;
import com.aues.library.repository.CartItemRepository;
import com.aues.library.repository.OrderRepository;
import com.aues.library.repository.PaymentRepository;
import com.aues.library.repository.UserRepository;
import com.aues.library.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, CartItemRepository cartItemRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, List<Long> cartItemIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderCreationException("User with ID " + userId + " not found"));

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);

        if (cartItems.isEmpty()) {
            throw new OrderCreationException("No valid cart items found for IDs: " + cartItemIds);
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(cartItem -> cartItem.getBookCopy().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setTotalAmount(totalAmount);

        // Save the order to generate its ID
        order = orderRepository.save(order);

        // Set the order reference for each cart item and update them
        for (CartItem cartItem : cartItems) {
            cartItem.setOrder(order);
        }
        cartItemRepository.saveAll(cartItems); // Save the updated cart items

        // Set the cart items in the order object if needed for return
        order.setCartItems(cartItems);

        return order;
    }


    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, Order updatedOrder) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));

        existingOrder.setOrderDate(updatedOrder.getOrderDate());
        existingOrder.setTotalAmount(updatedOrder.getTotalAmount());

        // Additional updates (cart items, etc.) can be handled here as needed

        return orderRepository.save(existingOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order with ID " + id + " not found");
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found for Order ID: " + orderId));

            // Check if the payment status is PENDING before canceling
            if ("PENDING".equalsIgnoreCase(payment.getPaymentStatus())) {
                payment.setPaymentStatus("CANCELED");
                paymentRepository.save(payment);
                return true;
            }
        }
        return false;
    }
}
