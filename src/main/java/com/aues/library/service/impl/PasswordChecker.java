package com.aues.library.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordChecker {
    private final PasswordEncoder passwordEncoder;
    public static void main(String[] args) {
        Boolean pass = new BCryptPasswordEncoder().matches("student" ,"$2a$10$v58DebwM88hxClKSgC/0VOJ1vHPtN0ewgEXmc98CjzjL1X/L5mUBm");
        System.out.println(pass);
    }

}
