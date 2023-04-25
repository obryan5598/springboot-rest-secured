package com.example.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
        ,securedEnabled = true
        )
public class SecurityConfig {

    PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        J2eePreAuthenticatedProcessingFilter filter = new J2eePreAuthenticatedProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource authenticationDetailsSource = new J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource();
        authenticationDetailsSource.setMappableRolesRetriever(() -> Set.of(
                "AdminRole",
                "UserRole"));
        filter.setAuthenticationDetailsSource(authenticationDetailsSource);
        authenticationProvider.setPreAuthenticatedUserDetailsService(new J2eeAuthUserDetailService());

        return http
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(filter, BasicAuthenticationFilter.class)
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/rs/monitor")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/**")
                .hasAnyRole("AdminRole","UserRole")
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(authenticationProvider));
    }

}
