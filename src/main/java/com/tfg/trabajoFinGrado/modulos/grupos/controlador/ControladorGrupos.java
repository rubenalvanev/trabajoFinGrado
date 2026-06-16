package com.tfg.trabajoFinGrado.modulos.grupos.controlador;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.grupos.dto.PeticionCrearGrupo;
import com.tfg.trabajoFinGrado.modulos.grupos.dto.RespuestaGrupoDetalle;
import com.tfg.trabajoFinGrado.modulos.grupos.servicio.ServicioGrupos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ControladorGrupos {

    private final ServicioGrupos servicioGrupos;

    @GetMapping
    public ResponseEntity<RespuestaApi<List<RespuestaGrupoDetalle>>> obtenerTodos() {
        return ResponseEntity.ok(RespuestaApi.exito(servicioGrupos.obtenerTodosConDetalles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<RespuestaGrupoDetalle>> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(RespuestaApi.exito(servicioGrupos.obtenerDetalle(id)));
    }

    @PostMapping
    public ResponseEntity<RespuestaApi<RespuestaGrupoDetalle>> crear(
            @Valid @RequestBody PeticionCrearGrupo peticion) {
        RespuestaGrupoDetalle creado = servicioGrupos.crearGrupo(peticion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.exito("Grupo creado correctamente", creado));
    }

    @PutMapping("/{id}/modulos")
    public ResponseEntity<RespuestaApi<RespuestaGrupoDetalle>> actualizarModulos(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> cuerpo) {
        RespuestaGrupoDetalle actualizado = servicioGrupos.actualizarModulosDeGrupo(id, cuerpo.get("idsModulos"));
        return ResponseEntity.ok(RespuestaApi.exito("Módulos del grupo actualizados", actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        servicioGrupos.eliminarGrupo(id);
        return ResponseEntity.ok(RespuestaApi.exito("Grupo eliminado correctamente", null));
    }

    @PostMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<RespuestaApi<Void>> asignarUsuario(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId) {
        servicioGrupos.asignarUsuario(grupoId, usuarioId);
        return ResponseEntity.ok(RespuestaApi.exito("Usuario añadido al grupo", null));
    }

    @DeleteMapping("/{grupoId}/usuarios/{usuarioId}")
    public ResponseEntity<RespuestaApi<Void>> quitarUsuario(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId) {
        servicioGrupos.quitarUsuario(grupoId, usuarioId);
        return ResponseEntity.ok(RespuestaApi.exito("Usuario eliminado del grupo", null));
    }
}