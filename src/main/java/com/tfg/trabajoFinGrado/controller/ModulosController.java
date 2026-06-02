package com.tfg.trabajoFinGrado.controller;

import com.tfg.trabajoFinGrado.entity.Usuario;
import com.tfg.trabajoFinGrado.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.YearMonth;

@Controller
@RequiredArgsConstructor
public class ModulosController {

    private final UsuarioService usuarioService;

    @GetMapping({"/", "/modulos"})
    public String dashboard(Model model, Authentication auth) {

        usuarioService.cambiarUltimoRegistro(auth.getName());
        return "modulos/modulos";
    }
}
