package com.aues.library.service.impl;

import com.aues.library.dto.CartItemRequest;
import com.aues.library.exceptions.BookCopyNotFoundException;
import com.aues.library.exceptions.CartItemNotFoundException;
import com.aues.library.exceptions.UserNotFoundException;
import com.aues.library.model.BookCopy;
import com.aues.library.model.CartItem;
import com.aues.library.model.User;
import com.aues.library.repository.BookCopyRepository;
import com.aues.library.repository.CartItemRepository;
import com.aues.library.repository.UserRepository;
import com.aues.library.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;


    @Override
    public CartItem createCartItem(CartItemRequest cartItemRequest) {
        // Validate User and BookCopy
        User user = userRepository.findById(cartItemRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + cartItemRequest.getUserId() + " not found"));
        BookCopy bookCopy = bookCopyRepository.findById(cartItemRequest.getBookCopyId())
                .orElseThrow(() -> new BookCopyNotFoundException("BookCopy with ID " + cartItemRequest.getBookCopyId() + " not found"));

        // Prevent duplicate CartItems for the same User and BookCopy
        if (cartItemRepository.existsByUserIdAndBookCopyId(cartItemRequest.getUserId(), cartItemRequest.getBookCopyId())) {
            throw new RuntimeException("CartItem for this user and book copy already exists.");
        }

        // Validate quantity
        if (cartItemRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        // Create and save the CartItem
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setBookCopy(bookCopy);
        cartItem.setQuantity(cartItemRequest.getQuantity());

        return cartItemRepository.save(cartItem);
    }



@Override
public CartItem getCartItemById(Long id) {
    return cartItemRepository.findById(id)
            .orElseThrow(() -> new CartItemNotFoundException("CartItem with ID " + id + " not found"));
}

@Override
public List<CartItem> getAllCartItems() {
    return cartItemRepository.findByOrderIsNull();
}

//    @Override
//    public CartItem updateCartItem(Long id, CartItemRequest updatedCartItemRequest) {
//        // Fetch existing CartItem
//        CartItem existingCartItem = cartItemRepository.findById(id)
//                .orElseThrow(() -> new CartItemNotFoundException("CartItem with ID " + id + " not found"));
//
//        // Validate and set User if provided
//        if (updatedCartItemRequest.getUserId() != null) {
//            User user = userRepository.findById(updatedCartItemRequest.getUserId())
//                    .orElseThrow(() -> new UserNotFoundException("User with ID " + updatedCartItemRequest.getUserId() + " not found"));
//            existingCartItem.setUser(user);
//        }
//
//        // Validate and set BookCopy if provided
//        if (updatedCartItemRequest.getBookCopyId() != null) {
//            BookCopy bookCopy = bookCopyRepository.findById(updatedCartItemRequest.getBookCopyId())
//                    .orElseThrow(() -> new BookCopyNotFoundException("BookCopy with ID " + updatedCartItemRequest.getBookCopyId() + " not found"));
//            existingCartItem.setBookCopy(bookCopy);
//        }
//
//        // Validate and set quantity if provided
//        if (updatedCartItemRequest.getQuantity() != null && updatedCartItemRequest.getQuantity() > 0) {
//            existingCartItem.setQuantity(updatedCartItemRequest.getQuantity());
//        } else if (updatedCartItemRequest.getQuantity() != null) {
//            throw new IllegalArgumentException("Quantity must be greater than zero.");
//        }
//
//        return cartItemRepository.save(existingCartItem);
//    }


@Override
public void deleteCartItem(Long id) {
    if (!cartItemRepository.existsById(id)) {
        throw new CartItemNotFoundException("CartItem with ID " + id + " not found");
    }
    cartItemRepository.deleteById(id);
}

@Override
public List<CartItem> searchCartItems(Optional<Long> userId, Optional<Long> bookCopyId) {
    Specification<CartItem> spec = Specification.where(
                    userId.map(this::userIdEqualsAndOrderIsNull).orElse(null))
            .and(bookCopyId.map(this::bookCopyIdEquals).orElse(null));

    return cartItemRepository.findAll(spec);
}

    @Override
    public List<CartItem> getCartItemsByOrderId(Long orderId) {
        return cartItemRepository.findByOrderId(orderId);
    }

    private Specification<CartItem> userIdEqualsAndOrderIsNull(Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("user").get("id"), userId),
                criteriaBuilder.isNull(root.get("order"))
        );
    }

private Specification<CartItem> bookCopyIdEquals(Long bookCopyId) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("bookCopy").get("id"), bookCopyId);
}
}
