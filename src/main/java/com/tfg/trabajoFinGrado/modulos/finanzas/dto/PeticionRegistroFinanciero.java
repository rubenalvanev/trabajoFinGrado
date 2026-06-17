package com.tfg.trabajoFinGrado.modulos.finanzas.dto;

import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.TipoRegistro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class PeticionRegistroFinanciero {

    @NotNull(message = "El tipo es obligatorio")
    private TipoRegistro tipo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero")
    private BigDecimal importe;

    private String categoria;

    private LocalDate fecha;
}
