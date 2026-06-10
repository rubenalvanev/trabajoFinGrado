package com.tfg.trabajoFinGrado.modulos.usuarios.repositorio;

import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioGrupo extends JpaRepository<Grupo, Long> {

    Optional<Grupo> findByNombre(String nombre);

    @Query("SELECT g FROM Grupo g LEFT JOIN FETCH g.modulos ORDER BY g.nombre ASC")
    List<Grupo> encontrarTodosConModulos();
}