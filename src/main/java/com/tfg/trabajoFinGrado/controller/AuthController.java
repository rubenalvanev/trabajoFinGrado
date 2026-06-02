package com.tfg.trabajoFinGrado.controller;

import com.tfg.trabajoFinGrado.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            Model model) {
        if (error != null) model.addAttribute("error", "Usuario o contraseña incorrectos");
        if (logout != null) model.addAttribute("logout", "Has cerrado sesión correctamente");
        if (expired != null) model.addAttribute("error", "Tu sesión ha expirado");
        return "auth/login";
    }

}
