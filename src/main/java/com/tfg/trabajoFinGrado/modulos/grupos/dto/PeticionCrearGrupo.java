package com.tfg.trabajoFinGrado.modulos.grupos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PeticionCrearGrupo {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String nombre;

    private String descripcion;

    private List<Long> idsUsuarios;

    private List<Long> idsModulos;
}