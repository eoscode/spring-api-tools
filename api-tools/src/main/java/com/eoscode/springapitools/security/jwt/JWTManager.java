package com.eoscode.springapitools.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JWTManager {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private Long expiration;

    public String generateToken(String username) {

        return Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiration)))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
    }


    public boolean isValid(String token) {
        Claims claims = getClaims(token);
        if (claims != null) {
            String username = claims.getSubject();
            Date expirationDate = claims.getExpiration();
            Date now = new Date(System.currentTimeMillis());
            return username != null && expirationDate != null && now.before(expirationDate);
        }
        return false;
    }

    public String getUsername(String token) {
        Claims claims = getClaims(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    private Claims getClaims(String token) {
        try {
            SignatureAlgorithm sa = SignatureAlgorithm.HS512;
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), sa.getJcaName());
            final var jwtParser = Jwts.parser()
                    .verifyWith(secretKeySpec)
                    .build();

            return jwtParser.parseSignedClaims(token).getPayload();
        }
        catch (Exception e) {
            return null;
        }
    }

}
