package com.tfg.trabajoFinGrado.modulos.autenticacion.servicio;

import com.tfg.trabajoFinGrado.modulos.autenticacion.dto.PeticionLogin;
import com.tfg.trabajoFinGrado.modulos.autenticacion.dto.RespuestaLogin;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.seguridad.ServicioJwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAutenticacion {

    private final AuthenticationManager gestorAutenticacion;
    private final ServicioJwt servicioJwt;

    public RespuestaLogin iniciarSesion(PeticionLogin peticion) {
        gestorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(peticion.getEmail(), peticion.getContrasena())
        );

        return null;
    }

    public RespuestaLogin generarRespuestaLogin(Usuario usuario) {
        String token = servicioJwt.generarToken(usuario);

        return RespuestaLogin.builder()
                .token(token)
                .idUsuario(usuario.getId())
                .nombre(usuario.obtenerNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .grupo(usuario.getGrupo() != null ? usuario.getGrupo().getNombre() : null)
                .build();
    }
}
