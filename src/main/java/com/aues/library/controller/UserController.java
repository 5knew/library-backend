package com.aues.library.controller;

import com.aues.library.model.Book;
import com.aues.library.model.User;
import com.aues.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get User by ID
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }


    // Update user by ID
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
        User user = userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(user);
    }

    // Delete user by ID
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // Get user's favorite book IDs
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<List<Long>> getUserFavoriteBookIds(@PathVariable Long userId) {
        List<Long> favoriteBookIds = userService.getUserFavoriteBookIds(userId); // Assuming this method exists in userService
        return ResponseEntity.ok(favoriteBookIds);
    }

    // Add a book to user's favorites
    @PostMapping("/{userId}/favorites/{bookId}")
    public ResponseEntity<Void> addUserFavorite(@PathVariable Long userId, @PathVariable Long bookId) {
        userService.addUserFavorite(userId, bookId);
        return ResponseEntity.ok().build();
    }


    // Remove a book from user's favorites
    @DeleteMapping("/{userId}/favorites/{bookId}")
    public ResponseEntity<Void> deleteUserFavorite(@PathVariable Long userId, @PathVariable Long bookId) {
        userService.deleteUserFavorite(userId, bookId);
        return ResponseEntity.noContent().build();
    }

    // Search user's favorite book IDs
    @GetMapping("/{userId}/favorites/search")
    public ResponseEntity<List<Long>> searchUserFavoriteBookIds(
            @PathVariable Long userId,
            @RequestParam Optional<String> name,
            @RequestParam Optional<Long> authorId,
            @RequestParam Optional<Long> categoryId) {
        List<Long> favoriteBookIds = userService.searchUserFavoriteBookIds(userId, name, authorId, categoryId);
        return ResponseEntity.ok(favoriteBookIds);
    }
}
