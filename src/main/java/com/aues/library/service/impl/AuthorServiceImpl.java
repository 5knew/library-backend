package com.aues.library.service.impl;

import com.aues.library.exceptions.AuthorNotFoundException;
import com.aues.library.model.Author;
import com.aues.library.repository.AuthorRepository;
import com.aues.library.repository.BookRepository;
import com.aues.library.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorServiceImpl(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Override
    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author with ID " + id + " not found"));
    }

    public List<Author> getAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        return authors.isEmpty() ? Collections.emptyList() : authors;
    }

    @Override
    public Author updateAuthor(Long id, Author updatedAuthor) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author with ID " + id + " not found"));

        existingAuthor.setName(updatedAuthor.getName());
        existingAuthor.setPhone(updatedAuthor.getPhone());
        existingAuthor.setAddress(updatedAuthor.getAddress());

        return authorRepository.save(existingAuthor);
    }

    @Override
    public void deleteAuthor(Long id) {
        // Проверяем, есть ли книги с данным автором
        if (bookRepository.existsByAuthorId(id)) {
            throw new IllegalStateException("Cannot delete author with ID " + id + " as they are associated with books.");
        }

        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFoundException("Author with ID " + id + " not found");
        }
        // Удаляем автора, если нет связанных книг
        authorRepository.deleteById(id);
    }

    @Override
    public List<Author> searchAuthors(Optional<String> name, Optional<String> phone, Optional<String> address) {
        Specification<Author> spec = Specification.where(
                        name.map(this::nameContains).orElse(null))
                .and(phone.map(this::phoneContains).orElse(null))
                .and(address.map(this::addressContains).orElse(null));

        return authorRepository.findAll(spec);
    }

    private Specification<Author> nameContains(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<Author> phoneContains(String phone) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
    }

    private Specification<Author> addressContains(String address) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%");
    }
}
