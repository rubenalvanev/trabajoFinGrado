package com.tfg.trabajoFinGrado.modulos.finanzas.repositorio;

import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.RegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.TipoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface RepositorioRegistroFinanciero extends JpaRepository<RegistroFinanciero, Long> {

    List<RegistroFinanciero> findByTipoOrderByFechaDesc(TipoRegistro tipo);

    List<RegistroFinanciero> findAllByOrderByFechaDesc();

    List<RegistroFinanciero> findByFechaBetweenOrderByFechaDesc(LocalDate inicio, LocalDate fin);

    List<RegistroFinanciero> findByTipoAndFechaBetweenOrderByFechaDesc(
            TipoRegistro tipo, LocalDate inicio, LocalDate fin);

    @Query("SELECT COALESCE(SUM(r.importe), 0) FROM RegistroFinanciero r WHERE r.tipo = :tipo AND r.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumarImportePorTipoYFecha(
            @Param("tipo") TipoRegistro tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(r.importe), 0) FROM RegistroFinanciero r WHERE r.tipo = :tipo")
    BigDecimal sumarImportePorTipo(@Param("tipo") TipoRegistro tipo);
}
