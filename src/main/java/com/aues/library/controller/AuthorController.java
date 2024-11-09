package com.aues.library.controller;

import com.aues.library.exceptions.AuthorNotFoundException;
import com.aues.library.model.Author;
import com.aues.library.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/authors")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        Author createdAuthor = authorService.createAuthor(author);
        return new ResponseEntity<>(createdAuthor, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        try {
            Author author = authorService.getAuthorById(id);
            return new ResponseEntity<>(author, HttpStatus.OK);
        } catch (AuthorNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        return new ResponseEntity<>(authors, HttpStatus.OK); // Всегда возвращаем JSON-массив, даже если он пуст
    }


    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @RequestBody Author updatedAuthor) {
        try {
            Author author = authorService.updateAuthor(id, updatedAuthor);
            return new ResponseEntity<>(author, HttpStatus.OK);
        } catch (AuthorNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAuthor(@PathVariable Long id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Unable to delete author because they are associated with books.");
        } catch (AuthorNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Author>> searchAuthors(
            @RequestParam Optional<String> name,
            @RequestParam Optional<String> phone,
            @RequestParam Optional<String> address) {

        List<Author> authors = authorService.searchAuthors(name, phone, address);
        return authors.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(authors, HttpStatus.OK);
    }
}
