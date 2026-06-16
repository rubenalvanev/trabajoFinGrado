package com.tfg.trabajoFinGrado.modulos.documentos.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion;
import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.documentos.dto.RespuestaDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.modelo.CategoriaDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.modelo.Documento;
import com.tfg.trabajoFinGrado.modulos.documentos.modelo.VersionDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.repositorio.RepositorioDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.repositorio.RepositorioVersionDocumento;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioDocumentos {

    private final RepositorioDocumento repositorioDocumento;
    private final RepositorioVersionDocumento repositorioVersionDocumento;

    @Value("${localytics.documentos.ruta-almacenamiento:uploads/documentos}")
    private String rutaAlmacenamiento;

    public List<RespuestaDocumento> obtenerTodos() {
        return repositorioDocumento.findByActivoTrueOrderByActualizadoEnDesc()
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public List<RespuestaDocumento> obtenerPorCategoria(CategoriaDocumento categoria) {
        return repositorioDocumento.findByActivoTrueAndCategoriaOrderByActualizadoEnDesc(categoria)
                .stream().map(this::mapearADto).collect(Collectors.toList());
    }

    public RespuestaDocumento obtenerPorId(Long id) {
        Documento doc = buscarDocumento(id);
        RespuestaDocumento dto = mapearADto(doc);
        List<RespuestaDocumento.RespuestaVersion> versiones =
                repositorioVersionDocumento.findByDocumentoIdOrderByNumeroVersionDesc(id)
                        .stream().map(this::mapearVersion).collect(Collectors.toList());
        dto.setVersiones(versiones);
        return dto;
    }

    @Transactional
    public RespuestaDocumento crearDocumento(
            String nombre,
            String descripcion,
            CategoriaDocumento categoria,
            MultipartFile archivo,
            String comentario,
            Usuario autor) throws IOException {

        Documento documento = Documento.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .creadoPor(autor)
                .build();
        documento = repositorioDocumento.save(documento);

        // Guardar la primera versión
        guardarVersion(documento, archivo, comentario, autor, 1);

        return mapearADto(repositorioDocumento.findById(documento.getId()).orElseThrow());
    }

    @Transactional
    public RespuestaDocumento subirNuevaVersion(
            Long documentoId,
            MultipartFile archivo,
            String comentario,
            Usuario autor) throws IOException {

        Documento documento = buscarDocumento(documentoId);
        int siguienteVersion = repositorioVersionDocumento.countByDocumentoId(documentoId) + 1;
        guardarVersion(documento, archivo, comentario, autor, siguienteVersion);

        return mapearADto(repositorioDocumento.findById(documentoId).orElseThrow());
    }

    public Resource descargarVersion(Long versionId) throws MalformedURLException {
        VersionDocumento version = repositorioVersionDocumento.findById(versionId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Versión no encontrada"));

        Path ruta = Paths.get(version.getRutaAlmacenamiento());
        Resource recurso = new UrlResource(ruta.toUri());

        if (!recurso.exists()) {
            throw new RecursoNoEncontradoExcepcion("El archivo no existe en el servidor");
        }
        return recurso;
    }

    public String obtenerNombreArchivo(Long versionId) {
        return repositorioVersionDocumento.findById(versionId)
                .map(VersionDocumento::getNombreArchivo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Versión no encontrada"));
    }

    @Transactional
    public void eliminarDocumento(Long id) {
        Documento documento = buscarDocumento(id);
        documento.setActivo(false);
        repositorioDocumento.save(documento);
    }


    private void guardarVersion(
            Documento documento,
            MultipartFile archivo,
            String comentario,
            Usuario autor,
            int numeroVersion) throws IOException {

        Path directorio = Paths.get(rutaAlmacenamiento);
        Files.createDirectories(directorio);

        String nombreUnico = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = directorio.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        VersionDocumento version = VersionDocumento.builder()
                .documento(documento)
                .numeroVersion(numeroVersion)
                .nombreArchivo(archivo.getOriginalFilename())
                .rutaAlmacenamiento(destino.toString())
                .tipoMime(archivo.getContentType())
                .tamanioBytes(archivo.getSize())
                .comentario(comentario)
                .subidoPor(autor)
                .build();

        repositorioVersionDocumento.save(version);
    }

    private Documento buscarDocumento(Long id) {
        return repositorioDocumento.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Documento no encontrado con id: " + id));
    }

    private RespuestaDocumento mapearADto(Documento doc) {
        VersionDocumento ultimaVersion = doc.obtenerUltimaVersion();
        int totalVersiones = repositorioVersionDocumento.countByDocumentoId(doc.getId());

        return RespuestaDocumento.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .descripcion(doc.getDescripcion())
                .categoria(doc.getCategoria())
                .creadoPor(doc.getCreadoPor() != null ? doc.getCreadoPor().obtenerNombreCompleto() : "")
                .creadoEn(doc.getCreadoEn())
                .actualizadoEn(doc.getActualizadoEn())
                .totalVersiones(totalVersiones)
                .ultimaVersion(ultimaVersion != null ? mapearVersion(ultimaVersion) : null)
                .build();
    }

    private RespuestaDocumento.RespuestaVersion mapearVersion(VersionDocumento v) {
        return RespuestaDocumento.RespuestaVersion.builder()
                .id(v.getId())
                .numeroVersion(v.getNumeroVersion())
                .nombreArchivo(v.getNombreArchivo())
                .tipoMime(v.getTipoMime())
                .tamanioBytes(v.getTamanioBytes())
                .comentario(v.getComentario())
                .subidoPor(v.getSubidoPor() != null ? v.getSubidoPor().obtenerNombreCompleto() : "")
                .subidoEn(v.getSubidoEn())
                .build();
    }
}