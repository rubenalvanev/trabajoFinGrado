package com.tfg.trabajoFinGrado.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private final ServicioJwt servicioJwt;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest peticion,
            @NonNull HttpServletResponse respuesta,
            @NonNull FilterChain cadenaFiltros
    ) throws ServletException, IOException {

        final String encabezadoAutorizacion = peticion.getHeader("Authorization");

        if (encabezadoAutorizacion == null || !encabezadoAutorizacion.startsWith("Bearer ")) {
            cadenaFiltros.doFilter(peticion, respuesta);
            return;
        }

        try {
            final String token = encabezadoAutorizacion.substring(7);
            final String emailUsuario = servicioJwt.extraerUsuario(token);

            if (emailUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails detallesUsuario = userDetailsService.loadUserByUsername(emailUsuario);

                if (servicioJwt.esTokenValido(token, detallesUsuario)) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    detallesUsuario, null, detallesUsuario.getAuthorities()
                            );
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(peticion));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (Exception ex) {
            log.warn("Token JWT inválido: {}", ex.getMessage());
        }

        cadenaFiltros.doFilter(peticion, respuesta);
    }
}