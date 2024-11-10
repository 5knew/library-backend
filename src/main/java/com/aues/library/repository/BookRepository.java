package com.aues.library.repository;

import com.aues.library.model.Book;
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

    List<Book> findByNameContainingIgnoreCaseOrAuthors_NameContainingIgnoreCase(String query, String query1);

    List<Book> findByCategories_Id(Long categoryId);
}
