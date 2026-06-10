package com.tfg.trabajoFinGrado.modulos.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaUsuario {

    private Long id;
    private String nombre;
    private String apellidos;
    private String nombreCompleto;
    private String email;
    private String rol;
    private String grupo;
    private boolean activo;
    private LocalDateTime creadoEn;
}