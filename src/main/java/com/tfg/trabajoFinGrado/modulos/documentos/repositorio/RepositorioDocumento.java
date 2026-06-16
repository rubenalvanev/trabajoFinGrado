package com.tfg.trabajoFinGrado.modulos.documentos.repositorio;

import com.tfg.trabajoFinGrado.modulos.documentos.modelo.CategoriaDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.modelo.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioDocumento extends JpaRepository<Documento, Long> {

    List<Documento> findByActivoTrueOrderByActualizadoEnDesc();

    List<Documento> findByActivoTrueAndCategoriaOrderByActualizadoEnDesc(CategoriaDocumento categoria);
}