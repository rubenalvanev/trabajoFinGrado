package com.tfg.trabajoFinGrado.modulos.modulos.repositorio;

import com.tfg.trabajoFinGrado.modulos.modulos.modelo.ModuloActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioModuloActivo extends JpaRepository<ModuloActivo, Long> {

    Optional<ModuloActivo> findByModuloId(Long moduloId);

    Optional<ModuloActivo> findByModuloClave(String clave);

    boolean existsByModuloClave(String clave);

    @Query("SELECT ma FROM ModuloActivo ma JOIN FETCH ma.modulo m ORDER BY m.orden ASC")
    List<ModuloActivo> encontrarTodosConModulo();

    void deleteByModuloId(Long moduloId);
}
