package com.example.rest;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;

public class ApplicationUser extends org.springframework.security.core.userdetails.User {
    private final String username;
    private final String role;

    public ApplicationUser(String username, Collection<? extends GrantedAuthority> authorities, String role) {
        super(username, "N/A", authorities);
        this.username = username;
        this.role = role;
    }

    private ApplicationUser(String username, String password, Collection<? extends GrantedAuthority> authorities, String role) {
        super(username, Optional.ofNullable(password).orElse("N/A"), authorities);
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

}
