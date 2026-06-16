const FichajesPanel = (() => {

    let intervaloReloj = null;

    const renderizar = async (usuario) => {

        if (intervaloReloj) { clearInterval(intervaloReloj); intervaloReloj = null; }

        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';

        try {
            const [fichajeHoy, resumenMes] = await Promise.all([
                API.get('/fichajes/hoy').catch(() => null),
                API.get('/fichajes/resumen-mensual')
            ]);

            const esAdmin = usuario && usuario.rol === 'ADMIN';
            let fichajesHoyTodos = null;
            if (esAdmin) {
                fichajesHoyTodos = await API.get('/fichajes/hoy/todos').catch(() => []);
            }

            renderizarContenido(area, fichajeHoy, resumenMes, fichajesHoyTodos, esAdmin);
            iniciarReloj();
        } catch (e) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar fichajes: ${e.message}</p>`;
        }
    };

    const renderizarContenido = (area, fichajeHoy, resumenMes, fichajesHoyTodos, esAdmin) => {
        const ahora = new Date();
        const fechaHoy = ahora.toLocaleDateString('es-ES', { weekday:'long', year:'numeric', month:'long', day:'numeric' });

        area.innerHTML = `
            <div style="text-align:center; margin-bottom:28px;">
                <div id="reloj-tiempo-real"
                     style="font-family:var(--fuente-display); font-size:52px; font-weight:800;
                            color:var(--color-texto); letter-spacing:2px; line-height:1;">
                    ${formatearHora(ahora)}
                </div>
                <div style="font-size:14px; color:var(--color-texto-secundario); margin-top:6px; text-transform:capitalize;">
                    ${fechaHoy}
                </div>
            </div>

            <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:24px;">
                ${construirTarjetaFichajeHoy(fichajeHoy)}
                ${construirTarjetaResumenDia(fichajeHoy)}
            </div>

            <div style="display:flex; gap:12px; justify-content:center; margin-bottom:28px; flex-wrap:wrap;">
                ${construirBotonesAccion(fichajeHoy)}
            </div>

            ${construirResumenMensual(resumenMes)}

            ${esAdmin ? construirVistaAdmin(fichajesHoyTodos) : ''}

            <div id="modal-fichajes"></div>
        `;
    };

    const construirTarjetaFichajeHoy = (fichaje) => {
        const entrada = fichaje?.horaEntrada
            ? new Date(fichaje.horaEntrada).toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit' })
            : '—';
        const salida = fichaje?.horaSalida
            ? new Date(fichaje.horaSalida).toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit' })
            : fichaje?.fichadoActualmente ? 'En curso...' : '—';

        const estadoColor = !fichaje ? 'var(--color-texto-tenue)'
            : fichaje.fichadoActualmente ? '#22c55e'
            : '#6366f1';
        const estadoTexto = !fichaje ? 'Sin fichar'
            : fichaje.fichadoActualmente ? '● Trabajando'
            : '✓ Jornada completada';

        return `
            <div class="tarjeta">
                <div class="est-etiqueta">Fichaje de hoy</div>
                <div style="margin-top:12px; display:flex; flex-direction:column; gap:10px;">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:13px; color:var(--color-texto-secundario);">Entrada</span>
                        <span style="font-size:18px; font-weight:700; font-family:var(--fuente-display);">
                            ${entrada}
                        </span>
                    </div>
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:13px; color:var(--color-texto-secundario);">Salida</span>
                        <span style="font-size:18px; font-weight:700; font-family:var(--fuente-display);
                                     color:${fichaje?.fichadoActualmente ? '#f59e0b' : 'var(--color-texto)'}">
                            ${salida}
                        </span>
                    </div>
                    <div style="border-top:1px solid var(--color-borde); padding-top:10px;
                                display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:12px; color:var(--color-texto-tenue);">Estado</span>
                        <span style="font-size:13px; font-weight:600; color:${estadoColor};">
                            ${estadoTexto}
                        </span>
                    </div>
                </div>
            </div>
        `;
    };

    const construirTarjetaResumenDia = (fichaje) => {
        const tiempoTrabajado = fichaje?.tiempoTrabajado || '0h 0m';
        const tiempoExtra     = fichaje?.tiempoExtra     || '0h 0m';
        const hayExtra        = (fichaje?.minutosExtra || 0) > 0;

        return `
            <div class="tarjeta">
                <div class="est-etiqueta">Resumen del día</div>
                <div style="margin-top:12px; display:flex; flex-direction:column; gap:10px;">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:13px; color:var(--color-texto-secundario);">Tiempo trabajado</span>
                        <span style="font-size:20px; font-weight:700; font-family:var(--fuente-display);
                                     color:var(--color-primario);">
                            ${tiempoTrabajado}
                        </span>
                    </div>
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:13px; color:var(--color-texto-secundario);">Horas extra</span>
                        <span style="font-size:18px; font-weight:700; font-family:var(--fuente-display);
                                     color:${hayExtra ? '#f59e0b' : 'var(--color-texto-tenue)'};">
                            ${hayExtra ? '+' + tiempoExtra : '—'}
                        </span>
                    </div>
                    <div style="border-top:1px solid var(--color-borde); padding-top:10px;">
                        ${fichaje?.observaciones
                            ? `<div style="font-size:12px; color:var(--color-texto-secundario);">
                                   <em>"${fichaje.observaciones}"</em>
                               </div>`
                            : `<button style="font-size:12px; background:none; border:none; color:var(--color-texto-tenue);
                                              cursor:pointer; padding:0; text-decoration:underline;"
                                       onclick="FichajesPanel.abrirModalObservaciones()">
                                   + Añadir nota del día
                               </button>`}
                    </div>
                </div>
            </div>
        `;
    };

    const construirBotonesAccion = (fichaje) => {
        const puedeEntrar = !fichaje || (!fichaje.horaEntrada);
        const puedeSalir  = fichaje?.fichadoActualmente;
        const yaCompleto  = fichaje?.horaSalida != null;

        if (yaCompleto) {
            return `
                <div style="text-align:center; padding:16px 24px; background:var(--color-superficie);
                             border:1px solid var(--color-borde); border-radius:10px;">
                    <span style="color:var(--color-exito); font-weight:600;">✓ Jornada completada hoy</span>
                </div>
            `;
        }

        return `
            ${puedeEntrar ? `
                <button class="btn-primario" style="padding:14px 36px; font-size:16px; border-radius:10px;"
                        onclick="FichajesPanel.ficharEntrada()">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <polyline points="12 6 12 12 16 14"/>
                    </svg>
                    Registrar entrada
                </button>
            ` : ''}
            ${puedeSalir ? `
                <button class="btn-secundario" style="padding:14px 36px; font-size:16px; border-radius:10px;
                              border-color:#f59e0b; color:#f59e0b;"
                        onclick="FichajesPanel.ficharSalida()">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    Registrar salida
                </button>
            ` : ''}
        `;
    };

    const construirResumenMensual = (resumen) => {
        const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
        const nombreMes = meses[(resumen.mes || 1) - 1];

        return `
            <div class="tarjeta" style="margin-bottom:20px;">
                <div class="tarjeta-cabecera">
                    <div>
                        <div class="tarjeta-titulo">Mi mes — ${nombreMes} ${resumen.anio}</div>
                        <div style="font-size:13px; color:var(--color-texto-secundario); margin-top:2px;">
                            Resumen de tu jornada mensual
                        </div>
                    </div>
                    <button class="btn-secundario" style="font-size:13px;"
                            onclick="FichajesPanel.abrirSelectorMes()">
                        Cambiar mes
                    </button>
                </div>

                <div class="cuadricula-estadisticas" style="margin-bottom:20px;">
                    <div class="tarjeta-estadistica">
                        <div class="est-etiqueta">Días trabajados</div>
                        <div class="est-valor neutral">${resumen.diasTrabajados || 0}</div>
                    </div>
                    <div class="tarjeta-estadistica">
                        <div class="est-etiqueta">Tiempo total</div>
                        <div class="est-valor positivo">${resumen.totalTiempoTrabajado || '0h 0m'}</div>
                    </div>
                    <div class="tarjeta-estadistica">
                        <div class="est-etiqueta">Horas extra</div>
                        <div class="est-valor ${(resumen.totalMinutosExtra || 0) > 0 ? 'neutral' : 'neutral'}"
                             style="color:${(resumen.totalMinutosExtra || 0) > 0 ? '#f59e0b' : 'var(--color-texto-tenue)'}">
                            ${(resumen.totalMinutosExtra || 0) > 0 ? '+' + resumen.totalTiempoExtra : '—'}
                        </div>
                    </div>
                </div>

                ${(resumen.fichajes && resumen.fichajes.length > 0) ? `
                    <div class="contenedor-tabla">
                        <table>
                            <thead><tr>
                                <th>Fecha</th>
                                <th>Entrada</th>
                                <th>Salida</th>
                                <th>Tiempo trabajado</th>
                                <th>Horas extra</th>
                                <th>Notas</th>
                            </tr></thead>
                            <tbody>
                                ${resumen.fichajes.map(f => construirFilaHistorial(f)).join('')}
                            </tbody>
                        </table>
                    </div>
                ` : `
                    <p style="text-align:center; color:var(--color-texto-tenue);
                               padding:24px; font-size:13px;">
                        Sin fichajes registrados este mes
                    </p>
                `}
            </div>
        `;
    };

    const construirFilaHistorial = (f) => {
        const fecha    = new Date(f.fecha + 'T00:00:00').toLocaleDateString('es-ES', { weekday:'short', day:'2-digit', month:'2-digit' });
        const entrada  = f.horaEntrada ? new Date(f.horaEntrada).toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit' }) : '—';
        const salida   = f.horaSalida  ? new Date(f.horaSalida).toLocaleTimeString('es-ES',  { hour:'2-digit', minute:'2-digit' }) : (f.fichadoActualmente ? '🟢 En curso' : '—');
        const hayExtra = (f.minutosExtra || 0) > 0;

        return `
            <tr>
                <td style="font-weight:500; text-transform:capitalize;">${fecha}</td>
                <td style="color:#86efac;">${entrada}</td>
                <td style="color:${f.fichadoActualmente ? '#f59e0b' : 'var(--color-texto)'};">${salida}</td>
                <td style="font-weight:600;">${f.tiempoTrabajado || '—'}</td>
                <td style="color:${hayExtra ? '#f59e0b' : 'var(--color-texto-tenue)'};">
                    ${hayExtra ? '+' + f.tiempoExtra : '—'}
                </td>
                <td style="font-size:12px; color:var(--color-texto-secundario); font-style:italic;">
                    ${f.observaciones || ''}
                </td>
            </tr>
        `;
    };

    const construirVistaAdmin = (fichajes) => {
        if (!fichajes) return '';
        const activos    = fichajes.filter(f => f.fichadoActualmente).length;
        const completados = fichajes.filter(f => f.horaSalida).length;
        const sinFichar   = fichajes.filter(f => !f.horaEntrada).length;

        return `
            <div class="tarjeta">
                <div class="tarjeta-cabecera">
                    <div class="tarjeta-titulo">Equipo hoy</div>
                    <div style="display:flex; gap:10px;">
                        <span class="badge" style="background:rgba(34,197,94,0.1); color:#86efac;">
                            ${activos} trabajando
                        </span>
                        <span class="badge" style="background:rgba(99,102,241,0.1); color:#a5b4fc;">
                            ${completados} finalizados
                        </span>
                        <span class="badge" style="background:rgba(100,100,120,0.1); color:var(--color-texto-tenue);">
                            ${sinFichar} sin fichar
                        </span>
                    </div>
                </div>
                ${fichajes.length === 0
                    ? '<p style="text-align:center; color:var(--color-texto-tenue); padding:24px; font-size:13px;">Nadie ha fichado hoy todavía</p>'
                    : `<div class="contenedor-tabla">
                        <table>
                            <thead><tr>
                                <th>Empleado</th>
                                <th>Entrada</th>
                                <th>Salida</th>
                                <th>Tiempo trabajado</th>
                                <th>Estado</th>
                            </tr></thead>
                            <tbody>
                                ${fichajes.map(f => {
                                    const entrada = f.horaEntrada
                                        ? new Date(f.horaEntrada).toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit' })
                                        : '—';
                                    const salida = f.horaSalida
                                        ? new Date(f.horaSalida).toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit' })
                                        : f.fichadoActualmente ? 'En curso' : '—';
                                    const estadoBadge = f.fichadoActualmente
                                        ? '<span class="badge" style="background:rgba(34,197,94,0.1);color:#86efac;">● Trabajando</span>'
                                        : f.horaSalida
                                        ? '<span class="badge" style="background:rgba(99,102,241,0.1);color:#a5b4fc;">✓ Completado</span>'
                                        : '<span class="badge" style="background:rgba(100,100,120,0.1);color:var(--color-texto-tenue);">Sin fichar</span>';
                                    return `
                                        <tr>
                                            <td>
                                                <div style="display:flex; align-items:center; gap:8px;">
                                                    <div class="avatar-tabla">${f.nombreUsuario.charAt(0)}</div>
                                                    <span style="font-weight:500;">${f.nombreUsuario}</span>
                                                </div>
                                            </td>
                                            <td style="color:#86efac; font-weight:500;">${entrada}</td>
                                            <td>${salida}</td>
                                            <td style="font-weight:600;">${f.tiempoTrabajado || '—'}</td>
                                            <td>${estadoBadge}</td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>`}
            </div>
        `;
    };

    const iniciarReloj = () => {
        intervaloReloj = setInterval(() => {
            const reloj = document.getElementById('reloj-tiempo-real');
            if (!reloj) { clearInterval(intervaloReloj); return; }
            reloj.textContent = formatearHora(new Date());
        }, 1000);
    };

    const formatearHora = (fecha) =>
        fecha.toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit', second:'2-digit' });

    const ficharEntrada = async () => {
        try {
            await API.post('/fichajes/entrada');
            Aplicacion.mostrarToast('¡Entrada registrada! Buen día de trabajo 🟢', 'exito');
            await renderizar(window._usuarioActualFichajes);
        } catch (e) {
            Aplicacion.mostrarToast('Error: ' + e.message, 'error');
        }
    };

    const ficharSalida = async () => {
        try {
            await API.post('/fichajes/salida');
            Aplicacion.mostrarToast('¡Salida registrada! Hasta mañana 👋', 'exito');
            await renderizar(window._usuarioActualFichajes);
        } catch (e) {
            Aplicacion.mostrarToast('Error: ' + e.message, 'error');
        }
    };

    const abrirModalObservaciones = () => {
        document.getElementById('modal-fichajes').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Nota del día</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Observaciones</label>
                            <textarea class="campo-textarea" id="obs-texto"
                                      placeholder="Ej: Reunión con cliente, trabajo en remoto, visita médica..."
                                      style="height:100px;"></textarea>
                        </div>
                        <div id="obs-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="FichajesPanel.guardarObservaciones()">Guardar</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const guardarObservaciones = async () => {
        const errEl = document.getElementById('obs-error');
        errEl.classList.add('oculto');
        try {
            await API.patch('/fichajes/observaciones', {
                observaciones: document.getElementById('obs-texto').value.trim()
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Nota guardada', 'exito');
            await renderizar(window._usuarioActualFichajes);
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const abrirSelectorMes = () => {
        const ahora = new Date();
        const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

        document.getElementById('modal-fichajes').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Seleccionar mes</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Mes</label>
                                <select class="campo-select" id="sel-mes">
                                    ${meses.map((m, i) => `
                                        <option value="${i+1}" ${i+1 === ahora.getMonth()+1 ? 'selected' : ''}>
                                            ${m}
                                        </option>`).join('')}
                                </select>
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Año</label>
                                <select class="campo-select" id="sel-anio">
                                    ${[ahora.getFullYear()-1, ahora.getFullYear()].map(a => `
                                        <option value="${a}" ${a === ahora.getFullYear() ? 'selected' : ''}>
                                            ${a}
                                        </option>`).join('')}
                                </select>
                            </div>
                        </div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="FichajesPanel.cargarMes()">Ver mes</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const cargarMes = async () => {
        const mes  = document.getElementById('sel-mes').value;
        const anio = document.getElementById('sel-anio').value;
        document.querySelector('.modal-fondo')?.remove();
        try {
            const resumen = await API.get(`/fichajes/resumen-mensual?anio=${anio}&mes=${mes}`);
            const contenedor = document.querySelector('.tarjeta');
            if (contenedor) {
                const html = construirResumenMensual(resumen);
                const div = document.createElement('div');
                div.innerHTML = html;
                await renderizar(window._usuarioActualFichajes);
            }
        } catch (e) {
            Aplicacion.mostrarToast('Error: ' + e.message, 'error');
        }
    };

    const renderizarConUsuario = async (usuario) => {
        window._usuarioActualFichajes = usuario;
        await renderizar(usuario);
    };

    return {
        renderizar: renderizarConUsuario,
        ficharEntrada,
        ficharSalida,
        abrirModalObservaciones,
        guardarObservaciones,
        abrirSelectorMes,
        cargarMes
    };
})();