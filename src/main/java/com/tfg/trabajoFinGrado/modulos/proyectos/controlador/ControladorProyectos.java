package com.tfg.trabajoFinGrado.modulos.proyectos.controlador;

import com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion;
import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.modulos.servicio.ServicioModulo;
import com.tfg.trabajoFinGrado.modulos.proyectos.dto.PeticionActualizarProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.dto.PeticionCrearProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.dto.RespuestaProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.Cliente;
import com.tfg.trabajoFinGrado.modulos.proyectos.servicio.ServicioProyectos;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ControladorProyectos {

    private final ServicioProyectos servicioProyectos;
    private final ServicioModulo servicioModulo;

    private void verificarModuloActivo() {
        if (!servicioModulo.estaActivo("PROYECTOS")) {
            throw new AccesoDenegadoExcepcion("El módulo de Proyectos no está activo");
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<RespuestaProyecto>>> obtenerTodos() {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioProyectos.obtenerTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<RespuestaProyecto>> obtenerPorId(@PathVariable Long id) {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioProyectos.obtenerPorId(id)));
    }

    @PostMapping
    public ResponseEntity<RespuestaApi<RespuestaProyecto>> crear(
            @Valid @RequestBody PeticionCrearProyecto peticion,
            @AuthenticationPrincipal Usuario usuarioActual) {
        verificarModuloActivo();
        RespuestaProyecto creado = servicioProyectos.crear(peticion, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.exito("Proyecto creado correctamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<RespuestaProyecto>> actualizar(
            @PathVariable Long id,
            @RequestBody PeticionActualizarProyecto peticion) {
        verificarModuloActivo();
        RespuestaProyecto actualizado = servicioProyectos.actualizar(id, peticion);
        return ResponseEntity.ok(RespuestaApi.exito("Proyecto actualizado correctamente", actualizado));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<RespuestaApi<RespuestaProyecto>> finalizar(@PathVariable Long id) {
        verificarModuloActivo();
        RespuestaProyecto finalizado = servicioProyectos.finalizar(id);
        return ResponseEntity.ok(RespuestaApi.exito("Proyecto finalizado correctamente", finalizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        verificarModuloActivo();
        servicioProyectos.eliminar(id);
        return ResponseEntity.ok(RespuestaApi.exito("Proyecto eliminado correctamente", null));
    }

    @GetMapping("/clientes")
    public ResponseEntity<RespuestaApi<List<Cliente>>> obtenerClientes() {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioProyectos.obtenerClientes()));
    }

    @PostMapping("/clientes")
    public ResponseEntity<RespuestaApi<Cliente>> crearCliente(
            @RequestBody Cliente cliente,
            @AuthenticationPrincipal Usuario usuarioActual) {
        verificarModuloActivo();
        Cliente creado = servicioProyectos.crearCliente(cliente, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.exito("Cliente creado correctamente", creado));
    }
}