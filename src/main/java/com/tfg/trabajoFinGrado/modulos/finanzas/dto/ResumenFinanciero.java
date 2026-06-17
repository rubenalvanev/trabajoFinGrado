package com.tfg.trabajoFinGrado.modulos.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenFinanciero {

    private BigDecimal totalBeneficios;
    private BigDecimal totalGastos;
    private BigDecimal total;

    private BigDecimal beneficiosMesActual;
    private BigDecimal gastosMesActual;
    private BigDecimal totalMesActual;

    private BigDecimal beneficiosMesPasado;
    private BigDecimal gastosMesPasado;
    private BigDecimal totalMesPasado;
}
