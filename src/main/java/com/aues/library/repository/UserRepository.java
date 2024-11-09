package com.aues.library.repository;

import com.aues.library.model.Book;
import com.aues.library.model.User;
import com.aues.library.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>{


    User findByEmail(String email);

    User findByRole(Role role);

    @Query("SELECT u.favorites FROM User u WHERE u.id = :userId")
    List<Book> findFavoritesByUserId(@Param("userId") Long userId);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long userId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long userId);
}

