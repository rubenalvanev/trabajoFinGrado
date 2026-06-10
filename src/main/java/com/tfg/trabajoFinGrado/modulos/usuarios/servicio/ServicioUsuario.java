package com.tfg.trabajoFinGrado.modulos.usuarios.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion;
import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.usuarios.dto.PeticionCrearUsuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.dto.PeticionEditarPerfil;
import com.tfg.trabajoFinGrado.modulos.usuarios.dto.RespuestaUsuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Grupo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.repositorio.RepositorioGrupo;
import com.tfg.trabajoFinGrado.modulos.usuarios.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioUsuario {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioGrupo repositorioGrupo;
    private final PasswordEncoder codificadorContrasena;

    public List<RespuestaUsuario> obtenerTodos() {
        return repositorioUsuario.findByActivoTrueOrderByNombreAsc()
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public RespuestaUsuario obtenerPorId(Long id) {
        return mapearADto(buscarUsuario(id));
    }

    @Transactional
    public RespuestaUsuario crear(PeticionCrearUsuario peticion) {
        if (repositorioUsuario.existsByEmail(peticion.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + peticion.getEmail());
        }

        Grupo grupo = null;
        if (peticion.getGrupoId() != null) {
            grupo = repositorioGrupo.findById(peticion.getGrupoId())
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Grupo no encontrado"));
        }

        Usuario usuario = Usuario.builder()
                .nombre(peticion.getNombre())
                .apellidos(peticion.getApellidos())
                .email(peticion.getEmail())
                .contrasena(codificadorContrasena.encode(peticion.getContrasena()))
                .rol(peticion.getRol())
                .grupo(grupo)
                .activo(true)
                .build();

        return mapearADto(repositorioUsuario.save(usuario));
    }

    @Transactional
    public RespuestaUsuario editarPerfil(Long idUsuario, PeticionEditarPerfil peticion,
                                         Usuario usuarioActual) {
        if (!usuarioActual.getId().equals(idUsuario) && !usuarioActual.esAdmin()) {
            throw new AccesoDenegadoExcepcion("Solo puedes editar tu propio perfil");
        }

        Usuario usuario = buscarUsuario(idUsuario);

        if (peticion.getNombre() != null && !peticion.getNombre().isBlank()) {
            usuario.setNombre(peticion.getNombre());
        }
        if (peticion.getApellidos() != null) {
            usuario.setApellidos(peticion.getApellidos());
        }
        if (peticion.getNuevaContrasena() != null && !peticion.getNuevaContrasena().isBlank()) {
            if (peticion.getContrasenaActual() == null ||
                    !codificadorContrasena.matches(
                            peticion.getContrasenaActual(), usuario.getContrasena())) {
                throw new IllegalArgumentException("La contraseña actual no es correcta");
            }
            usuario.setContrasena(
                    codificadorContrasena.encode(peticion.getNuevaContrasena()));
        }

        return mapearADto(repositorioUsuario.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscarUsuario(id);
        usuario.setActivo(false);
        repositorioUsuario.save(usuario);
    }

    public List<RespuestaUsuario> obtenerEmpleados() {
        return repositorioUsuario.findByActivoTrueOrderByNombreAsc()
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    private Usuario buscarUsuario(Long id) {
        return repositorioUsuario.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "Usuario no encontrado con id: " + id));
    }

    public RespuestaUsuario mapearADto(Usuario usuario) {
        return RespuestaUsuario.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .nombreCompleto(usuario.obtenerNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .grupo(usuario.getGrupo() != null ? usuario.getGrupo().getNombre() : null)
                .activo(usuario.isActivo())
                .creadoEn(usuario.getCreadoEn())
                .build();
    }
}