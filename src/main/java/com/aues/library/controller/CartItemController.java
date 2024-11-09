package com.aues.library.controller;

import com.aues.library.dto.CartItemRequest;
import com.aues.library.exceptions.CartItemNotFoundException;
import com.aues.library.model.CartItem;
import com.aues.library.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("api/v1/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    @Autowired
    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping
    public ResponseEntity<CartItem> createCartItem(@RequestBody CartItemRequest cartItemRequest) {
        CartItem createdCartItem = cartItemService.createCartItem(cartItemRequest);
        return new ResponseEntity<>(createdCartItem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItem> getCartItemById(@PathVariable Long id) {
        try {
            CartItem cartItem = cartItemService.getCartItemById(id);
            return new ResponseEntity<>(cartItem, HttpStatus.OK);
        } catch (CartItemNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getAllCartItems() {
        List<CartItem> cartItems = cartItemService.getAllCartItems();
        return cartItems.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(cartItems, HttpStatus.OK);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartItem>> getCartItemsByUserId(@PathVariable Long userId) {
        List<CartItem> cartItems = cartItemService.searchCartItems(Optional.of(userId), Optional.empty());
        return cartItems.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(cartItems, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<CartItem> updateCartItem(@PathVariable Long id, @RequestBody CartItem updatedCartItem) {
        try {
            CartItem cartItem = cartItemService.updateCartItem(id, updatedCartItem);
            return new ResponseEntity<>(cartItem, HttpStatus.OK);
        } catch (CartItemNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCartItem(@PathVariable Long id) {
        try {
            cartItemService.deleteCartItem(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CartItemNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CartItem>> searchCartItems(
            @RequestParam Optional<Long> userId,
            @RequestParam Optional<Long> bookCopyId) {
        List<CartItem> cartItems = cartItemService.searchCartItems(userId, bookCopyId);
        return cartItems.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(cartItems, HttpStatus.OK);
    }
}
