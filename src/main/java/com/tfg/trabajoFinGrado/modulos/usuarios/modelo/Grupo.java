package com.tfg.trabajoFinGrado.modulos.usuarios.modelo;

import com.tfg.trabajoFinGrado.modulos.modulos.modelo.Modulo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grupos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "grupos_modulos",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "modulo_id")
    )
    @Builder.Default
    private Set<Modulo> modulos = new HashSet<>();

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @PrePersist
    protected void antesDeGuardar() {
        creadoEn = LocalDateTime.now();
    }
}