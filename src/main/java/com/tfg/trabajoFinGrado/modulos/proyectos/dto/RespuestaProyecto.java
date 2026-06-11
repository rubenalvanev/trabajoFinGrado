package com.tfg.trabajoFinGrado.modulos.proyectos.dto;

import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.EstadoProyecto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaProyecto {

    private Long id;
    private String nombre;
    private String descripcion;
    private EstadoProyecto estado;
    private String etiquetaEstado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<String> empleados;
    private List<String> clientes;
    private String creadoPor;
    private LocalDateTime creadoEn;
}