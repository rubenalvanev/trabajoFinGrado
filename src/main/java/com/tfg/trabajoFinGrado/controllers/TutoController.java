package com.tfg.trabajoFinGrado.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TutoController {

    @GetMapping("/login")
    public String mostrarVentana1(Model model) {
        model.addAttribute("mensaje", "Login");
        return "login";
    }

    @GetMapping("/ventana2")
    public String mostrarVentana2(Model model) {
        model.addAttribute("mensaje", "Bienvenido a la Ventana 2");
        return "ventana2"; // Carga ventana2.html
    }

}
