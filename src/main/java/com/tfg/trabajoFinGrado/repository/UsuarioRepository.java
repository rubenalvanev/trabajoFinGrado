package com.tfg.trabajoFinGrado.repository;

import com.tfg.trabajoFinGrado.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombre(String nombre);
    Optional<Usuario> findByEmail(String email);
    boolean existsByNombre(String nombre);
    boolean existsByEmail(String email);
    long countByRole(Usuario.Role role);
    long countByActivo(boolean activo);
}
