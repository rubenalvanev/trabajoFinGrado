package com.tfg.trabajoFinGrado.modulos.autenticacion.controlador;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.autenticacion.dto.PeticionLogin;
import com.tfg.trabajoFinGrado.modulos.autenticacion.dto.RespuestaLogin;
import com.tfg.trabajoFinGrado.modulos.autenticacion.servicio.ServicioAutenticacion;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.repositorio.RepositorioUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ControladorAutenticacion {

    private final AuthenticationManager gestorAutenticacion;
    private final ServicioAutenticacion servicioAutenticacion;
    private final RepositorioUsuario repositorioUsuario;

    @PostMapping("/login")
    public ResponseEntity<RespuestaApi<RespuestaLogin>> login(@Valid @RequestBody PeticionLogin peticion) {
        gestorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(peticion.getEmail(), peticion.getContrasena())
        );

        Usuario usuario = repositorioUsuario.findByEmail(peticion.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        RespuestaLogin respuesta = servicioAutenticacion.generarRespuestaLogin(usuario);
        return ResponseEntity.ok(RespuestaApi.exito("Inicio de sesión exitoso", respuesta));
    }
}
