const InventarioPanel = (() => {

    const renderizar = async () => {
        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';
        try {
            const stock = await API.get('/inventario');
            renderizarContenido(area, stock);
        } catch (e) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar el inventario: ${e.message}</p>`;
        }
    };

    const renderizarContenido = (area, stock) => {
        const fmt = (n) => Number(n).toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
        const totalValor = stock.reduce((acc, s) => acc + (s.precio * s.cantidadTotal), 0);
        const totalUnidades = stock.reduce((acc, s) => acc + s.cantidadTotal, 0);

        area.innerHTML = `
            <div class="cuadricula-estadisticas" style="margin-bottom:20px;">
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Artículos en stock</div>
                    <div class="est-valor neutral">${stock.length}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Unidades totales</div>
                    <div class="est-valor neutral">${totalUnidades.toLocaleString('es-ES')}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Valor total del stock</div>
                    <div class="est-valor positivo">${fmt(totalValor)}</div>
                </div>
            </div>

            <div class="barra-acciones">
                <h3 class="tarjeta-titulo">Stock</h3>
                <button class="btn-primario" onclick="InventarioPanel.abrirModalCrear()">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Añadir stock
                </button>
            </div>

            <div class="tarjeta" style="margin-top:16px;">
                <div class="contenedor-tabla">
                    <table>
                        <thead><tr>
                            <th>Nombre</th><th>Proveedor</th><th>Cantidad</th><th>Precio unitario</th><th>Valor total</th><th>Acciones</th>
                        </tr></thead>
                        <tbody>
                            ${stock.length === 0
                                ? '<tr><td colspan="6" style="text-align:center; color:var(--color-texto-tenue); padding:32px;">No hay artículos en el inventario.</td></tr>'
                                : stock.map(s => construirFila(s, fmt)).join('')
                            }
                        </tbody>
                    </table>
                </div>
            </div>
            <div id="modal-inventario"></div>
        `;
    };

    const construirFila = (s, fmt) => `
        <tr>
            <td style="font-weight:500;">${s.nombre}</td>
            <td>${s.proveedor}</td>
            <td>
                <span style="font-weight:600; color:${s.cantidadTotal < 10 ? '#fcd34d' : 'var(--color-texto)'}">
                    ${s.cantidadTotal.toLocaleString('es-ES')}
                    ${s.cantidadTotal < 10 ? ' ⚠' : ''}
                </span>
            </td>
            <td>${fmt(s.precio)}</td>
            <td style="font-weight:600; color:var(--color-exito)">${fmt(s.precio * s.cantidadTotal)}</td>
            <td>
                <div style="display:flex; gap:6px;">
                    <button class="btn-icono" title="Editar" onclick="InventarioPanel.abrirModalEditar(${s.id}, '${escapar(s.nombre)}', '${escapar(s.proveedor)}', ${s.cantidadTotal}, ${s.precio})">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <button class="btn-peligro" title="Eliminar" onclick="InventarioPanel.eliminar(${s.id}, '${escapar(s.nombre)}')">
                        <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/></svg>
                    </button>
                </div>
            </td>
        </tr>
    `;

    const escapar = (s) => String(s).replace(/'/g, "\\'");

    const formularioStock = (valores = {}) => `
        <div class="modal-form">
            <div class="campo-grupo">
                <label class="campo-etiqueta">Nombre del producto *</label>
                <input class="campo-input" id="inv-nombre" type="text" value="${valores.nombre || ''}" placeholder="Ej: Tornillos M6">
            </div>
            <div class="campo-grupo">
                <label class="campo-etiqueta">Proveedor *</label>
                <input class="campo-input" id="inv-proveedor" type="text" value="${valores.proveedor || ''}" placeholder="Ej: Distribuciones García">
            </div>
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                <div class="campo-grupo">
                    <label class="campo-etiqueta">Cantidad total *</label>
                    <input class="campo-input" id="inv-cantidad" type="number" min="0" value="${valores.cantidadTotal ?? ''}">
                </div>
                <div class="campo-grupo">
                    <label class="campo-etiqueta">Precio unitario (€) *</label>
                    <input class="campo-input" id="inv-precio" type="number" step="0.01" min="0.01" value="${valores.precio ?? ''}">
                </div>
            </div>
            <div id="inv-error" class="mensaje-error oculto"></div>
        </div>
    `;

    const abrirModalCrear = () => {
        document.getElementById('modal-inventario').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Añadir stock</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    ${formularioStock()}
                    <div class="modal-acciones" style="margin-top:16px;">
                        <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                        <button class="btn-primario" onclick="InventarioPanel.crear()">Añadir</button>
                    </div>
                </div>
            </div>
        `;
    };

    const abrirModalEditar = (id, nombre, proveedor, cantidadTotal, precio) => {
        document.getElementById('modal-inventario').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Editar stock</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    ${formularioStock({ nombre, proveedor, cantidadTotal, precio })}
                    <div class="modal-acciones" style="margin-top:16px;">
                        <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                        <button class="btn-primario" onclick="InventarioPanel.actualizar(${id})">Guardar</button>
                    </div>
                </div>
            </div>
        `;
    };

    const obtenerDatosFormulario = () => ({
        nombre: document.getElementById('inv-nombre').value.trim(),
        proveedor: document.getElementById('inv-proveedor').value.trim(),
        cantidadTotal: parseInt(document.getElementById('inv-cantidad').value),
        precio: parseFloat(document.getElementById('inv-precio').value)
    });

    const crear = async () => {
        const errEl = document.getElementById('inv-error');
        errEl.classList.add('oculto');
        try {
            const datos = obtenerDatosFormulario();
            await API.post('/inventario', datos);
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Stock añadido correctamente', 'exito');
            await renderizar();
        } catch (e) { errEl.textContent = e.message; errEl.classList.remove('oculto'); }
    };

    const actualizar = async (id) => {
        const errEl = document.getElementById('inv-error');
        errEl.classList.add('oculto');
        try {
            const datos = obtenerDatosFormulario();
            await API.put(`/inventario/${id}`, datos);
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Stock actualizado', 'exito');
            await renderizar();
        } catch (e) { errEl.textContent = e.message; errEl.classList.remove('oculto'); }
    };

    const eliminar = async (id, nombre) => {
        if (!confirm(`¿Eliminar "${nombre}" del inventario?`)) return;
        try {
            await API.del(`/inventario/${id}`);
            Aplicacion.mostrarToast('Artículo eliminado', 'exito');
            await renderizar();
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    return { renderizar, abrirModalCrear, abrirModalEditar, crear, actualizar, eliminar };
})();