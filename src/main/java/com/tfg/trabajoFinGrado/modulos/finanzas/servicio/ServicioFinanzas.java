package com.tfg.trabajoFinGrado.modulos.finanzas.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.finanzas.dto.PeticionRegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.dto.ResumenFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.dto.RespuestaRegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.RegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.TipoRegistro;
import com.tfg.trabajoFinGrado.modulos.finanzas.repositorio.RepositorioRegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ServicioFinanzas {

    private final RepositorioRegistroFinanciero repositorioFinanciero;

    public List<RespuestaRegistroFinanciero> obtenerTodos() {
        return repositorioFinanciero.findAllByOrderByFechaDesc()
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public List<RespuestaRegistroFinanciero> obtenerPorTipo(TipoRegistro tipo) {
        return repositorioFinanciero.findByTipoOrderByFechaDesc(tipo)
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaRegistroFinanciero crear(PeticionRegistroFinanciero peticion, Usuario autor) {
        RegistroFinanciero registro = RegistroFinanciero.builder()
                .tipo(peticion.getTipo())
                .descripcion(peticion.getDescripcion())
                .importe(peticion.getImporte())
                .categoria(peticion.getCategoria())
                .fecha(peticion.getFecha() != null ? peticion.getFecha() : LocalDate.now())
                .creadoPor(autor)
                .build();

        return mapearADto(repositorioFinanciero.save(registro));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorioFinanciero.existsById(id)) {
            throw new RecursoNoEncontradoExcepcion("Registro financiero no encontrado con id: " + id);
        }
        repositorioFinanciero.deleteById(id);
    }

    public ResumenFinanciero obtenerResumen() {
        LocalDate ahora = LocalDate.now();
        LocalDate inicioMesActual = ahora.withDayOfMonth(1);
        LocalDate finMesActual = ahora.withDayOfMonth(ahora.lengthOfMonth());

        LocalDate mesPasado = ahora.minusMonths(1);
        LocalDate inicioMesPasado = mesPasado.withDayOfMonth(1);
        LocalDate finMesPasado = mesPasado.withDayOfMonth(mesPasado.lengthOfMonth());

        BigDecimal totalBeneficios = repositorioFinanciero.sumarImportePorTipo(TipoRegistro.BENEFICIO);
        BigDecimal totalGastos = repositorioFinanciero.sumarImportePorTipo(TipoRegistro.GASTO);

        BigDecimal benMesActual = repositorioFinanciero.sumarImportePorTipoYFecha(
                TipoRegistro.BENEFICIO, inicioMesActual, finMesActual);
        BigDecimal gastosMesActual = repositorioFinanciero.sumarImportePorTipoYFecha(
                TipoRegistro.GASTO, inicioMesActual, finMesActual);

        BigDecimal benMesPasado = repositorioFinanciero.sumarImportePorTipoYFecha(
                TipoRegistro.BENEFICIO, inicioMesPasado, finMesPasado);
        BigDecimal gastosMesPasado = repositorioFinanciero.sumarImportePorTipoYFecha(
                TipoRegistro.GASTO, inicioMesPasado, finMesPasado);

        return ResumenFinanciero.builder()
                .totalBeneficios(totalBeneficios)
                .totalGastos(totalGastos)
                .total(totalBeneficios.subtract(totalGastos))
                .beneficiosMesActual(benMesActual)
                .gastosMesActual(gastosMesActual)
                .totalMesActual(benMesActual.subtract(gastosMesActual))
                .beneficiosMesPasado(benMesPasado)
                .gastosMesPasado(gastosMesPasado)
                .totalMesPasado(benMesPasado.subtract(gastosMesPasado))
                .build();
    }

    private RespuestaRegistroFinanciero mapearADto(RegistroFinanciero registro) {
        return RespuestaRegistroFinanciero.builder()
                .id(registro.getId())
                .tipo(registro.getTipo())
                .descripcion(registro.getDescripcion())
                .importe(registro.getImporte())
                .categoria(registro.getCategoria())
                .fecha(registro.getFecha())
                .creadoPor(registro.getCreadoPor() != null
                        ? registro.getCreadoPor().obtenerNombreCompleto() : "")
                .build();
    }
}
