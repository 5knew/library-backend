package com.aues.library.service.impl;

import com.aues.library.dto.FilteredOrdersResponse;
import com.aues.library.exceptions.OrderCreationException;
import com.aues.library.exceptions.OrderNotFoundException;
import com.aues.library.model.*;
import com.aues.library.model.Order;
import com.aues.library.repository.CartItemRepository;
import com.aues.library.repository.OrderRepository;
import com.aues.library.repository.PaymentRepository;
import com.aues.library.repository.UserRepository;
import com.aues.library.service.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, CartItemRepository cartItemRepository, PaymentRepository paymentRepository, EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentRepository = paymentRepository;
        this.entityManager = entityManager;
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
    public FilteredOrdersResponse getAllOrders(Date startDate, Date endDate, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        Specification<Order> spec = Specification.where(null);

        // Date range filtering
        if (startDate != null || endDate != null) {
            spec = spec.and((root, query, criteriaBuilder) -> {
                if (startDate != null && endDate != null) {
                    return criteriaBuilder.between(root.get("orderDate"), startDate, endDate);
                } else if (startDate != null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate);
                } else {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate);
                }
            });
        }

        // Amount range filtering
        if (minAmount != null || maxAmount != null) {
            spec = spec.and((root, query, criteriaBuilder) -> {
                if (minAmount != null && maxAmount != null) {
                    return criteriaBuilder.between(root.get("totalAmount"), minAmount, maxAmount);
                } else if (minAmount != null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
                } else {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), maxAmount);
                }
            });
        }

        // Fetch paginated orders
        Page<Order> orders = orderRepository.findAll(spec, pageable);

        // Calculate total sum of orders matching the specification
        BigDecimal totalSum = orderRepository.findAll(spec)
                .stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return the custom response with paginated orders and total sum
        return new FilteredOrdersResponse(orders, totalSum);
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

    @Override
    public Page<Order> advancedSearch(String paymentStatus, BigDecimal minAmount, BigDecimal maxAmount,
                                      LocalDate startDate, LocalDate endDate, Long bookId, Long bookCopyId,
                                      List<Long> authorIds, List<Long> categoryIds, String sortField,
                                      String sortDirection, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> orderRoot = query.from(Order.class);
        List<Predicate> predicates = new ArrayList<>();

        // Join with Payment if needed to add paymentStatus predicate
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            Join<Order, Payment> paymentJoin = orderRoot.join("payment", JoinType.LEFT);
            predicates.add(cb.equal(paymentJoin.get("paymentStatus"), paymentStatus));
        }

        // Add totalAmount predicates
        if (minAmount != null) {
            predicates.add(cb.greaterThanOrEqualTo(orderRoot.get("totalAmount"), minAmount));
        }
        if (maxAmount != null) {
            predicates.add(cb.lessThanOrEqualTo(orderRoot.get("totalAmount"), maxAmount));
        }

        // Add date predicates
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(orderRoot.get("orderDate"), java.sql.Date.valueOf(startDate)));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(orderRoot.get("orderDate"), java.sql.Date.valueOf(endDate)));
        }

        // Join and add conditions for related entities (book, bookCopy, authors, categories)
        if (bookId != null || bookCopyId != null || (authorIds != null && !authorIds.isEmpty()) || (categoryIds != null && !categoryIds.isEmpty())) {
            Join<Order, CartItem> cartItemJoin = orderRoot.join("cartItems", JoinType.LEFT);

            if (bookCopyId != null) {
                predicates.add(cb.equal(cartItemJoin.get("bookCopy").get("id"), bookCopyId));
            }

            if (bookId != null || (authorIds != null && !authorIds.isEmpty()) || (categoryIds != null && !categoryIds.isEmpty())) {
                Join<CartItem, BookCopy> bookCopyJoin = cartItemJoin.join("bookCopy", JoinType.LEFT);
                Join<BookCopy, Book> bookJoin = bookCopyJoin.join("book", JoinType.LEFT);

                if (bookId != null) {
                    predicates.add(cb.equal(bookJoin.get("id"), bookId));
                }

                if (authorIds != null && !authorIds.isEmpty()) {
                    Join<Book, Author> authorJoin = bookJoin.join("authors", JoinType.LEFT);
                    predicates.add(authorJoin.get("id").in(authorIds));
                }

                if (categoryIds != null && !categoryIds.isEmpty()) {
                    Join<Book, Category> categoryJoin = bookJoin.join("categories", JoinType.LEFT);
                    predicates.add(categoryJoin.get("id").in(categoryIds));
                }
            }
        }

        // Apply predicates to the query
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Handle sorting
        if (sortField != null && !sortField.isEmpty() && sortDirection != null && !sortDirection.isEmpty()) {
            Path<Object> sortPath = orderRoot.get(sortField);
            query.orderBy(sortDirection.equalsIgnoreCase("asc") ? cb.asc(sortPath) : cb.desc(sortPath));
        } else {
            query.orderBy(cb.desc(orderRoot.get("orderDate")));
        }


        // Execute the query with pagination
        TypedQuery<Order> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Count for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot)).where(cb.and(predicates.toArray(new Predicate[0])));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }




}
