package com.example.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/rs/masterData")
@Tag(name = "SecuredAPI", description = "Secured API Endpoints")
@Slf4j
public class SecuredAPI {


    @Secured({"ROLE_AdminRole","ROLE_UserRole"})
    @GetMapping("/secured")
    @Operation(summary = "Returns principal from secured service")
    public ResponseEntity<String> securedAPI(Principal principal, @Parameter(description = "An example parameter used for response") @RequestParam(name = "exampleParameter", required = false) String exampleParameter) {
        StringBuilder sb = new StringBuilder();
        sb.append("SECURED SERVICE OUTPUT").append(System.lineSeparator());
        sb.append("PRINCIPAL: ").append(principal.getName()).append(System.lineSeparator());
        sb.append("EXAMPLE PARAMETER: ").append(exampleParameter).append(System.lineSeparator());

        log.debug(sb.toString());
        return new ResponseEntity<> (sb.toString(), HttpStatus.OK);
    }

    @Secured({"ROLE_AdminRole"})
    @GetMapping("/securedAdmin")
    @Operation(summary = "Returns principal from secured ADMIN service")
    public ResponseEntity<String> securedAdminAPI(Principal principal, @Parameter(description = "An example parameter used for response") @RequestParam(name = "exampleParameter", required = false) String exampleParameter) {
        StringBuilder sb = new StringBuilder();
        sb.append("ADMIN SECURED SERVICE OUTPUT").append(System.lineSeparator());
        sb.append("PRINCIPAL: ").append(principal.getName()).append(System.lineSeparator());
        sb.append("EXAMPLE PARAMETER: ").append(exampleParameter).append(System.lineSeparator());

        log.debug(sb.toString());
        return new ResponseEntity<> (sb.toString(), HttpStatus.OK);}

}
