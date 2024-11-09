package com.aues.library.service.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.aues.library.model.Author;
import com.aues.library.model.Book;
import com.aues.library.model.BookCopy;
import com.aues.library.model.Category;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookSpecification {


    public static Specification<Book> hasDescription(String description) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Book> hasIsbn(String isbn) {
        return (root, query, cb) -> cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Book> hasAuthors(List<Long> authorIds) {
        return (root, query, cb) -> root.join("authors").get("id").in(authorIds);
    }

    public static Specification<Book> hasCategories(List<Long> categoryIds) {
        return (root, query, cb) -> root.join("categories").get("id").in(categoryIds);
    }
}

