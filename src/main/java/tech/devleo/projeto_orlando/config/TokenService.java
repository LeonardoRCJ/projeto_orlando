package tech.devleo.projeto_orlando.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    /**
     * Gera um token JWT para o usuário
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * Valida o token JWT e retorna o DecodedJWT se válido
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (Exception e) {
            throw new JWTVerificationException("Token validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extrai o username do token
     */
    public String getUsername(DecodedJWT decodedJWT) {
        String email = decodedJWT.getClaim("email").asString();
        if (email != null && !email.isEmpty()) {
            return email;
        }
        return decodedJWT.getSubject();
    }
}
