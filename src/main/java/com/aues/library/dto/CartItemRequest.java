package com.aues.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Book Instance cannot be null")
    private Long bookCopyId;

    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

}


