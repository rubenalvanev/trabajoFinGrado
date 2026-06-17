package com.tfg.trabajoFinGrado.modulos.finanzas.dto;

import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.TipoRegistro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaRegistroFinanciero {

    private Long id;
    private TipoRegistro tipo;
    private String descripcion;
    private BigDecimal importe;
    private String categoria;
    private LocalDate fecha;
    private String creadoPor;
}
