package com.aues.library.dto;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {

    private String token;
    private String refreshToken;
    private UserInfo userInfo; // Additional field for user info

    // UserInfo could be a simple DTO that includes the information you want to send to the client
    @Data
    public static class UserInfo {
        private Long id;
        private String email;
        private String name; // Add more fields as needed
        // Constructor, getters, and setters
    }
}
