package com.tfg.trabajoFinGrado.modulos.proyectos.modelo;

public enum EstadoProyecto {
    PLANIFICANDO("Planificando"),
    EN_PROCESO("En proceso"),
    FINALIZANDO("Finalizando"),
    ACABADO("Acabado");

    private final String etiqueta;

    EstadoProyecto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}