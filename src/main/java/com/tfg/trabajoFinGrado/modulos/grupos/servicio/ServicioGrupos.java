package com.tfg.trabajoFinGrado.modulos.grupos.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.grupos.dto.PeticionCrearGrupo;
import com.tfg.trabajoFinGrado.modulos.grupos.dto.RespuestaGrupoDetalle;
import com.tfg.trabajoFinGrado.modulos.grupos.modelo.GrupoUsuario;
import com.tfg.trabajoFinGrado.modulos.grupos.repositorio.RepositorioGrupoUsuario;
import com.tfg.trabajoFinGrado.modulos.modulos.modelo.Modulo;
import com.tfg.trabajoFinGrado.modulos.modulos.repositorio.RepositorioModulo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Grupo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.repositorio.RepositorioGrupo;
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
public class ServicioGrupos {

    private final RepositorioGrupo repositorioGrupo;
    private final RepositorioGrupoUsuario repositorioGrupoUsuario;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioModulo repositorioModulo;

    public List<RespuestaGrupoDetalle> obtenerTodosConDetalles() {
        return repositorioGrupo.encontrarTodosConModulos()
                .stream()
                .map(this::construirDetalle)
                .collect(Collectors.toList());
    }

    public RespuestaGrupoDetalle obtenerDetalle(Long grupoId) {
        Grupo grupo = buscarGrupo(grupoId);
        return construirDetalle(grupo);
    }

    @Transactional
    public RespuestaGrupoDetalle crearGrupo(PeticionCrearGrupo peticion) {
        if (repositorioGrupo.findByNombre(peticion.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un grupo con el nombre: " + peticion.getNombre());
        }

        Set<Modulo> modulos = resolverModulos(peticion.getIdsModulos());

        Grupo grupo = Grupo.builder()
                .nombre(peticion.getNombre())
                .descripcion(peticion.getDescripcion())
                .modulos(modulos)
                .build();
        grupo = repositorioGrupo.save(grupo);

        if (peticion.getIdsUsuarios() != null && !peticion.getIdsUsuarios().isEmpty()) {
            final Long grupoId = grupo.getId();
            for (Long usuarioId : peticion.getIdsUsuarios()) {
                Usuario usuario = repositorioUsuario.findById(usuarioId)
                        .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado: " + usuarioId));
                GrupoUsuario asignacion = GrupoUsuario.builder()
                        .usuario(usuario)
                        .grupo(repositorioGrupo.findById(grupoId).orElseThrow())
                        .build();
                repositorioGrupoUsuario.save(asignacion);
            }
        }

        return construirDetalle(repositorioGrupo.findById(grupo.getId()).orElseThrow());
    }

    @Transactional
    public RespuestaGrupoDetalle actualizarModulosDeGrupo(Long grupoId, List<Long> idsModulos) {
        Grupo grupo = buscarGrupo(grupoId);
        grupo.setModulos(resolverModulos(idsModulos));
        return construirDetalle(repositorioGrupo.save(grupo));
    }

    @Transactional
    public void eliminarGrupo(Long grupoId) {
        if (!repositorioGrupo.existsById(grupoId)) {
            throw new RecursoNoEncontradoExcepcion("Grupo no encontrado");
        }
        repositorioGrupoUsuario.findByGrupoId(grupoId)
                .forEach(gu -> repositorioGrupoUsuario.deleteById(gu.getId()));
        repositorioGrupo.deleteById(grupoId);
    }

    @Transactional
    public void asignarUsuario(Long grupoId, Long usuarioId) {
        if (repositorioGrupoUsuario.existsByUsuarioIdAndGrupoId(usuarioId, grupoId)) {
            throw new IllegalArgumentException("El usuario ya pertenece a este grupo");
        }
        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado"));
        Grupo grupo = buscarGrupo(grupoId);

        repositorioGrupoUsuario.save(GrupoUsuario.builder()
                .usuario(usuario)
                .grupo(grupo)
                .build());
    }

    @Transactional
    public void quitarUsuario(Long grupoId, Long usuarioId) {
        if (!repositorioGrupoUsuario.existsByUsuarioIdAndGrupoId(usuarioId, grupoId)) {
            throw new RecursoNoEncontradoExcepcion("El usuario no pertenece a este grupo");
        }
        repositorioGrupoUsuario.deleteByUsuarioIdAndGrupoId(usuarioId, grupoId);
    }

    private Grupo buscarGrupo(Long id) {
        return repositorioGrupo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Grupo no encontrado con id: " + id));
    }

    private Set<Modulo> resolverModulos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(repositorioModulo.findAllById(ids));
    }

    private RespuestaGrupoDetalle construirDetalle(Grupo grupo) {
        List<RespuestaGrupoDetalle.MiembroGrupo> miembros =
                repositorioGrupoUsuario.encontrarMiembrosConDetalles(grupo.getId())
                        .stream()
                        .map(gu -> RespuestaGrupoDetalle.MiembroGrupo.builder()
                                .idUsuario(gu.getUsuario().getId())
                                .nombreCompleto(gu.getUsuario().obtenerNombreCompleto())
                                .email(gu.getUsuario().getEmail())
                                .build())
                        .collect(Collectors.toList());

        List<RespuestaGrupoDetalle.ModuloGrupo> modulos = grupo.getModulos().stream()
                .map(m -> RespuestaGrupoDetalle.ModuloGrupo.builder()
                        .id(m.getId())
                        .clave(m.getClave())
                        .nombre(m.getNombre())
                        .build())
                .collect(Collectors.toList());

        return RespuestaGrupoDetalle.builder()
                .id(grupo.getId())
                .nombre(grupo.getNombre())
                .descripcion(grupo.getDescripcion())
                .miembros(miembros)
                .modulos(modulos)
                .build();
    }
}