package com.tfg.trabajoFinGrado.modulos.documentos.modelo;

import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "versiones_documento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_almacenamiento", nullable = false, length = 500)
    private String rutaAlmacenamiento;

    @Column(name = "tipo_mime", length = 100)
    private String tipoMime;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(length = 500)
    private String comentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por", nullable = false)
    private Usuario subidoPor;

    @Column(name = "subido_en")
    private LocalDateTime subidoEn;

    @PrePersist
    protected void antesDeGuardar() {
        subidoEn = LocalDateTime.now();
    }
}