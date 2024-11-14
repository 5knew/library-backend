package com.aues.library.repository;

import com.aues.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    // Search for books by name with a case-insensitive match
    @Query("SELECT COUNT(b) > 0 FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    boolean existsByAuthorId(@Param("authorId") Long authorId);
    Page<Book> findAll(Pageable pageable);

    List<Book> findByNameContainingIgnoreCaseOrAuthors_NameContainingIgnoreCase(String query, String query1);

    List<Book> findByCategories_Id(Long categoryId);
}
