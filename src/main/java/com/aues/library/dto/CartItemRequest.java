package com.aues.library.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long userId;
    private Long bookCopyId;
    private int quantity;

}

