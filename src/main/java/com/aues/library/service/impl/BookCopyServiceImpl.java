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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookCopyServiceImpl.class);
    private final S3Service s3Service;

    @Autowired
    public BookCopyServiceImpl(BookCopyRepository bookCopyRepository, BookRepository bookRepository, S3Service s3Service) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookRepository = bookRepository;
        this.s3Service = s3Service;
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
                // Upload file to S3 and save URL
                String fullPdfUrl = s3Service.uploadFile(bookCopyRequest.getFullPdf(), "books/" + bookCopy.getId() + "/fullPdf.pdf");
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
        }
    }

    public Page<BookCopy> getFilteredBookCopies(BigDecimal minPrice, BigDecimal maxPrice,
                                                Date startDate, Date endDate,
                                                String language, Long bookCopyId,
                                                Pageable pageable) {
        Specification<BookCopy> spec = Specification.where(null);

        // Filter by bookCopyId if provided
        if (bookCopyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), bookCopyId));
        }

        // Filter by price range if provided
        if (minPrice != null || maxPrice != null) {
            spec = spec.and((root, query, cb) -> {
                if (minPrice != null && maxPrice != null) {
                    return cb.between(root.get("price"), minPrice, maxPrice);
                } else if (minPrice != null) {
                    return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
                } else {
                    return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
                }
            });
        }

        // Filter by publication date range if provided
        if (startDate != null || endDate != null) {
            spec = spec.and((root, query, cb) -> {
                if (startDate != null && endDate != null) {
                    return cb.between(root.get("publicationDate"), startDate, endDate);
                } else if (startDate != null) {
                    return cb.greaterThanOrEqualTo(root.get("publicationDate"), startDate);
                } else {
                    return cb.lessThanOrEqualTo(root.get("publicationDate"), endDate);
                }
            });
        }

        // Filter by language if provided
        if (language != null && !language.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("language"), language));
        }

        return bookCopyRepository.findAll(spec, pageable);
    }





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
        BookCopy existingBookCopy = getBookCopyById(id);

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
            if (existingBookCopy.getFullPdf() != null) {
                String oldPdfKey = s3Service.extractKeyFromUrl(existingBookCopy.getFullPdf());
                s3Service.deleteFile(oldPdfKey);
            }
            String newFullPdfUrl = s3Service.uploadFile(updatedBookCopyRequest.getFullPdf(), "books/" + id + "/fullPdf.pdf");
            existingBookCopy.setFullPdf(newFullPdfUrl);
        }

        return bookCopyRepository.save(existingBookCopy);
    }


    @Override
    public void deleteBookCopy(Long id) {
        BookCopy bookCopy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new BookCopyNotFoundException("BookCopy with ID " + id + " not found."));

        // Delete the full PDF from S3 if it exists
        if (bookCopy.getFullPdf() != null) {
            String pdfKey = s3Service.extractKeyFromUrl(bookCopy.getFullPdf());
            s3Service.deleteFile(pdfKey);
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
