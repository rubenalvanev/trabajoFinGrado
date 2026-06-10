package com.tfg.trabajoFinGrado.comun.excepcion;

public class AccesoDenegadoExcepcion extends RuntimeException {

    public AccesoDenegadoExcepcion(String mensaje) {
        super(mensaje);
    }
}