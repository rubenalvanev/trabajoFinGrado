package com.tfg.trabajoFinGrado.modulos.fichajes.modelo;

import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "fichajes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "fecha"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fichaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalDateTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Column(name = "jornada_horas", nullable = false)
    @Builder.Default
    private Integer jornadaHoras = 8;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void antesDeGuardar() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
        if (fecha == null) fecha = LocalDate.now();
    }

    @PreUpdate
    protected void antesDeActualizar() {
        actualizadoEn = LocalDateTime.now();
    }

    public long calcularMinutosTrabajados() {
        if (horaEntrada == null || horaSalida == null) return 0;
        return ChronoUnit.MINUTES.between(horaEntrada, horaSalida);
    }

    public long calcularMinutosExtra() {
        long minutosJornada = jornadaHoras * 60L;
        long minutosTrabajados = calcularMinutosTrabajados();
        return Math.max(0, minutosTrabajados - minutosJornada);
    }

    public boolean estaFichadoActualmente() {
        return horaEntrada != null && horaSalida == null;
    }
}