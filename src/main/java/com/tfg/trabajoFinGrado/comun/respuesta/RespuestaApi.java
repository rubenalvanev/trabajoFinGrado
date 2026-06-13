package com.tfg.trabajoFinGrado.comun.respuesta;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespuestaApi<T> {

    private boolean exito;
    private String mensaje;
    private T datos;
    private String error;

    public static <T> RespuestaApi<T> exito(T datos) {
        return RespuestaApi.<T>builder().exito(true).datos(datos).build();
    }

    public static <T> RespuestaApi<T> exito(String mensaje, T datos) {
        return RespuestaApi.<T>builder().exito(true).mensaje(mensaje).datos(datos).build();
    }

    public static <T> RespuestaApi<T> error(String error) {
        return RespuestaApi.<T>builder().exito(false).error(error).build();
    }
}
