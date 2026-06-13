package com.tfg.trabajoFinGrado.modulos.fichajes.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion;
import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.fichajes.dto.ResumenMensual;
import com.tfg.trabajoFinGrado.modulos.fichajes.dto.RespuestaFichaje;
import com.tfg.trabajoFinGrado.modulos.fichajes.modelo.Fichaje;
import com.tfg.trabajoFinGrado.modulos.fichajes.repositorio.RepositorioFichaje;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import com.tfg.trabajoFinGrado.modulos.usuarios.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioFichajes {

    private final RepositorioFichaje repositorioFichaje;
    private final RepositorioUsuario repositorioUsuario;

    @Transactional
    public RespuestaFichaje registrarEntrada(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Optional<Fichaje> fichajeExistente =
                repositorioFichaje.findByUsuarioIdAndFecha(usuario.getId(), hoy);

        if (fichajeExistente.isPresent()) {
            Fichaje fichaje = fichajeExistente.get();
            if (fichaje.getHoraEntrada() != null) {
                throw new IllegalArgumentException("Ya has registrado tu entrada hoy");
            }
            fichaje.setHoraEntrada(LocalDateTime.now());
            return mapearADto(repositorioFichaje.save(fichaje));
        }

        Fichaje nuevo = Fichaje.builder()
                .usuario(usuario)
                .fecha(hoy)
                .horaEntrada(LocalDateTime.now())
                .build();

        return mapearADto(repositorioFichaje.save(nuevo));
    }

    @Transactional
    public RespuestaFichaje registrarSalida(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Fichaje fichaje = repositorioFichaje.findByUsuarioIdAndFecha(usuario.getId(), hoy)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No has registrado tu entrada hoy"));

        if (fichaje.getHoraEntrada() == null) {
            throw new IllegalArgumentException("Debes registrar la entrada antes de la salida");
        }
        if (fichaje.getHoraSalida() != null) {
            throw new IllegalArgumentException("Ya has registrado tu salida hoy");
        }
        if (LocalDateTime.now().isBefore(fichaje.getHoraEntrada())) {
            throw new IllegalArgumentException("La hora de salida no puede ser anterior a la entrada");
        }

        fichaje.setHoraSalida(LocalDateTime.now());
        return mapearADto(repositorioFichaje.save(fichaje));
    }

    @Transactional
    public RespuestaFichaje anadirObservaciones(Usuario usuario, String observaciones) {
        LocalDate hoy = LocalDate.now();
        Fichaje fichaje = repositorioFichaje.findByUsuarioIdAndFecha(usuario.getId(), hoy)
                .orElseThrow(() -> new IllegalArgumentException("No hay fichaje hoy para añadir observaciones"));
        fichaje.setObservaciones(observaciones);
        return mapearADto(repositorioFichaje.save(fichaje));
    }

    public RespuestaFichaje obtenerFichajeHoy(Usuario usuario) {
        return repositorioFichaje
                .findByUsuarioIdAndFecha(usuario.getId(), LocalDate.now())
                .map(this::mapearADto)
                .orElse(null);
    }

    public ResumenMensual obtenerResumenMensual(Usuario usuario, int anio, int mes) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Fichaje> fichajes = repositorioFichaje
                .encontrarDeUsuarioEnRango(usuario.getId(), inicio, fin);

        return construirResumen(usuario.obtenerNombreCompleto(), anio, mes, fichajes);
    }

    public List<RespuestaFichaje> obtenerFichajesDeHoyTodos() {
        return repositorioFichaje.findByFechaOrderByUsuario_NombreAsc(LocalDate.now())
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public List<RespuestaFichaje> obtenerFichajesEnRango(LocalDate inicio, LocalDate fin) {
        return repositorioFichaje.encontrarTodosEnRangoConUsuario(inicio, fin)
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public ResumenMensual obtenerResumenMensualDeUsuario(Long usuarioId, int anio, int mes) {
        Usuario usuario = repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado"));

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Fichaje> fichajes = repositorioFichaje
                .encontrarDeUsuarioEnRango(usuarioId, inicio, fin);

        return construirResumen(usuario.obtenerNombreCompleto(), anio, mes, fichajes);
    }

    private ResumenMensual construirResumen(String nombreUsuario, int anio, int mes,
                                            List<Fichaje> fichajes) {
        long totalMinutos = fichajes.stream()
                .mapToLong(Fichaje::calcularMinutosTrabajados)
                .sum();
        long totalExtra = fichajes.stream()
                .mapToLong(Fichaje::calcularMinutosExtra)
                .sum();

        return ResumenMensual.builder()
                .anio(anio)
                .mes(mes)
                .nombreUsuario(nombreUsuario)
                .diasTrabajados((int) fichajes.stream()
                        .filter(f -> f.calcularMinutosTrabajados() > 0).count())
                .totalMinutosTrabajados(totalMinutos)
                .totalMinutosExtra(totalExtra)
                .totalTiempoTrabajado(formatearMinutos(totalMinutos))
                .totalTiempoExtra(formatearMinutos(totalExtra))
                .fichajes(fichajes.stream().map(this::mapearADto).collect(Collectors.toList()))
                .build();
    }

    private RespuestaFichaje mapearADto(Fichaje fichaje) {
        long minutosTrabajados = fichaje.calcularMinutosTrabajados();
        long minutosExtra      = fichaje.calcularMinutosExtra();

        return RespuestaFichaje.builder()
                .id(fichaje.getId())
                .usuarioId(fichaje.getUsuario().getId())
                .nombreUsuario(fichaje.getUsuario().obtenerNombreCompleto())
                .fecha(fichaje.getFecha())
                .horaEntrada(fichaje.getHoraEntrada())
                .horaSalida(fichaje.getHoraSalida())
                .jornadaHoras(fichaje.getJornadaHoras())
                .observaciones(fichaje.getObservaciones())
                .minutosTrabajados(minutosTrabajados)
                .minutosExtra(minutosExtra)
                .fichadoActualmente(fichaje.estaFichadoActualmente())
                .tiempoTrabajado(formatearMinutos(minutosTrabajados))
                .tiempoExtra(formatearMinutos(minutosExtra))
                .build();
    }

    private String formatearMinutos(long minutos) {
        if (minutos <= 0) return "0h 0m";
        return (minutos / 60) + "h " + (minutos % 60) + "m";
    }
}