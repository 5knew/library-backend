package com.aues.library.service;

import com.aues.library.model.Book;
import com.aues.library.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Date;
import java.util.List;
import java.util.Optional;


public interface UserService {
    //User Management
    User getUserById(Long userId);
    User updateUser(Long userId, User updatedUser);
    void deleteUser(Long userId);

    // Favorites management
    List<Long> getUserFavoriteBookIds(Long userId);
    void addUserFavorite(Long userId, Long bookId);
    void deleteUserFavorite(Long userId, Long bookId);
    List<Long> searchUserFavoriteBookIds(Long userId, Optional<String> name, Optional<Long> authorId,
                                         Optional<Long> categoryId);


}
