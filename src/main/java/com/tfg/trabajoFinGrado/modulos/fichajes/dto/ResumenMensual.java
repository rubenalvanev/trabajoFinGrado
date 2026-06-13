package com.tfg.trabajoFinGrado.modulos.fichajes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenMensual {

    private int anio;
    private int mes;
    private String nombreUsuario;

    private int diasTrabajados;
    private long totalMinutosTrabajados;
    private long totalMinutosExtra;

    private String totalTiempoTrabajado;
    private String totalTiempoExtra;

    private List<RespuestaFichaje> fichajes;
}