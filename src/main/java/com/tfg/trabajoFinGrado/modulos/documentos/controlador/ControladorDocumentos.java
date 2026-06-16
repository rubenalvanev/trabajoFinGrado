package com.tfg.trabajoFinGrado.modulos.documentos.controlador;

import com.tfg.trabajoFinGrado.comun.excepcion.AccesoDenegadoExcepcion;
import com.tfg.trabajoFinGrado.comun.respuesta.RespuestaApi;
import com.tfg.trabajoFinGrado.modulos.documentos.dto.RespuestaDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.modelo.CategoriaDocumento;
import com.tfg.trabajoFinGrado.modulos.documentos.servicio.ServicioDocumentos;
import com.tfg.trabajoFinGrado.modulos.modulos.servicio.ServicioModulo;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class ControladorDocumentos {

    private final ServicioDocumentos servicioDocumentos;
    private final ServicioModulo servicioModulo;

    private void verificarModuloActivo() {
        if (!servicioModulo.estaActivo("DOCUMENTOS")) {
            throw new AccesoDenegadoExcepcion("El módulo de Documentos no está activo");
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaApi<List<RespuestaDocumento>>> obtenerTodos() {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioDocumentos.obtenerTodos()));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<RespuestaApi<List<RespuestaDocumento>>> obtenerPorCategoria(
            @PathVariable CategoriaDocumento categoria) {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioDocumentos.obtenerPorCategoria(categoria)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<RespuestaDocumento>> obtenerPorId(@PathVariable Long id) {
        verificarModuloActivo();
        return ResponseEntity.ok(RespuestaApi.exito(servicioDocumentos.obtenerPorId(id)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespuestaApi<RespuestaDocumento>> subir(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("categoria") CategoriaDocumento categoria,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "comentario", required = false) String comentario,
            @AuthenticationPrincipal Usuario usuarioActual) throws IOException {
        verificarModuloActivo();
        RespuestaDocumento creado = servicioDocumentos.crearDocumento(
                nombre, descripcion, categoria, archivo, comentario, usuarioActual);
        return ResponseEntity.ok(RespuestaApi.exito("Documento subido correctamente", creado));
    }

    @PostMapping(value = "/{id}/versiones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespuestaApi<RespuestaDocumento>> subirVersion(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "comentario", required = false) String comentario,
            @AuthenticationPrincipal Usuario usuarioActual) throws IOException {
        verificarModuloActivo();
        RespuestaDocumento actualizado = servicioDocumentos.subirNuevaVersion(id, archivo, comentario, usuarioActual);
        return ResponseEntity.ok(RespuestaApi.exito("Nueva versión subida correctamente", actualizado));
    }

    @GetMapping("/versiones/{versionId}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long versionId) throws Exception {
        verificarModuloActivo();
        Resource recurso = servicioDocumentos.descargarVersion(versionId);
        String nombreArchivo = servicioDocumentos.obtenerNombreArchivo(versionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(recurso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        verificarModuloActivo();
        servicioDocumentos.eliminarDocumento(id);
        return ResponseEntity.ok(RespuestaApi.exito("Documento eliminado correctamente", null));
    }
}