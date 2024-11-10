package com.aues.library.service;

import com.aues.library.dto.BookCopyRequest;
import com.aues.library.dto.UpdatedBookCopyRequest;
import com.aues.library.model.BookCopy;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookCopyService {
    BookCopy createBookCopy(BookCopyRequest bookCopyRequest);

    BookCopy getBookCopyById(Long id);

    List<BookCopy> getAllBookCopies();

    BookCopy updateBookCopy(Long id, UpdatedBookCopyRequest updatedBookCopyRequest);

    void deleteBookCopy(Long id);

    List<BookCopy> searchBookCopies(
            Optional<BigDecimal> minPrice, Optional<BigDecimal> maxPrice,
            Optional<Date> startDate, Optional<Date> endDate);

    List<BookCopy> getBookCopiesByBookId(Long bookId);
}