package com.aues.library.service.impl;

import com.aues.library.model.BookCopy;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.Date;

public class BookCopySpecification {

    public static Specification<BookCopy> getBookCopiesByCriteria(
            BigDecimal minPrice, BigDecimal maxPrice, Date startDate, Date endDate) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Filter by price
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            // Filter by publication date
            if (startDate != null && endDate != null) {
                predicate = cb.and(predicate, cb.between(root.get("publicationDate"), startDate, endDate));
            } else if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("publicationDate"), startDate));
            } else if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("publicationDate"), endDate));
            }


            return predicate;
        };
    }
}
