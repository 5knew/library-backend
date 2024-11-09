package com.aues.library.repository;

import com.aues.library.model.Author;
import com.aues.library.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
    Optional<Author> findByNameIgnoreCase(String name);
}

