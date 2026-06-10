package com.tfg.trabajoFinGrado.modulos.autenticacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaLogin {

    private String token;
    private Long idUsuario;
    private String nombre;
    private String email;
    private String rol;
    private String grupo;
}