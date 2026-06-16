package com.tfg.trabajoFinGrado.modulos.inventario.servicio;

import com.tfg.trabajoFinGrado.comun.excepcion.RecursoNoEncontradoExcepcion;
import com.tfg.trabajoFinGrado.modulos.inventario.modelo.Stock;
import com.tfg.trabajoFinGrado.modulos.inventario.repositorio.RepositorioStock;
import com.tfg.trabajoFinGrado.modulos.usuarios.modelo.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioInventario {

    private final RepositorioStock repositorioStock;

    public List<Stock> obtenerTodos() {
        return repositorioStock.findByActivoTrueOrderByNombreAsc();
    }

    public Stock obtenerPorId(Long id) {
        return repositorioStock.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Stock no encontrado con id: " + id));
    }

    @Transactional
    public Stock crear(Stock stock, Usuario autor) {
        stock.setCreadoPor(autor);
        return repositorioStock.save(stock);
    }

    @Transactional
    public Stock actualizar(Long id, Stock datosActualizados) {
        Stock stock = obtenerPorId(id);
        stock.setNombre(datosActualizados.getNombre());
        stock.setProveedor(datosActualizados.getProveedor());
        stock.setCantidadTotal(datosActualizados.getCantidadTotal());
        stock.setPrecio(datosActualizados.getPrecio());
        return repositorioStock.save(stock);
    }

    @Transactional
    public void eliminar(Long id) {
        Stock stock = obtenerPorId(id);
        stock.setActivo(false);
        repositorioStock.save(stock);
    }
}