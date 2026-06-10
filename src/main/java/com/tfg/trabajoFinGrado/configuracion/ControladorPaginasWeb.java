package com.tfg.trabajoFinGrado.configuracion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador web que sirve la Single Page Application (SPA).
 * Todas las rutas del frontend se redirigen al index.html.
 */
@Controller
public class ControladorPaginasWeb {

    @GetMapping({"/", "/login", "/app", "/app/**"})
    public String paginaPrincipal() {
        return "index";
    }
}
