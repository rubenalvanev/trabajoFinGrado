package com.tfg.trabajoFinGrado.config;

import com.tfg.trabajoFinGrado.entity.Usuario;
import com.tfg.trabajoFinGrado.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByNombre("admin")) {
            Usuario admin = Usuario.builder()
                    .nombre("admin")
                    .contrasena(passwordEncoder.encode("admin123"))
                    .email("admin@tfg.com")
                    .nombreCompleto("Administrador del Sistema")
                    .role(Usuario.Role.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuario admin creado. Usuario: admin | Contraseña: admin123");
        }
    }
}
