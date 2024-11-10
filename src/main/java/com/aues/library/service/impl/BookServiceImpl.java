package com.aues.library.service.impl;

import com.aues.library.controller.BookController;
import com.aues.library.exceptions.BookCreationException;
import com.aues.library.exceptions.BookDeletionException;
import com.aues.library.exceptions.BookNotFoundException;
import com.aues.library.exceptions.BookUpdateException;
import com.aues.library.model.Author;
import com.aues.library.model.Book;
import com.aues.library.model.Category;
import com.aues.library.repository.AuthorRepository;
import com.aues.library.repository.BookCopyRepository;
import com.aues.library.repository.BookRepository;
import com.aues.library.repository.CategoryRepository;
import com.aues.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
//@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CloudinaryService cloudinaryService;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           CategoryRepository categoryRepository,
                           BookCopyRepository bookCopyRepository,
                           CloudinaryService cloudinaryService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public Book createBook(MultipartFile fullPdfFile, MultipartFile previewPdfFile, MultipartFile imageFile,
                           String name, List<Long> authorIds, String description,
                           List<Long> categoryIds, String isbn) {
        try {
            // Upload files to Cloudinary if provided
            String fullPdfUrl = null;
            if (fullPdfFile != null && !fullPdfFile.isEmpty()) {
                fullPdfUrl = cloudinaryService.uploadFile(fullPdfFile);
            }
            String previewPdfUrl = null;
            if (previewPdfFile != null && !previewPdfFile.isEmpty()) {
                previewPdfUrl = cloudinaryService.uploadFile(previewPdfFile);
            }
            String imageUrl = imageFile != null && !imageFile.isEmpty() ? cloudinaryService.uploadFile(imageFile) : null;

            // Retrieve categories by IDs
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                throw new BookCreationException("Some categories were not found for the provided IDs.");
            }

            // Retrieve authors by IDs
            List<Author> authors = authorRepository.findAllById(authorIds);
            if (authors.size() != authorIds.size()) {
                throw new BookCreationException("Some authors were not found for the provided IDs.");
            }

            // Create and populate the new Book entity
            Book book = new Book();
            book.setName(name);
            book.setAuthors(authors);
            book.setFullPdf(fullPdfUrl);
            book.setPreviewPdf(previewPdfUrl);
            book.setPhotos(imageUrl != null ? Collections.singletonList(imageUrl) : Collections.emptyList());
            book.setDescription(description);
            book.setCategories(categories);
            book.setIsbn(isbn);

            return bookRepository.save(book);
        } catch (DataIntegrityViolationException e) {
            throw new BookCreationException("A book with the provided details already exists.");
        } catch (Exception e) {
            logger.error("Error creating book: ", e);
            throw new BookCreationException("Failed to create book");
        }
    }



    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book updateBook(Long bookId, MultipartFile fullPdfFile, MultipartFile previewPdfFile, MultipartFile imageFile,
                           String name, List<Long> authorIds, String description,
                           List<Long> categoryIds, String isbn) {

        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + bookId + " not found"));

        try {
            List<String> newPhotos = new ArrayList<>();
            if (imageFile != null && !imageFile.isEmpty()) {
                String photoUrl = handleFileUpload(imageFile, existingBook.getPhotos().isEmpty() ? null : existingBook.getPhotos().get(0));
                if (photoUrl != null) { // Only add if photoUrl is non-null
                    newPhotos.add(photoUrl);
                }
            } else {
                // Retain existing photos
                newPhotos.addAll(existingBook.getPhotos());
            }
            existingBook.setPhotos(newPhotos);

            // Replace other fields similarly
            existingBook.setFullPdf(handleFileUpload(fullPdfFile, existingBook.getFullPdf()));
            existingBook.setPreviewPdf(handleFileUpload(previewPdfFile, existingBook.getPreviewPdf()));
            existingBook.setName(name);
            existingBook.setDescription(description);
            existingBook.setIsbn(isbn);

            // Replace categories list
            if (categoryIds != null) {
                existingBook.setCategories(new ArrayList<>(retrieveOrCreateCategoriesByIds(categoryIds)));
            }

            // Replace authors list
            if (authorIds != null) {
                existingBook.setAuthors(new ArrayList<>(retrieveOrCreateAuthorsByIds(authorIds)));
            }

            return bookRepository.save(existingBook);
        } catch (Exception e) {
            logger.error("Error updating book", e);
            throw new BookUpdateException("Failed to update book with ID " + bookId, e);
        }
    }



    private String handleFileUpload(MultipartFile file, String existingUrl) {
        if (file != null && !file.isEmpty()) {
            if (existingUrl != null) {
                cloudinaryService.deleteFile(existingUrl);
            }
            return cloudinaryService.uploadFile(file);
        }
        // Provide a default URL or throw an exception if `file` is required
        return existingUrl != null ? existingUrl : "default-placeholder-url";
    }


    private List<Category> retrieveOrCreateCategoriesByIds(List<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        // Check if all requested categories were found
        if (categories.size() != categoryIds.size()) {
            throw new BookCreationException("Some categories were not found for the provided IDs.");
        }

        return categories;
    }


    private List<Author> retrieveOrCreateAuthorsByIds(List<Long> authorIds) {
        return authorRepository.findAllById(authorIds).stream()
                .peek(author -> {
                    if (author == null) throw new BookCreationException("Author not found for one or more IDs");
                }).collect(Collectors.toList());
    }



    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));

        try {
            // Delete PDF and images from Cloudinary
            if (book.getFullPdf() != null) cloudinaryService.deleteFile(book.getFullPdf());
            if (book.getPreviewPdf() != null) cloudinaryService.deleteFile(book.getPreviewPdf());
            book.getPhotos().forEach(cloudinaryService::deleteFile);

            // Delete all copies of this book
            bookCopyRepository.deleteAllByBook(book);

            // Delete the book
            bookRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error deleting book", e);
            throw new BookDeletionException("Failed to delete book with ID " + id, e);
        }
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.findByNameContainingIgnoreCaseOrAuthors_NameContainingIgnoreCase(query, query);
    }

    @Override
    public List<Book> searchBooksAdvanced(Optional<String> description, Optional<String> isbn,
                                          Optional<List<Long>> authorIds, Optional<List<Long>> categoryIds) {
        Specification<Book> spec = Specification.where(null);

        // Add specifications only if fields are present and non-empty
        if (description.isPresent() && !description.get().isEmpty()) {
            spec = spec.and(BookSpecification.hasDescription(description.get()));
        }
        if (isbn.isPresent() && !isbn.get().isEmpty()) {
            spec = spec.and(BookSpecification.hasIsbn(isbn.get()));
        }
        if (authorIds.isPresent() && !authorIds.get().isEmpty()) {
            spec = spec.and(BookSpecification.hasAuthors(authorIds.get()));
        }
        if (categoryIds.isPresent() && !categoryIds.get().isEmpty()) {
            spec = spec.and(BookSpecification.hasCategories(categoryIds.get()));
        }

        return bookRepository.findAll(spec);
    }

    public List<Book> getBooksByCategoryId(Long categoryId) {
        return bookRepository.findByCategories_Id(categoryId); // Ensure this method exists in your repository
    }





}


