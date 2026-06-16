package com.tfg.trabajoFinGrado.modulos.grupos.repositorio;

import com.tfg.trabajoFinGrado.modulos.grupos.modelo.GrupoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioGrupoUsuario extends JpaRepository<GrupoUsuario, Long> {

    List<GrupoUsuario> findByGrupoId(Long grupoId);

    List<GrupoUsuario> findByUsuarioId(Long usuarioId);

    Optional<GrupoUsuario> findByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

    boolean existsByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

    void deleteByUsuarioIdAndGrupoId(Long usuarioId, Long grupoId);

    @Query("SELECT gu FROM GrupoUsuario gu JOIN FETCH gu.usuario JOIN FETCH gu.grupo WHERE gu.grupo.id = :grupoId")
    List<GrupoUsuario> encontrarMiembrosConDetalles(@Param("grupoId") Long grupoId);
}