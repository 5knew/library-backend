package com.aues.library.controller;

import com.aues.library.dto.BookCreationRequest;
import com.aues.library.dto.UpdateBookRequest;
import com.aues.library.exceptions.BookCreationException;
import com.aues.library.exceptions.BookDeletionException;
import com.aues.library.exceptions.BookNotFoundException;
import com.aues.library.exceptions.BookUpdateException;
import com.aues.library.model.Book;
import com.aues.library.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper; // Ensure ObjectMapper is imported

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/admin/books")
public class BookController {

    private final BookService bookService;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // POST endpoint to create a new book
    @PostMapping
    public ResponseEntity<Book> createBook(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("isbn") String isbn,
            @RequestParam("authorIds") List<Long> authorIds,
            @RequestParam("categoryIds") List<Long> categoryIds,
            @RequestParam(value = "fullPdfFile", required = true) MultipartFile fullPdfFile,
            @RequestParam(value = "previewPdfFile", required = true) MultipartFile previewPdfFile,
            @RequestParam(value = "imageFile", required = true) MultipartFile imageFile
    ) {
        Book book = bookService.createBook(
                fullPdfFile, previewPdfFile, imageFile, name, authorIds, description, categoryIds, isbn
        );
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }


    // GET endpoint to retrieve a book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (BookNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // GET endpoint to retrieve all books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return books.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(books, HttpStatus.OK);
    }

    // PUT endpoint to update a book by ID
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("isbn") String isbn,
            @RequestParam("authorIds") List<Long> authorIds,
            @RequestParam("categoryIds") List<Long> categoryIds,
            @RequestParam(value = "fullPdfFile", required = false) MultipartFile fullPdfFile,
            @RequestParam(value = "previewPdfFile", required = false) MultipartFile previewPdfFile,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        Book updatedBook = bookService.updateBook(
                id, fullPdfFile, previewPdfFile, imageFile, name, authorIds, description, categoryIds, isbn
        );
        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }


    // DELETE endpoint to delete a book by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (BookNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (BookDeletionException e) {
            logger.error("Error deleting book: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // GET endpoint for basic book search by query
    @GetMapping("/search-basic")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        List<Book> books = bookService.searchBooks(query);
        return books.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/search-advanced")
    public ResponseEntity<List<Book>> searchBooksAdvanced(
            @RequestParam Optional<String> description,
            @RequestParam Optional<String> isbn,
            @RequestParam Optional<List<Long>> authorIds,
            @RequestParam Optional<List<Long>> categoryIds) {

        logger.debug("Received search request with filters: description={}, isbn={}, authorIds={}, categoryIds={}",
                description, isbn, authorIds, categoryIds);

        // Modify the service call to ignore empty fields
        List<Book> results = bookService.searchBooksAdvanced(
                description.filter(desc -> !desc.isEmpty()),  // Only add filter if non-empty
                isbn.filter(isb -> !isb.isEmpty()),            // Only add filter if non-empty
                authorIds, categoryIds);

        if (results.isEmpty()) {
            logger.debug("No books found matching filters.");
            return ResponseEntity.noContent().build();  // Returns 204 NO_CONTENT
        } else {
            return ResponseEntity.ok(results);
        }
    }


}
