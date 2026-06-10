package com.tfg.trabajoFinGrado.modulos.modulos.modelo;

import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "modulos_activos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloActivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modulo_id", nullable = false, unique = true)
    private Modulo modulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activado_por", nullable = false)
    private Usuario activadoPor;

    @Column(name = "activado_en")
    private LocalDateTime activadoEn;

    @PrePersist
    protected void antesDeGuardar() {
        activadoEn = LocalDateTime.now();
    }
}