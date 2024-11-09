package com.aues.library.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_LIBRARIAN, ROLE_STUDENT, ROLE_ADMIN, ROLE_CUSTOMER;

    @Override
    public String getAuthority() {
        return name();
    }
}
