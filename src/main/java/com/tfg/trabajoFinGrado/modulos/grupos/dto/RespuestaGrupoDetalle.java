package com.tfg.trabajoFinGrado.modulos.grupos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaGrupoDetalle {

    private Long id;
    private String nombre;
    private String descripcion;
    private List<MiembroGrupo> miembros;
    private List<ModuloGrupo> modulos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MiembroGrupo {
        private Long idUsuario;
        private String nombreCompleto;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuloGrupo {
        private Long id;
        private String clave;
        private String nombre;
    }
}