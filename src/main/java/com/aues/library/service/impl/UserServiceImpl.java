package com.aues.library.service.impl;

import com.aues.library.exceptions.NoFavoritesFoundException;
import com.aues.library.exceptions.UserUpdateException;
import com.aues.library.model.Book;
import com.aues.library.model.User;
import com.aues.library.repository.BookRepository;
import com.aues.library.repository.UserRepository;
import com.aues.library.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Long> getUserFavoriteBookIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Extract book IDs from the favorites list
        return user.getFavorites().stream()
                .map(Book::getId) // Get the ID of each favorite book
                .collect(Collectors.toList());
    }


    @Override
    public void addUserFavorite(Long userId, Long bookId) {
        User user = getUserById(userId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + bookId));

        if (!user.getFavorites().contains(book)) {
            user.getFavorites().add(book);
            userRepository.save(user);
        }
    }

    @Override
    public void deleteUserFavorite(Long userId, Long bookId) {
        User user = getUserById(userId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + bookId));

        if (user.getFavorites().contains(book)) {
            user.getFavorites().remove(book);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> searchUserFavoriteBookIds(Long userId, Optional<String> name, Optional<Long> authorId,
                                                Optional<Long> categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Filter the favorites based on the search criteria
        List<Long> favoriteBookIds = user.getFavorites().stream()
                .filter(book -> name.map(n -> book.getName().toLowerCase().contains(n.toLowerCase())).orElse(true))
                .filter(book -> authorId.map(aId -> book.getAuthors().stream()
                                .anyMatch(authorObj -> authorObj.getId().equals(aId)))
                        .orElse(true))
                .filter(book -> categoryId.map(cId -> book.getCategories().stream()
                                .anyMatch(category -> category.getId().equals(cId)))
                        .orElse(true))
                .map(Book::getId) // Extract just the book IDs
                .collect(Collectors.toList());

        // If no favorites were found after filtering, throw an exception
        if (favoriteBookIds.isEmpty()) {
            throw new NoFavoritesFoundException("No favorite books found for the specified filters for user ID: " + userId);
        }

        return favoriteBookIds;
    }



    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public User updateUser(Long userId, User updatedUser) {
        User existingUser = getUserById(userId);

        if (updatedUser == null) {
            throw new IllegalArgumentException("Updated user details cannot be null");
        }

        // Safely update fields if non-null, otherwise retain existing values
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getEmail() != null) {
            // Check for email format
            if (!isValidEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Invalid email format");
            }
            // Check if email is unique (not already used by another user)
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(updatedUser.getEmail(), userId)) {
                throw new IllegalArgumentException("Email already exists for another user");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        String encryptedPassword = passwordEncoder.encode(updatedUser.getPassword());
        if (updatedUser.getPassword() != null && !existingUser.getPassword().equals(encryptedPassword)) {
            // Add password validation or encryption if needed
            existingUser.setPassword(encryptedPassword);
        }

        if (updatedUser.getStudentId() != null) {
            existingUser.setStudentId(updatedUser.getStudentId());
        }

        if (updatedUser.getCourse() != null) {
            existingUser.setCourse(updatedUser.getCourse());
        }

        if (updatedUser.getPhoneNumber() != null) {
            // Check if the phone number is unique for other users
            boolean exists = userRepository.existsByPhoneNumber(updatedUser.getPhoneNumber());
            if (exists) {
                throw new IllegalArgumentException("Phone number already in use by another user");
            }
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        try {
            return userRepository.save(existingUser);
        } catch (Exception e) {
            throw new UserUpdateException("Failed to update user with ID: " + userId, e);
        }
    }

    // Helper methods
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }





    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }




}
