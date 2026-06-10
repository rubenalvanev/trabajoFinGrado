package com.tfg.trabajoFinGrado.modulos.modulos.repositorio;

import com.tfg.trabajoFinGrado.modulos.modulos.modelo.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioModulo extends JpaRepository<Modulo, Long> {

    Optional<Modulo> findByClave(String clave);

    List<Modulo> findAllByOrderByOrdenAsc();

    List<Modulo> findByObligatorioFalseOrderByOrdenAsc();
}