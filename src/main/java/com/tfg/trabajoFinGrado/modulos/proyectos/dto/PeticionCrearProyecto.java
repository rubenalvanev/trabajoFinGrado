package com.tfg.trabajoFinGrado.modulos.proyectos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PeticionCrearProyecto {

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    private String nombre;

    private String descripcion;

    private List<Long> idsEmpleados;

    private List<Long> idsClientes;

    private LocalDate fechaInicio;
}