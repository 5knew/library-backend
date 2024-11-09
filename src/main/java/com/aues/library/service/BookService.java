package com.aues.library.service;

import com.aues.library.dto.UpdateBookRequest;
import com.aues.library.model.Book;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookService {
    // Создание новой книги
    Book createBook(MultipartFile fullPdfFile, MultipartFile previewPdfFile, MultipartFile imageFile,
                    String name, List<Long> authorIds, String description,
                    List<Long> categoryIds, String isbn);

    // Получение книги по ID
    Book getBookById(Long id);

    // Получение всех книг
    List<Book> getAllBooks();

    // Обновление существующей книги
    Book updateBook(Long bookId, MultipartFile fullPdfFile, MultipartFile previewPdfFile, MultipartFile imageFile,
                    String name, List<Long> authorIds, String description,
                    List<Long> categoryIds, String isbn);

    // Удаление книги по ID
    void deleteBook(Long id);

    // Поиск книг по заданному критерию (например, по названию или автору)
    List<Book> searchBooks(String query);


    List<Book> searchBooksAdvanced(Optional<String> description, Optional<String> isbn,
                        Optional<List<Long>> authorIds, Optional<List<Long>> categoryIds);
}

