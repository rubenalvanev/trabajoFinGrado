package com.tfg.trabajoFinGrado.modulos.inventario.repositorio;

import com.tfg.trabajoFinGrado.modulos.inventario.modelo.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioStock extends JpaRepository<Stock, Long> {

    List<Stock> findByActivoTrueOrderByNombreAsc();

    List<Stock> findAllByOrderByNombreAsc();

    boolean existsByNombreAndActivoTrue(String nombre);
}