package com.tfg.trabajoFinGrado.comun.excepcion;

import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    public ResponseEntity<RespuestaApi<Void>> manejarRecursoNoEncontrado(RecursoNoEncontradoExcepcion ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RespuestaApi.error(ex.getMessage()));
    }

    @ExceptionHandler(AccesoDenegadoExcepcion.class)
    public ResponseEntity<RespuestaApi<Void>> manejarAccesoDenegado(AccesoDenegadoExcepcion ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RespuestaApi.error(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarAccesoDenegadoSecurity(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RespuestaApi.error("Acceso denegado"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RespuestaApi.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaApi<Map<String, String>>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RespuestaApi.<Map<String, String>>builder()
                        .exito(false)
                        .error("Error de validación")
                        .datos(errores)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi<Void>> manejarExcepcionGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespuestaApi.error("Error interno del servidor: " + ex.getMessage()));
    }
}