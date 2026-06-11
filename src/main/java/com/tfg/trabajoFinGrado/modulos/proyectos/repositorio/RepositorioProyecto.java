package com.tfg.trabajoFinGrado.modulos.proyectos.repositorio;

import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.EstadoProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioProyecto extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findAllByOrderByCreadoEnDesc();

    List<Proyecto> findByEstadoOrderByCreadoEnDesc(EstadoProyecto estado);

    @Query("SELECT p FROM Proyecto p LEFT JOIN FETCH p.empleados LEFT JOIN FETCH p.clientes ORDER BY p.creadoEn DESC")
    List<Proyecto> encontrarTodosConRelaciones();
}