package com.tfg.trabajoFinGrado.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class ServicioJwt {

    @Value("${trabajoFinGrado.jwt.secreto}")
    private String secreto;

    @Value("${trabajoFinGrado.jwt.expiracion}")
    private long expiracion;

    public String generarToken(UserDetails userDetails) {
        Map<String, Object> reclamaciones = new HashMap<>();
        return construirToken(reclamaciones, userDetails.getUsername());
    }

    public String extraerUsuario(String token) {
        return extraerReclamacion(token, Claims::getSubject);
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String usuario = extraerUsuario(token);
        return usuario.equals(userDetails.getUsername()) && !haExpirado(token);
    }

    private String construirToken(Map<String, Object> reclamaciones, String sujeto) {
        return Jwts.builder()
                .claims(reclamaciones)
                .subject(sujeto)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiracion))
                .signWith(obtenerClaveSecreta())
                .compact();
    }

    private boolean haExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Date extraerExpiracion(String token) {
        return extraerReclamacion(token, Claims::getExpiration);
    }

    private <T> T extraerReclamacion(String token, Function<Claims, T> resolvedor) {
        final Claims reclamaciones = extraerTodasLasReclamaciones(token);
        return resolvedor.apply(reclamaciones);
    }

    private Claims extraerTodasLasReclamaciones(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClaveSecreta() {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }
}
