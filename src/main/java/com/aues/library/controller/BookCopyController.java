package com.aues.library.controller;

import com.aues.library.dto.BookCopyRequest;
import com.aues.library.dto.UpdatedBookCopyRequest;
import com.aues.library.model.BookCopy;
import com.aues.library.service.BookCopyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/book-copies")
public class BookCopyController {

    private final BookCopyService bookCopyService;

    @Autowired
    public BookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    // Create a new BookCopy with MultipartFile

    @PostMapping
    public ResponseEntity<BookCopy> createBookCopy(
            @RequestParam Long bookId,
            @RequestParam BigDecimal price,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date publicationDate,  // Specify date format here
            @RequestParam String language,
            @RequestPart(required = false) MultipartFile fullPdf) {

        BookCopyRequest bookCopyRequest = new BookCopyRequest();
        bookCopyRequest.setBookId(bookId);
        bookCopyRequest.setPrice(price);
        bookCopyRequest.setPublicationDate(publicationDate);
        bookCopyRequest.setLanguage(language);
        bookCopyRequest.setFullPdf(fullPdf);

        BookCopy bookCopy = bookCopyService.createBookCopy(bookCopyRequest);
        return new ResponseEntity<>(bookCopy, HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<BookCopy> updateBookCopy(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date publicationDate,
            @RequestParam(required = false) String language,
            @RequestPart(required = false) MultipartFile fullPdf) {

        UpdatedBookCopyRequest updatedBookCopyRequest = new UpdatedBookCopyRequest();
        updatedBookCopyRequest.setPrice(price);
        updatedBookCopyRequest.setPublicationDate(publicationDate);
        updatedBookCopyRequest.setLanguage(language);
        updatedBookCopyRequest.setFullPdf(fullPdf);

        BookCopy bookCopy = bookCopyService.updateBookCopy(id, updatedBookCopyRequest);
        return new ResponseEntity<>(bookCopy, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookCopy> getBookCopyById(@PathVariable Long id) {
        BookCopy bookCopy = bookCopyService.getBookCopyById(id);
        return new ResponseEntity<>(bookCopy, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<BookCopy>> getAllBookCopies() {
        List<BookCopy> bookCopies = bookCopyService.getAllBookCopies();
        return new ResponseEntity<>(bookCopies, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookCopy(@PathVariable Long id) {
        bookCopyService.deleteBookCopy(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/by-book/{bookId}")
    public ResponseEntity<List<BookCopy>> getBookCopiesByBookId(@PathVariable Long bookId) {
        List<BookCopy> bookCopies = bookCopyService.getBookCopiesByBookId(bookId);
        return new ResponseEntity<>(bookCopies, HttpStatus.OK);
    }




    @GetMapping("/search")
    public ResponseEntity<List<BookCopy>> searchBookCopies(
            @RequestParam Optional<BigDecimal> minPrice,
            @RequestParam Optional<BigDecimal> maxPrice,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<Date> startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<Date> endDate) {

        List<BookCopy> bookCopies = bookCopyService.searchBookCopies(minPrice, maxPrice, startDate, endDate);
        return bookCopies.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(bookCopies, HttpStatus.OK);
    }
}
