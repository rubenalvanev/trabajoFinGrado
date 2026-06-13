package com.tfg.trabajoFinGrado.modulos.fichajes.repositorio;

import com.tfg.trabajoFinGrado.modulos.fichajes.modelo.Fichaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioFichaje extends JpaRepository<Fichaje, Long> {

    Optional<Fichaje> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    List<Fichaje> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(
            Long usuarioId, LocalDate inicio, LocalDate fin);

    List<Fichaje> findByFechaOrderByUsuario_NombreAsc(LocalDate fecha);

    @Query("SELECT f FROM Fichaje f JOIN FETCH f.usuario u " +
           "WHERE f.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY f.fecha DESC, u.nombre ASC")
    List<Fichaje> encontrarTodosEnRangoConUsuario(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT f FROM Fichaje f JOIN FETCH f.usuario u " +
           "WHERE f.usuario.id = :usuarioId AND f.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY f.fecha DESC")
    List<Fichaje> encontrarDeUsuarioEnRango(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);
}