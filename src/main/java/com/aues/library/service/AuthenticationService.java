package com.aues.library.service;


import com.aues.library.dto.JwtAuthenticationResponse;
import com.aues.library.dto.RefreshTokenRequest;
import com.aues.library.dto.SignInRequest;
import com.aues.library.dto.SignUpRequest;
import com.aues.library.model.User;

public interface AuthenticationService {

    User signup(SignUpRequest signUpRequest);
    JwtAuthenticationResponse signin(SignInRequest signInRequest);
    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
