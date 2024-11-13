package com.aues.library.repository;

import com.aues.library.model.CartItem;
import com.aues.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long>, JpaSpecificationExecutor<CartItem> {
    List<CartItem> findByUserId(Long userId);
    List<CartItem> findByBookCopyId(Long bookCopyId);
//    List<CartItem> findAll();
    List<CartItem> findByOrderIsNull();
    boolean existsByUserIdAndBookCopyId(Long userId, Long bookCopyId);
}
