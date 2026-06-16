package com.tfg.trabajoFinGrado.modulos.documentos.repositorio;

import com.tfg.trabajoFinGrado.modulos.documentos.modelo.VersionDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioVersionDocumento extends JpaRepository<VersionDocumento, Long> {

    List<VersionDocumento> findByDocumentoIdOrderByNumeroVersionDesc(Long documentoId);

    Integer countByDocumentoId(Long documentoId);
}