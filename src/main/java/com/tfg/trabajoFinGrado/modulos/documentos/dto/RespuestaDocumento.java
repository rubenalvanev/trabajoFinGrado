package com.tfg.trabajoFinGrado.modulos.documentos.dto;

import com.tfg.trabajoFinGrado.modulos.documentos.modelo.CategoriaDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDocumento {

    private Long id;
    private String nombre;
    private String descripcion;
    private CategoriaDocumento categoria;
    private String creadoPor;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private int totalVersiones;
    private RespuestaVersion ultimaVersion;
    private List<RespuestaVersion> versiones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RespuestaVersion {
        private Long id;
        private int numeroVersion;
        private String nombreArchivo;
        private String tipoMime;
        private Long tamanioBytes;
        private String comentario;
        private String subidoPor;
        private LocalDateTime subidoEn;
    }
}