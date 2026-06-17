package com.tfg.trabajoFinGrado.modulos.finanzas.servicio;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.RegistroFinanciero;
import com.tfg.trabajoFinGrado.modulos.finanzas.modelo.TipoRegistro;
import com.tfg.trabajoFinGrado.modulos.finanzas.repositorio.RepositorioRegistroFinanciero;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ServicioPdfFinanzas {

    private final RepositorioRegistroFinanciero repositorioFinanciero;

    private static final DeviceRgb COLOR_CABECERA = new DeviceRgb(30, 41, 59);
    private static final DeviceRgb COLOR_BENEFICIO = new DeviceRgb(220, 252, 231);
    private static final DeviceRgb COLOR_GASTO = new DeviceRgb(254, 226, 226);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public byte[] generarInformeMensual() {
        LocalDate ahora = LocalDate.now();
        LocalDate inicio = ahora.withDayOfMonth(1);
        LocalDate fin = ahora.withDayOfMonth(ahora.lengthOfMonth());

        List<RegistroFinanciero> registros = repositorioFinanciero
                .findByFechaBetweenOrderByFechaDesc(inicio, fin);

        return construirPdf(registros, inicio, fin);
    }

    private byte[] construirPdf(List<RegistroFinanciero> registros, LocalDate inicio, LocalDate fin) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter escritor = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(escritor);
             Document documento = new Document(pdf)) {

            documento.add(new Paragraph("LOCALYTICS ERP")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            documento.add(new Paragraph("Informe Financiero Mensual")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            documento.add(new Paragraph("Período: " + inicio.format(FORMATO_FECHA)
                    + " - " + fin.format(FORMATO_FECHA))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            List<RegistroFinanciero> beneficios = registros.stream()
                    .filter(r -> TipoRegistro.BENEFICIO.equals(r.getTipo()))
                    .toList();

            documento.add(new Paragraph("BENEFICIOS")
                    .setFontSize(12).setBold().setMarginBottom(5));
            documento.add(construirTabla(beneficios));

            BigDecimal totalBeneficios = beneficios.stream()
                    .map(RegistroFinanciero::getImporte)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            documento.add(new Paragraph("Total Beneficios: " + formatearImporte(totalBeneficios))
                    .setBold().setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));

            List<RegistroFinanciero> gastos = registros.stream()
                    .filter(r -> TipoRegistro.GASTO.equals(r.getTipo()))
                    .toList();

            documento.add(new Paragraph("GASTOS")
                    .setFontSize(12).setBold().setMarginBottom(5));
            documento.add(construirTabla(gastos));

            BigDecimal totalGastos = gastos.stream()
                    .map(RegistroFinanciero::getImporte)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            documento.add(new Paragraph("Total Gastos: " + formatearImporte(totalGastos))
                    .setBold().setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));

            BigDecimal resultado = totalBeneficios.subtract(totalGastos);
            documento.add(new Paragraph("RESULTADO TOTAL: " + formatearImporte(resultado))
                    .setFontSize(14).setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(resultado.compareTo(BigDecimal.ZERO) >= 0
                            ? new DeviceRgb(22, 163, 74)
                            : new DeviceRgb(220, 38, 38)));

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    private Table construirTabla(List<RegistroFinanciero> registros) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{15, 40, 25, 20}));
        tabla.setWidth(UnitValue.createPercentValue(100));

        String[] encabezados = {"Fecha", "Descripción", "Categoría", "Importe"};
        for (String encabezado : encabezados) {
            tabla.addHeaderCell(new Cell()
                    .add(new Paragraph(encabezado).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(COLOR_CABECERA));
        }

        if (registros.isEmpty()) {
            tabla.addCell(new Cell(1, 4)
                    .add(new Paragraph("Sin registros")
                            .setTextAlignment(TextAlignment.CENTER)));
            return tabla;
        }

        for (RegistroFinanciero r : registros) {
            DeviceRgb colorFila = TipoRegistro.BENEFICIO.equals(r.getTipo())
                    ? COLOR_BENEFICIO : COLOR_GASTO;

            tabla.addCell(new Cell()
                    .add(new Paragraph(r.getFecha().format(FORMATO_FECHA)))
                    .setBackgroundColor(colorFila));
            tabla.addCell(new Cell()
                    .add(new Paragraph(r.getDescripcion()))
                    .setBackgroundColor(colorFila));
            tabla.addCell(new Cell()
                    .add(new Paragraph(r.getCategoria() != null ? r.getCategoria() : "-"))
                    .setBackgroundColor(colorFila));
            tabla.addCell(new Cell()
                    .add(new Paragraph(formatearImporte(r.getImporte()))
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBackgroundColor(colorFila));
        }

        return tabla;
    }

    private String formatearImporte(BigDecimal importe) {
        return String.format("%.2f €", importe);
    }
}
