package com.tfg.trabajoFinGrado.modulos.modulos.controlador;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.modulos.dto.RespuestaModulo;
import com.tfg.trabajoFinGrado.modulos.modulos.servicio.ServicioModulo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/modulos")
@RequiredArgsConstructor
public class ControladorModulo {

    private final ServicioModulo servicioModulo;

    @GetMapping
    public ResponseEntity<RespuestaApi<List<RespuestaModulo>>> obtenerTodos(
            @AuthenticationPrincipal Usuario usuarioActual) {

        Set<String> activos = servicioModulo.obtenerClavesActivas();

        List<RespuestaModulo> modulos = servicioModulo.obtenerTodos()
                .stream()
                .map(m -> RespuestaModulo.builder()
                        .id(m.getId())
                        .clave(m.getClave())
                        .nombre(m.getNombre())
                        .descripcion(m.getDescripcion())
                        .icono(m.getIcono())
                        .obligatorio(m.isObligatorio())
                        .activo(activos.contains(m.getClave()))
                        .orden(m.getOrden())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(RespuestaApi.exito(modulos));
    }

    @GetMapping("/activos")
    public ResponseEntity<RespuestaApi<Set<String>>> obtenerActivos() {
        return ResponseEntity.ok(RespuestaApi.exito(servicioModulo.obtenerClavesActivas()));
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<Void>> activar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario admin) {
        servicioModulo.activar(id, admin);
        return ResponseEntity.ok(RespuestaApi.exito("Módulo activado correctamente", null));
    }

    @DeleteMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<Void>> desactivar(@PathVariable Long id) {
        servicioModulo.desactivar(id);
        return ResponseEntity.ok(RespuestaApi.exito("Módulo desactivado correctamente", null));
    }
}