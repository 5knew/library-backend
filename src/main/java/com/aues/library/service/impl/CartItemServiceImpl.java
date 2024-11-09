package com.aues.library.service.impl;

import com.aues.library.dto.CartItemRequest;
import com.aues.library.exceptions.CartItemNotFoundException;
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
        CartItem cartItem = new CartItem();

        // Fetch user and book copy by ID
        User user = userRepository.findById(cartItemRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        BookCopy bookCopy = bookCopyRepository.findById(cartItemRequest.getBookCopyId())
                .orElseThrow(() -> new RuntimeException("BookCopy not found"));

        // Set the entities
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
        return cartItemRepository.findAll();
    }

    @Override
    public CartItem updateCartItem(Long id, CartItem updatedCartItem) {
        CartItem existingCartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemNotFoundException("CartItem with ID " + id + " not found"));

        existingCartItem.setUser(updatedCartItem.getUser());
        existingCartItem.setBookCopy(updatedCartItem.getBookCopy());
        existingCartItem.setQuantity(updatedCartItem.getQuantity());

        return cartItemRepository.save(existingCartItem);
    }

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
                        userId.map(this::userIdEquals).orElse(null))
                .and(bookCopyId.map(this::bookCopyIdEquals).orElse(null));

        return cartItemRepository.findAll(spec);
    }

    private Specification<CartItem> userIdEquals(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    private Specification<CartItem> bookCopyIdEquals(Long bookCopyId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("bookCopy").get("id"), bookCopyId);
    }
}
