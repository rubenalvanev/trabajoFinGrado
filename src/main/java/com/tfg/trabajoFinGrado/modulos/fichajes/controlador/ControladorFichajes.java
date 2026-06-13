package com.tfg.trabajoFinGrado.modulos.fichajes.controlador;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.fichajes.dto.ResumenMensual;
import com.tfg.trabajoFinGrado.modulos.fichajes.dto.RespuestaFichaje;
import com.tfg.trabajoFinGrado.modulos.fichajes.servicio.ServicioFichajes;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fichajes")
@RequiredArgsConstructor
public class ControladorFichajes {

    private final ServicioFichajes servicioFichajes;

    @PostMapping("/entrada")
    public ResponseEntity<RespuestaApi<RespuestaFichaje>> registrarEntrada(
            @AuthenticationPrincipal Usuario usuarioActual) {
        RespuestaFichaje fichaje = servicioFichajes.registrarEntrada(usuarioActual);
        return ResponseEntity.ok(RespuestaApi.exito("Entrada registrada correctamente", fichaje));
    }

    @PostMapping("/salida")
    public ResponseEntity<RespuestaApi<RespuestaFichaje>> registrarSalida(
            @AuthenticationPrincipal Usuario usuarioActual) {
        RespuestaFichaje fichaje = servicioFichajes.registrarSalida(usuarioActual);
        return ResponseEntity.ok(RespuestaApi.exito("Salida registrada correctamente", fichaje));
    }

    @GetMapping("/hoy")
    public ResponseEntity<RespuestaApi<RespuestaFichaje>> obtenerFichajeHoy(
            @AuthenticationPrincipal Usuario usuarioActual) {
        RespuestaFichaje fichaje = servicioFichajes.obtenerFichajeHoy(usuarioActual);
        return ResponseEntity.ok(RespuestaApi.exito(fichaje));
    }

    @GetMapping("/resumen-mensual")
    public ResponseEntity<RespuestaApi<ResumenMensual>> obtenerResumenMensual(
            @RequestParam(defaultValue = "0") int anio,
            @RequestParam(defaultValue = "0") int mes,
            @AuthenticationPrincipal Usuario usuarioActual) {

        LocalDate ahora = LocalDate.now();
        int anioFinal = anio == 0 ? ahora.getYear()       : anio;
        int mesFinal  = mes  == 0 ? ahora.getMonthValue() : mes;

        ResumenMensual resumen = servicioFichajes.obtenerResumenMensual(
                usuarioActual, anioFinal, mesFinal);
        return ResponseEntity.ok(RespuestaApi.exito(resumen));
    }

    @PatchMapping("/observaciones")
    public ResponseEntity<RespuestaApi<RespuestaFichaje>> anadirObservaciones(
            @RequestBody Map<String, String> cuerpo,
            @AuthenticationPrincipal Usuario usuarioActual) {
        RespuestaFichaje fichaje = servicioFichajes.anadirObservaciones(
                usuarioActual, cuerpo.get("observaciones"));
        return ResponseEntity.ok(RespuestaApi.exito("Observaciones guardadas", fichaje));
    }

    @GetMapping("/hoy/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<List<RespuestaFichaje>>> obtenerFichajesDeHoyTodos() {
        return ResponseEntity.ok(
                RespuestaApi.exito(servicioFichajes.obtenerFichajesDeHoyTodos()));
    }

    @GetMapping("/rango")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<List<RespuestaFichaje>>> obtenerEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(
                RespuestaApi.exito(servicioFichajes.obtenerFichajesEnRango(inicio, fin)));
    }

    @GetMapping("/resumen-mensual/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaApi<ResumenMensual>> obtenerResumenDeUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int anio,
            @RequestParam(defaultValue = "0") int mes) {
        LocalDate ahora = LocalDate.now();
        int anioFinal = anio == 0 ? ahora.getYear()       : anio;
        int mesFinal  = mes  == 0 ? ahora.getMonthValue() : mes;

        return ResponseEntity.ok(RespuestaApi.exito(
                servicioFichajes.obtenerResumenMensualDeUsuario(usuarioId, anioFinal, mesFinal)));
    }
}