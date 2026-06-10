package com.tfg.trabajoFinGrado.modulos.usuarios.controlador;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.usuarios.dto.PeticionCrearUsuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.dto.PeticionEditarPerfil;
import com.tfg.trabajoFinGrado.modulos.usuarios.dto.RespuestaUsuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.servicio.ServicioUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class ControladorUsuario {

    private final ServicioUsuario servicioUsuario;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<List<RespuestaUsuario>>> obtenerTodos() {
        return ResponseEntity.ok(RespuestaApi.exito(servicioUsuario.obtenerTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<RespuestaUsuario>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioActual) {
        if (!usuarioActual.getId().equals(id) && !usuarioActual.esAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(RespuestaApi.error("Acceso denegado"));
        }
        return ResponseEntity.ok(RespuestaApi.exito(servicioUsuario.obtenerPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<RespuestaUsuario>> crear(
            @Valid @RequestBody PeticionCrearUsuario peticion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.exito("Usuario creado correctamente",
                        servicioUsuario.crear(peticion)));
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<RespuestaApi<RespuestaUsuario>> editarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody PeticionEditarPerfil peticion,
            @AuthenticationPrincipal Usuario usuarioActual) {
        return ResponseEntity.ok(RespuestaApi.exito("Perfil actualizado correctamente",
                servicioUsuario.editarPerfil(id, peticion, usuarioActual)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        servicioUsuario.eliminar(id);
        return ResponseEntity.ok(RespuestaApi.exito("Usuario eliminado correctamente", null));
    }
}