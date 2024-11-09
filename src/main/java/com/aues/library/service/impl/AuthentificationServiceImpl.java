package com.aues.library.service.impl;

import com.aues.library.dto.JwtAuthenticationResponse;
import com.aues.library.dto.RefreshTokenRequest;
import com.aues.library.dto.SignInRequest;
import com.aues.library.dto.SignUpRequest;
import com.aues.library.model.User;
import com.aues.library.model.enums.Role;
import com.aues.library.repository.UserRepository;
import com.aues.library.service.AuthenticationService;
import com.aues.library.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthentificationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public User signup(SignUpRequest signUpRequest){
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setRole(Role.ROLE_STUDENT);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        // Enrollment Current Date Logic
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateString = dateFormat.format(new Date());
        try {
            Date currentDate = dateFormat.parse(currentDateString);
            user.setEnrollmentDate(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return userRepository.save(user);
    }
    public JwtAuthenticationResponse signin(SignInRequest signInRequest) throws IllegalArgumentException{
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword()));

        var user = userRepository.findByEmail(signInRequest.getEmail());
        var jwt = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);

        // Set user information
        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setName(user.getFirstName()); // Assuming these fields exist in your User entity
        // Set more fields as needed

        jwtAuthenticationResponse.setUserInfo(userInfo);

        return jwtAuthenticationResponse;
    }


    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest){
        String userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
        User user = userRepository.findByEmail(userEmail);
        if(jwtService.isTokenValid(refreshTokenRequest.getToken(), user)){
            var jwt = jwtService.generateToken(user);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
            return jwtAuthenticationResponse;



        }
        return null;
    }
}
