package com.tfg.trabajoFinGrado.modulos.documentos.modelo;

import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "documentos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaDocumento categoria;

    @Column(nullable = false)
    private boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VersionDocumento> versiones = new ArrayList<>();

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void antesDeGuardar() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
        activo = true;
    }

    @PreUpdate
    protected void antesDeActualizar() {
        actualizadoEn = LocalDateTime.now();
    }

  
    public VersionDocumento obtenerUltimaVersion() {
        if (versiones == null || versiones.isEmpty()) return null;
        return versiones.stream()
                .max((a, b) -> Integer.compare(a.getNumeroVersion(), b.getNumeroVersion()))
                .orElse(null);
    }
}