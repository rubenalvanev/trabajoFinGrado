package com.tfg.trabajoFinGrado.modulos.finanzas.controlador;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.finanzas.dto.PeticionRegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.dto.ResumenFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.dto.RespuestaRegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.TipoRegistro;
import com.tfg.trabajoFinGrado.modulos.finanzas.servicio.ServicioFinanzas;
import com.tfg.trabajoFinGrado.modulos.finanzas.servicio.ServicioPdfFinanzas;
import com.tfg.trabajoFinGrado.modulos.modulos.servicio.ServicioModulo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/finanzas")
@RequiredArgsConstructor
public class ControladorFinanzas {

    private final ServicioFinanzas servicioFinanzas;
    private final ServicioPdfFinanzas servicioPdf;
    private final ServicioModulo servicioModulo;

    private void verificarModuloActivo() {
        if (!servicioModulo.estaActivo("FINANZAS")) {
            throw new com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion(
                    "El módulo de Finanzas no está activo");
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<RespuestaRegistroFinanciero>>> obtenerTodos() {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioFinanzas.obtenerTodos()));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<RespuestaApi<List<RespuestaRegistroFinanciero>>> obtenerPorTipo(
            @PathVariable TipoRegistro tipo) {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioFinanzas.obtenerPorTipo(tipo)));
    }

    @GetMapping("/resumen")
    public ResponseEntity<RespuestaApi<ResumenFinanciero>> obtenerResumen() {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioFinanzas.obtenerResumen()));
    }

    @PostMapping
    public ResponseEntity<RespuestaApi<RespuestaRegistroFinanciero>> crear(
            @Valid @RequestBody PeticionRegistroFinanciero peticion,
            @AuthenticationPrincipal Usuario usuarioActual) {
        verificarModuloActivo();
        RespuestaRegistroFinanciero creado = servicioFinanzas.crear(peticion, usuarioActual);
        return ResponseEntity.ok(RespuestaApi.exito("Registro creado correctamente", creado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        verificarModuloActivo();
        servicioFinanzas.eliminar(id);
        return ResponseEntity.ok(RespuestaApi.exito("Registro eliminado correctamente", null));
    }

    @GetMapping("/pdf/mensual")
    public ResponseEntity<byte[]> descargarPdfMensual() {
        verificarModuloActivo();
        byte[] pdf = servicioPdf.generarInformeMensual();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"informe-financiero-mensual.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
