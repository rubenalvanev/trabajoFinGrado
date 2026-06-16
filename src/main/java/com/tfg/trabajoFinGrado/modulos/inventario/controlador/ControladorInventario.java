package com.tfg.trabajoFinGrado.modulos.inventario.controlador;

import com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion;
import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.inventario.dto.PeticionStock;
import com.tfg.trabajoFinGrado.modulos.inventario.modelo.Stock;
import com.tfg.trabajoFinGrado.modulos.inventario.servicio.ServicioInventario;
import com.tfg.trabajoFinGrado.modulos.modulos.servicio.ServicioModulo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class ControladorInventario {

    private final ServicioInventario servicioInventario;
    private final ServicioModulo servicioModulo;

    private void verificarModuloActivo() {
        if (!servicioModulo.estaActivo("INVENTARIO")) {
            throw new AccesoDenegadoExcepcion("El módulo de Inventario no está activo");
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<Stock>>> obtenerTodos() {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioInventario.obtenerTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<Stock>> obtenerPorId(@PathVariable Long id) {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioInventario.obtenerPorId(id)));
    }

    @PostMapping
    public ResponseEntity<RespuestaApi<Stock>> crear(
            @Valid @RequestBody PeticionStock peticion,
            @AuthenticationPrincipal Usuario usuarioActual) {
        verificarModuloActivo();
        Stock stock = Stock.builder()
                .nombre(peticion.getNombre())
                .proveedor(peticion.getProveedor())
                .cantidadTotal(peticion.getCantidadTotal())
                .precio(peticion.getPrecio())
                .build();
        Stock creado = servicioInventario.crear(stock, usuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.exito("Stock creado correctamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaApi<Stock>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PeticionStock peticion) {
        verificarModuloActivo();
        Stock datosActualizados = Stock.builder()
                .nombre(peticion.getNombre())
                .proveedor(peticion.getProveedor())
                .cantidadTotal(peticion.getCantidadTotal())
                .precio(peticion.getPrecio())
                .build();
        Stock actualizado = servicioInventario.actualizar(id, datosActualizados);
        return ResponseEntity.ok(RespuestaApi.exito("Stock actualizado correctamente", actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        verificarModuloActivo();
        servicioInventario.eliminar(id);
        return ResponseEntity.ok(RespuestaApi.exito("Stock eliminado correctamente", null));
    }
}
