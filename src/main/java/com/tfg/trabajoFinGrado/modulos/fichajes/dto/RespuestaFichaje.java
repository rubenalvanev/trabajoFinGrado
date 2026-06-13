package com.tfg.trabajoFinGrado.modulos.fichajes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaFichaje {

    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private LocalDate fecha;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private Integer jornadaHoras;
    private String observaciones;

    private long minutosTrabajados;
    private long minutosExtra;
    private boolean fichadoActualmente;

    private String tiempoTrabajado;
    private String tiempoExtra;
}