package org.flitter.backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    private final String jwtSecret = "JWT_SECRET_GS2123141vdfsaevavfaafadfafasf";
    private final String jwtRefreshTokenSecret = "JWT_SECRET_GS2198765436dafeafwa2dafdsafsa";
    private final SecretKey secretAccessKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    private final SecretKey secretRefreshKey = Keys.hmacShaKeyFor(jwtRefreshTokenSecret.getBytes());

    public String generateAccessToken(String username, List<String> permissions) {
        // 15分钟
        long accessTokenExpirationMs = 15 * 60 * 1000;
        return Jwts.builder()
                .subject(username)
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + accessTokenExpirationMs))
                .signWith(secretAccessKey)
                .compact();
    }

    // 生成 Refresh Token
    public String generateRefreshToken(String username) {
        long refreshTokenExpirationMs = 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + refreshTokenExpirationMs))
                .signWith(secretRefreshKey)
                .compact();
    }

    public String getUsernameFromJwt(String token, boolean isAccessToken) {
        SecretKey key = isAccessToken ? secretAccessKey : secretRefreshKey;
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 验证 Token
    public boolean validateToken(String token, boolean isAccessToken,
                                 HttpServletRequest request) {
        try {
            SecretKey secret = isAccessToken ? secretAccessKey : secretRefreshKey;
            Jwts.parser().verifyWith(secret).build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            request.setAttribute("tokenExpired", "AccessToken expired");
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException |
                 IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // 验证 Token
    public boolean validateToken(String token, boolean isAccessToken) {
        try {
            SecretKey secret = isAccessToken ? secretAccessKey : secretRefreshKey;
            Jwts.parser().verifyWith(secret).build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException | ExpiredJwtException | MalformedJwtException | UnsupportedJwtException |
                 IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
