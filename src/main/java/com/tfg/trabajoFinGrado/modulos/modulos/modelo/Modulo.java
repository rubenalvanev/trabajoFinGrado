package com.tfg.trabajoFinGrado.modulos.modulos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "modulos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String clave;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 50)
    private String icono;

    @Column(nullable = false)
    private boolean obligatorio;

    @Column(nullable = false)
    private int orden;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @PrePersist
    protected void antesDeGuardar() {
        creadoEn = LocalDateTime.now();
    }
}
