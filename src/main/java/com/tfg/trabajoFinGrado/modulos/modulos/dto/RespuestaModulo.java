package com.tfg.trabajoFinGrado.modulos.modulos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaModulo {

    private Long id;
    private String clave;
    private String nombre;
    private String descripcion;
    private String icono;
    private boolean obligatorio;
    private boolean activo;
    private int orden;
}
