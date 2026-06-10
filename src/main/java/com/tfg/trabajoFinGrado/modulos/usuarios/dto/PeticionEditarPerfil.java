package com.tfg.trabajoFinGrado.modulos.usuarios.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PeticionEditarPerfil {

    @Size(max = 100)
    private String nombre;

    @Size(max = 150)
    private String apellidos;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaContrasena;

    private String contrasenaActual;
}