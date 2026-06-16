package com.tfg.trabajoFinGrado.modulos.grupos.modelo;

import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Grupo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grupos_usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Column(name = "asignado_en")
    private LocalDateTime asignadoEn;

    @PrePersist
    protected void antesDeGuardar() {
        asignadoEn = LocalDateTime.now();
    }
}