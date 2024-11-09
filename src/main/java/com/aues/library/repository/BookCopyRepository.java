package com.aues.library.repository;

import com.aues.library.model.BookCopy;
import com.aues.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long>, JpaSpecificationExecutor<BookCopy> {

    // Delete all copies of a specific book
    void deleteAllByBook(Book book);
}
