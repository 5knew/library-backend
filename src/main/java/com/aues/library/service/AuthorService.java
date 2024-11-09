package com.aues.library.service;

import com.aues.library.model.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Author createAuthor(Author author);
    Author getAuthorById(Long id);
    List<Author> getAllAuthors();
    Author updateAuthor(Long id, Author updatedAuthor);
    void deleteAuthor(Long id);
    List<Author> searchAuthors(Optional<String> name, Optional<String> phone, Optional<String> address);
}
