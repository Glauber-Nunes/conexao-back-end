package com.br.gsistemas.conexao.config;


import com.br.gsistemas.conexao.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "chaveSuperSecretaParaJWTToken123456789!";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String gerarToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getUsuarioId()) // Adiciona o ID do usuário ao token
                .claim("fotoUrl", user.getFotoUrl()) //Adiciona a URL da foto
                .claim("nome", user.getNome()) // Adicionando o nome no token
                .claim("authorities", Collections.singletonList("ROLE_" + user.getTipo().name())) //Adiciona "ROLE_MOTORISTA"
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 5)) // 5 anos
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extrairNome(String token) {
        return extrairClaims(token).get("nome", String.class);
    }

    private boolean isTokenExpirado(String token) {
        Date dataExpiracao = extrairClaims(token).getExpiration();
        return dataExpiracao.before(new Date());
    }

    public Claims extrairClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //Método que valida se o token é válido
    public boolean validarToken(String token, UserDetails userDetails) {
        String email = extrairEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpirado(token);
    }
}
