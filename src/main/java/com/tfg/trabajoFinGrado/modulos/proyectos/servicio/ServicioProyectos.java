package com.tfg.trabajoFinGrado.modulos.proyectos.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.proyectos.dto.PeticionActualizarProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.dto.PeticionCrearProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.dto.RespuestaProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.Cliente;
import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.EstadoProyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.modelo.Proyecto;
import com.tfg.trabajoFinGrado.modulos.proyectos.repositorio.RepositorioCliente;
import com.tfg.trabajoFinGrado.modulos.proyectos.repositorio.RepositorioProyecto;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioProyectos {

    private final RepositorioProyecto repositorioProyecto;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioUsuario repositorioUsuario;

    public List<RespuestaProyecto> obtenerTodos() {
        return repositorioProyecto.encontrarTodosConRelaciones()
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public RespuestaProyecto obtenerPorId(Long id) {
        return mapearADto(buscarProyecto(id));
    }

    @Transactional
    public RespuestaProyecto crear(PeticionCrearProyecto peticion, Usuario autor) {
        Set<Usuario> empleados = resolverEmpleados(peticion.getIdsEmpleados());
        Set<Cliente> clientes = resolverClientes(peticion.getIdsClientes());

        Proyecto proyecto = Proyecto.builder()
                .nombre(peticion.getNombre())
                .descripcion(peticion.getDescripcion())
                .estado(EstadoProyecto.PLANIFICANDO)
                .fechaInicio(peticion.getFechaInicio())
                .empleados(empleados)
                .clientes(clientes)
                .creadoPor(autor)
                .build();

        return mapearADto(repositorioProyecto.save(proyecto));
    }

    @Transactional
    public RespuestaProyecto actualizar(Long id, PeticionActualizarProyecto peticion) {
        Proyecto proyecto = buscarProyecto(id);

        if (peticion.getNombre() != null && !peticion.getNombre().isBlank()) {
            proyecto.setNombre(peticion.getNombre());
        }
        if (peticion.getDescripcion() != null) {
            proyecto.setDescripcion(peticion.getDescripcion());
        }
        if (peticion.getEstado() != null) {
            proyecto.setEstado(peticion.getEstado());
        }
        if (peticion.getIdsEmpleados() != null) {
            proyecto.setEmpleados(resolverEmpleados(peticion.getIdsEmpleados()));
        }
        if (peticion.getIdsClientes() != null) {
            proyecto.setClientes(resolverClientes(peticion.getIdsClientes()));
        }

        return mapearADto(repositorioProyecto.save(proyecto));
    }

    @Transactional
    public RespuestaProyecto finalizar(Long id) {
        Proyecto proyecto = buscarProyecto(id);
        proyecto.setEstado(EstadoProyecto.ACABADO);
        return mapearADto(repositorioProyecto.save(proyecto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorioProyecto.existsById(id)) {
            throw new RecursoNoEncontradoExcepcion("Proyecto no encontrado con id: " + id);
        }
        repositorioProyecto.deleteById(id);
    }

    public List<Cliente> obtenerClientes() {
        return repositorioCliente.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Cliente crearCliente(Cliente cliente, Usuario autor) {
        cliente.setCreadoPor(autor);
        return repositorioCliente.save(cliente);
    }

    private Proyecto buscarProyecto(Long id) {
        return repositorioProyecto.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Proyecto no encontrado con id: " + id));
    }

    private Set<Usuario> resolverEmpleados(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(repositorioUsuario.findAllById(ids));
    }

    private Set<Cliente> resolverClientes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(repositorioCliente.findAllById(ids));
    }

    private RespuestaProyecto mapearADto(Proyecto proyecto) {
        return RespuestaProyecto.builder()
                .id(proyecto.getId())
                .nombre(proyecto.getNombre())
                .descripcion(proyecto.getDescripcion())
                .estado(proyecto.getEstado())
                .etiquetaEstado(proyecto.getEstado().getEtiqueta())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFin(proyecto.getFechaFin())
                .empleados(proyecto.getEmpleados().stream()
                        .map(Usuario::obtenerNombreCompleto).collect(Collectors.toList()))
                .clientes(proyecto.getClientes().stream()
                        .map(Cliente::getNombre).collect(Collectors.toList()))
                .creadoPor(proyecto.getCreadoPor() != null
                        ? proyecto.getCreadoPor().obtenerNombreCompleto() : "")
                .creadoEn(proyecto.getCreadoEn())
                .build();
    }
}