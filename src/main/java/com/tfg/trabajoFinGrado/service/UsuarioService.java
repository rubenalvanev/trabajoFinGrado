package com.tfg.trabajoFinGrado.service;

import com.tfg.trabajoFinGrado.entity.Usuario;
import com.tfg.trabajoFinGrado.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarContrasena(Long id, String nuevaContrasena) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void alternarActivo(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setActivo(!usuario.isActivo());
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void cambiarUltimoRegistro(String nombre) {
        usuarioRepository.findByNombre(nombre).ifPresent(usuario -> {
            usuario.setUltimoRegistro(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void borrarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    public long totalUsuarios() { return usuarioRepository.count(); }
    public long totalAdmins() { return usuarioRepository.countByRole(Usuario.Role.ADMIN); }
    public long totalActivos() { return usuarioRepository.countByActivo(true); }
}
