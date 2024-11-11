package com.aues.library.service.impl;

import com.aues.library.dto.BookCopyRequest;
import com.aues.library.dto.UpdatedBookCopyRequest;
import com.aues.library.exceptions.BookCopyNotFoundException;
import com.aues.library.exceptions.BookCreationException;
import com.aues.library.model.Book;
import com.aues.library.model.BookCopy;
import com.aues.library.repository.BookCopyRepository;
import com.aues.library.repository.BookRepository;
import com.aues.library.service.BookCopyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final CloudinaryService cloudinaryService;
    private final BookRepository bookRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookCopyServiceImpl.class);

    @Autowired
    public BookCopyServiceImpl(BookCopyRepository bookCopyRepository, CloudinaryService cloudinaryService, BookRepository bookRepository) {
        this.bookCopyRepository = bookCopyRepository;
        this.cloudinaryService = cloudinaryService;
        this.bookRepository = bookRepository;
    }

    @Override
    public BookCopy createBookCopy(BookCopyRequest bookCopyRequest) throws BookCreationException {
        // Validate book existence
        Book book = bookRepository.findById(bookCopyRequest.getBookId())
                .orElseThrow(() -> new BookCreationException("Book with ID " + bookCopyRequest.getBookId() + " not found."));

        // Initialize and save BookCopy to get the ID
        BookCopy bookCopy = new BookCopy();
        bookCopy.setBook(book);
        bookCopy.setPrice(bookCopyRequest.getPrice());
        bookCopy.setPublicationDate(bookCopyRequest.getPublicationDate());
        bookCopy.setLanguage(bookCopyRequest.getLanguage());

        try {
            // Save BookCopy to get its ID
            bookCopy = bookCopyRepository.save(bookCopy);

            // Handle PDF file upload and save metadata if present
            if (bookCopyRequest.getFullPdf() != null && !bookCopyRequest.getFullPdf().isEmpty()) {
                // Upload file and save metadata
                String fullPdfUrl = cloudinaryService.uploadFile(bookCopyRequest.getFullPdf(), bookCopy.getId());
                bookCopy.setFullPdf(fullPdfUrl); // Save the URL to the BookCopy for reference
                bookCopy = bookCopyRepository.save(bookCopy); // Save the updated BookCopy with the PDF URL
            }

            return bookCopy;
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating book copy: ", e);
            throw new BookCreationException("A book copy with similar details already exists.");
        } catch (Exception e) {
            logger.error("Error creating book copy: ", e);
            throw new BookCreationException("Failed to create book copy.");
        }}

    @Override
    public BookCopy getBookCopyById(Long id) {
        return bookCopyRepository.findById(id)
                .orElseThrow(() -> new BookCopyNotFoundException("BookCopy with ID " + id + " not found."));
    }

    @Override
    public List<BookCopy> getAllBookCopies() {
        return bookCopyRepository.findAll();
    }

    @Override
    public BookCopy updateBookCopy(Long id, UpdatedBookCopyRequest updatedBookCopyRequest) {
        BookCopy existingBookCopy = getBookCopyById(id);  // Retrieve existing book copy

        // Update each field only if the new value is not null
        if (updatedBookCopyRequest.getPrice() != null) {
            existingBookCopy.setPrice(updatedBookCopyRequest.getPrice());
        }

        if (updatedBookCopyRequest.getPublicationDate() != null) {
            existingBookCopy.setPublicationDate(updatedBookCopyRequest.getPublicationDate());
        }

        if (updatedBookCopyRequest.getLanguage() != null) {
            existingBookCopy.setLanguage(updatedBookCopyRequest.getLanguage());
        }

        if (updatedBookCopyRequest.getFullPdf() != null && !updatedBookCopyRequest.getFullPdf().isEmpty()) {
            String newFullPdfUrl = cloudinaryService.uploadFile(updatedBookCopyRequest.getFullPdf(), id);
            existingBookCopy.setFullPdf(newFullPdfUrl);  // Update full PDF URL only if a new file is provided
        }

        // Save and return the updated BookCopy
        return bookCopyRepository.save(existingBookCopy);
    }


    @Override
    public void deleteBookCopy(Long id) {
        if (!bookCopyRepository.existsById(id)) {
            throw new BookCopyNotFoundException("BookCopy with ID " + id + " not found.");
        }
        bookCopyRepository.deleteById(id);
    }

    @Override
    public List<BookCopy> searchBookCopies(
            Optional<BigDecimal> minPrice, Optional<BigDecimal> maxPrice,
            Optional<Date> startDate, Optional<Date> endDate) {
        Specification<BookCopy> spec =BookCopySpecification.getBookCopiesByCriteria(
                minPrice.orElse(null),
                maxPrice.orElse(null),
                startDate.orElse(null),
                endDate.orElse(null)
        );
        return bookCopyRepository.findAll(spec);
    }

    @Override
    public List<BookCopy> getBookCopiesByBookId(Long bookId) {
        return bookCopyRepository.findByBookId(bookId);
    }




}
