package com.example.prgi.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrinciple implements UserDetails {

    private final User user;

    public UserPrinciple(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole();

        // ✅ Ensure role has ROLE_ prefix
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 👈 login with email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ✅ Helper to get role directly
    public String getRole() {
        return user.getRole();
    }
}