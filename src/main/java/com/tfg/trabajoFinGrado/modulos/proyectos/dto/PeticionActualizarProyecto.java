package com.tfg.trabajoFinGrado.modulos.proyectos.dto;

import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.EstadoProyecto;
import lombok.Data;

import java.util.List;

@Data
public class PeticionActualizarProyecto {

    private String nombre;
    private String descripcion;
    private EstadoProyecto estado;
    private List<Long> idsEmpleados;
    private List<Long> idsClientes;
}