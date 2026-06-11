package com.tfg.trabajoFinGrado.modulos.proyectos.repositorio;

import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByOrderByNombreAsc();
}