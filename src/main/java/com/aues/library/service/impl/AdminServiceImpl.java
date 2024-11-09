package com.aues.library.service.impl;

import com.aues.library.exceptions.UserNotFoundException;
import com.aues.library.model.User;
import com.aues.library.model.enums.Role;
import com.aues.library.repository.UserRepository;
import com.aues.library.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Temporal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AdminServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User addUser(User user) {
        // Validate mandatory fields are not null
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role is required.");
        }

        // Check that the email is unique
        boolean emailExists = userRepository.findByEmail(user.getEmail()) != null;
        if (emailExists) {
            throw new RuntimeException("Email already in use.");
        }

        // Encrypt the user's password
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        // Enrollment Current Date Logic
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateString = dateFormat.format(new Date());
        try {
            Date currentDate = dateFormat.parse(currentDateString);
            user.setEnrollmentDate(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // For fields like firstName and lastName, consider if they are mandatory
        // and implement similar null checks if required.

        // Optional fields (like studentId, course, enrollmentDate) don't need null checks
        // if they are truly optional and can be left as null in the database.

        // By default, set new users as enabled or not based on your application logic
        user.setEnabled(true); // Adjust as necessary

        // Save the new user to the repository
        return userRepository.save(user);
    }


    @Override
    public void updateUser(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (updatedUser.getFirstName() != null) {
            user.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            user.setLastName(updatedUser.getLastName());
        }
        // Be cautious with updating unique fields like email, ensure no conflicts
        if (updatedUser.getEmail() != null) {
            // Additional check for email uniqueness might be required here
            user.setEmail(updatedUser.getEmail());
        }
        // Consider encrypting the password before setting it
        if (updatedUser.getPassword() != (user.getPassword()) || updatedUser.getRole() != null) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // Ensure this is hashed
        }
        if (updatedUser.getRole() != null) {
            user.setRole(updatedUser.getRole()); // Ensure the new role is valid
        }
        if (updatedUser.getStudentId() != null) {
            user.setStudentId(updatedUser.getStudentId());
        }
        if (updatedUser.getCourse() != null) {
            user.setCourse(updatedUser.getCourse());
        }
        if (updatedUser.getEnrollmentDate() != null) {
            user.setEnrollmentDate(updatedUser.getEnrollmentDate());
        }
        // For boolean fields like `enabled`, consider if you want to allow updating to `false` explicitly
        user.setEnabled(updatedUser.isEnabled());


        userRepository.save(user);
    }



    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            // Throw an exception or handle the case where the user is not found
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}

