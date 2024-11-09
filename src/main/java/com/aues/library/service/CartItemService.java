package com.aues.library.service;

import com.aues.library.dto.CartItemRequest;
import com.aues.library.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemService {
    CartItem createCartItem(CartItemRequest cartItemRequest);
    CartItem getCartItemById(Long id);
    List<CartItem> getAllCartItems();
    CartItem updateCartItem(Long id, CartItem updatedCartItem);
    void deleteCartItem(Long id);
    List<CartItem> searchCartItems(Optional<Long> userId, Optional<Long> bookCopyId);
}
