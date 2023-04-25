package com.example.rest;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

import java.util.HashMap;

public class J2eeAuthUserDetailService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    HashMap<String, String> users = new HashMap<>();

    public J2eeAuthUserDetailService() {
        users.put("admin", "AdminRole");
        users.put("user", "UserRole");
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        var details = (PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails) token.getDetails();
        var role = getRoleFromHashMap(token.getName());
        return new ApplicationUser(token.getName(), details.getGrantedAuthorities(), role);

    }

    private String getRoleFromHashMap(String username) {
        return users.get(username);
    }

}
