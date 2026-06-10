package com.tfg.trabajoFinGrado.modulos.modulos.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.modulos.modelo.Modulo;
import com.tfg.trabajoFinGrado.modulos.modulos.modelo.ModuloActivo;
import com.tfg.trabajoFinGrado.modulos.modulos.repositorio.RepositorioModulo;
import com.tfg.trabajoFinGrado.modulos.modulos.repositorio.RepositorioModuloActivo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioModulo {

    private final RepositorioModulo repositorioModulo;
    private final RepositorioModuloActivo repositorioModuloActivo;

    public List<Modulo> obtenerTodos() {
        return repositorioModulo.findAllByOrderByOrdenAsc();
    }

    public List<Modulo> obtenerOpcionales() {
        return repositorioModulo.findByObligatorioFalseOrderByOrdenAsc();
    }

    public Set<String> obtenerClavesActivas() {
        return repositorioModuloActivo.encontrarTodosConModulo()
                .stream()
                .map(ma -> ma.getModulo().getClave())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void activar(Long moduloId, Usuario admin) {
        Modulo modulo = repositorioModulo.findById(moduloId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Módulo no encontrado"));

        if (repositorioModuloActivo.existsByModuloClave(modulo.getClave())) {
            throw new IllegalArgumentException("El módulo ya está activo");
        }

        ModuloActivo moduloActivo = ModuloActivo.builder()
                .modulo(modulo)
                .activadoPor(admin)
                .build();

        repositorioModuloActivo.save(moduloActivo);
    }

    @Transactional
    public void desactivar(Long moduloId) {
        Modulo modulo = repositorioModulo.findById(moduloId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Módulo no encontrado"));

        if (modulo.isObligatorio()) {
            throw new IllegalArgumentException("No se puede desactivar un módulo obligatorio");
        }

        repositorioModuloActivo.deleteByModuloId(moduloId);
    }

    public boolean estaActivo(String clave) {
        return repositorioModuloActivo.existsByModuloClave(clave);
    }
}
