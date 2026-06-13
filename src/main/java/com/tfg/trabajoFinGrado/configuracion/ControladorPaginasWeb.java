package com.tfg.trabajoFinGrado.configuracion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorPaginasWeb {

    @GetMapping({"/", "/login", "/app", "/app/**"})
    public String paginaPrincipal() {
        return "index";
    }
}
