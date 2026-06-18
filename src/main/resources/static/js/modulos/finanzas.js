const FinanzasPanel = (() => {

    let graficoBeneficios = null;
    let graficoGastos = null;
    let graficoTotal = null;

    const renderizar = async () => {
        const area = document.getElementById('area-modulo');
        area.innerHTML = `
            <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"><\/script>
            <div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>
        `;

        await cargarChartJs();

        try {
            const [resumen, todos] = await Promise.all([
                API.get('/finanzas/resumen'),
                API.get('/finanzas')
            ]);
            renderizarContenido(area, resumen, todos);
        } catch (e) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar finanzas: ${e.message}</p>`;
        }
    };

    const cargarChartJs = () => new Promise((resolve) => {
        if (window.Chart) { resolve(); return; }
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js';
        script.onload = resolve;
        document.head.appendChild(script);
    });

    const renderizarContenido = (area, resumen, todos) => {
        const fmt = (n) => Number(n || 0).toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
        const totalActual = Number(resumen.totalMesActual || 0);

        area.innerHTML = `
            <div class="cuadricula-estadisticas" style="margin-bottom:20px;">
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Total beneficios</div>
                    <div class="est-valor positivo">${fmt(resumen.totalBeneficios)}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Total gastos</div>
                    <div class="est-valor negativo">${fmt(resumen.totalGastos)}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Resultado global</div>
                    <div class="est-valor ${Number(resumen.total) >= 0 ? 'positivo' : 'negativo'}">${fmt(resumen.total)}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Mes pasado (resultado)</div>
                    <div class="est-valor ${Number(resumen.totalMesPasado) >= 0 ? 'positivo' : 'negativo'}">${fmt(resumen.totalMesPasado)}</div>
                </div>
            </div>

            <div class="cuadricula-graficos" style="margin-bottom:24px;">
                <div class="tarjeta-grafico">
                    <div class="grafico-titulo">Beneficios del mes</div>
                    <canvas id="grafico-beneficios" class="grafico-donut"></canvas>
                    <div style="margin-top:10px; font-size:13px; color:var(--color-texto-secundario);">
                        Mes actual: <strong style="color:#86efac;">${fmt(resumen.beneficiosMesActual)}</strong>
                    </div>
                </div>
                <div class="tarjeta-grafico">
                    <div class="grafico-titulo">Gastos del mes</div>
                    <canvas id="grafico-gastos" class="grafico-donut"></canvas>
                    <div style="margin-top:10px; font-size:13px; color:var(--color-texto-secundario);">
                        Mes actual: <strong style="color:#fca5a5;">${fmt(resumen.gastosMesActual)}</strong>
                    </div>
                </div>
                <div class="tarjeta-grafico">
                    <div class="grafico-titulo">Balance total del mes</div>
                    <canvas id="grafico-total" class="grafico-donut"></canvas>
                    <div style="margin-top:10px; font-size:13px; color:var(--color-texto-secundario);">
                        Resultado: <strong class="${totalActual >= 0 ? 'positivo' : 'negativo'}" style="color:${totalActual >= 0 ? '#86efac' : '#fca5a5'}">${fmt(totalActual)}</strong>
                    </div>
                </div>
            </div>

            <div class="barra-acciones" style="margin-bottom:16px;">
                <h3 class="tarjeta-titulo">Registros financieros</h3>
                <div style="display:flex; gap:10px; flex-wrap:wrap;">
                    <button class="btn-secundario" onclick="FinanzasPanel.descargarPdf()">
                        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>
                        PDF mensual
                    </button>
                    <button class="btn-primario" onclick="FinanzasPanel.abrirModal()">
                        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                        Nuevo registro
                    </button>
                </div>
            </div>

            <div class="tarjeta">
                <div class="contenedor-tabla">
                    <table>
                        <thead><tr>
                            <th>Fecha</th><th>Tipo</th><th>Descripción</th><th>Categoría</th><th>Importe</th><th>Acciones</th>
                        </tr></thead>
                        <tbody id="tabla-finanzas">
                            ${todos.length === 0 ? '<tr><td colspan="6" style="text-align:center; color:var(--color-texto-tenue); padding:32px;">Sin registros. Añade el primer registro.</td></tr>' : todos.map(r => construirFila(r)).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
            <div id="modal-finanzas"></div>
        `;

        setTimeout(() => {
            dibujarGraficoBeneficios(resumen);
            dibujarGraficoGastos(resumen);
            dibujarGraficoTotal(resumen);
        }, 50);
    };

    const construirFila = (r) => {
        const fmt = (n) => Number(n).toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
        const fecha = new Date(r.fecha).toLocaleDateString('es-ES');
        return `
            <tr>
                <td>${fecha}</td>
                <td><span class="badge badge-${r.tipo.toLowerCase()}">${r.tipo === 'BENEFICIO' ? '▲ Beneficio' : '▼ Gasto'}</span></td>
                <td>${r.descripcion}</td>
                <td>${r.categoria || '—'}</td>
                <td style="font-weight:600; color:${r.tipo === 'BENEFICIO' ? '#86efac' : '#fca5a5'}">${fmt(r.importe)}</td>
                <td>
                    <button class="btn-peligro" onclick="FinanzasPanel.eliminar(${r.id})">
                        <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/></svg>
                    </button>
                </td>
            </tr>
        `;
    };

    const dibujarGraficoBeneficios = (resumen) => {
        const ctx = document.getElementById('grafico-beneficios');
        if (!ctx || !window.Chart) return;
        if (graficoBeneficios) graficoBeneficios.destroy();
        const actual = Math.max(0, Number(resumen.beneficiosMesActual));
        const pasado = Math.max(0, Number(resumen.beneficiosMesPasado));
        graficoBeneficios = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Mes actual', 'Mes pasado'],
                datasets: [{ data: [actual || 0.001, pasado || 0.001], backgroundColor: ['#22c55e', '#166534'], borderWidth: 0, hoverOffset: 4 }]
            },
            options: { responsive: true, cutout: '70%', plugins: { legend: { position: 'bottom', labels: { color: '#8888a8', font: { size: 11 } } } } }
        });
    };

    const dibujarGraficoGastos = (resumen) => {
        const ctx = document.getElementById('grafico-gastos');
        if (!ctx || !window.Chart) return;
        if (graficoGastos) graficoGastos.destroy();
        const actual = Math.max(0, Number(resumen.gastosMesActual));
        const pasado = Math.max(0, Number(resumen.gastosMesPasado));
        graficoGastos = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Mes actual', 'Mes pasado'],
                datasets: [{ data: [actual || 0.001, pasado || 0.001], backgroundColor: ['#ef4444', '#7f1d1d'], borderWidth: 0, hoverOffset: 4 }]
            },
            options: { responsive: true, cutout: '70%', plugins: { legend: { position: 'bottom', labels: { color: '#8888a8', font: { size: 11 } } } } }
        });
    };

    const dibujarGraficoTotal = (resumen) => {
        const ctx = document.getElementById('grafico-total');
        if (!ctx || !window.Chart) return;
        if (graficoTotal) graficoTotal.destroy();
        const ben = Math.max(0, Number(resumen.beneficiosMesActual));
        const gas = Math.max(0, Number(resumen.gastosMesActual));
        graficoTotal = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Beneficios', 'Gastos'],
                datasets: [{ data: [ben || 0.001, gas || 0.001], backgroundColor: ['#22c55e', '#ef4444'], borderWidth: 0, hoverOffset: 4 }]
            },
            options: { responsive: true, cutout: '70%', plugins: { legend: { position: 'bottom', labels: { color: '#8888a8', font: { size: 11 } } } } }
        });
    };

    const abrirModal = () => {
        document.getElementById('modal-finanzas').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Nuevo registro</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Tipo *</label>
                            <select class="campo-select" id="fin-tipo">
                                <option value="BENEFICIO">Beneficio</option>
                                <option value="GASTO">Gasto</option>
                            </select>
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Descripción *</label>
                            <input class="campo-input" id="fin-descripcion" type="text" placeholder="Ej: Venta de servicios">
                        </div>
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Importe (€) *</label>
                                <input class="campo-input" id="fin-importe" type="number" step="0.01" min="0.01" placeholder="0.00">
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Fecha</label>
                                <input class="campo-input" id="fin-fecha" type="date" value="${new Date().toISOString().split('T')[0]}">
                            </div>
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Categoría</label>
                            <input class="campo-input" id="fin-categoria" type="text" placeholder="Ej: Ventas, Alquiler...">
                        </div>
                        <div id="fin-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="FinanzasPanel.crear()">Guardar</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const crear = async () => {
        const errEl = document.getElementById('fin-error');
        errEl.classList.add('oculto');
        try {
            await API.post('/finanzas', {
                tipo: document.getElementById('fin-tipo').value,
                descripcion: document.getElementById('fin-descripcion').value,
                importe: parseFloat(document.getElementById('fin-importe').value),
                categoria: document.getElementById('fin-categoria').value || null,
                fecha: document.getElementById('fin-fecha').value || null
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Registro creado correctamente', 'exito');
            await renderizar();
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const eliminar = async (id) => {
        if (!confirm('¿Eliminar este registro?')) return;
        try {
            await API.del(`/finanzas/${id}`);
            Aplicacion.mostrarToast('Registro eliminado', 'exito');
            await renderizar();
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const descargarPdf = async () => {
        try {
            await API.descargar('/finanzas/pdf/mensual', 'informe-financiero-mensual.pdf');
            Aplicacion.mostrarToast('PDF descargado', 'exito');
        } catch (e) { Aplicacion.mostrarToast('Error al generar PDF: ' + e.message, 'error'); }
    };

    return { renderizar, abrirModal, crear, eliminar, descargarPdf };
})();
