package com.eoscode.springapitools.security.jwt;


import com.eoscode.springapitools.exceptions.AuthorizationException;
import com.eoscode.springapitools.security.Auth;
import com.eoscode.springapitools.security.AuthenticationContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class JWTAuthenticationResource {

    @Autowired
    private JWTManager jwtManager;

    public ResponseEntity<String> refreshToken(HttpServletResponse response) {

        if (!AuthenticationContext.authenticated().isPresent()) {
            throw new AuthorizationException("invalid token.");
        }

        Auth<?> auth = AuthenticationContext.authenticated().get();
        String token = jwtManager.generateToken(auth.getUsername());
        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("access-control-expose-headers", "Authorization");

        String json = "{\"id\": \"" + auth.getId() + "\", "
                + "\"token\": \"" + token + "\"}";


        return ResponseEntity.ok(json);

    }

}
