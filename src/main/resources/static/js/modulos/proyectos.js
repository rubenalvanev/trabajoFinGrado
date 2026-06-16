/**
 * Módulo de Proyectos.
 * Gestión de proyectos, estados, empleados y clientes.
 */
const ProyectosPanel = (() => {

    const COLORES_ESTADO = {
        PLANIFICANDO: 'badge-planificando',
        EN_PROCESO: 'badge-en_proceso',
        FINALIZANDO: 'badge-finalizando',
        ACABADO: 'badge-acabado'
    };

    const renderizar = async () => {
        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';
        try {
            const [proyectos, empleados, clientes] = await Promise.all([
                API.get('/proyectos'),
                API.get('/usuarios/empleados'),
                API.get('/proyectos/clientes')
            ]);
            renderizarContenido(area, proyectos, empleados, clientes);
        } catch (e) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar proyectos: ${e.message}</p>`;
        }
    };

    const renderizarContenido = (area, proyectos, empleados, clientes) => {
        const activos = proyectos.filter(p => p.estado !== 'ACABADO').length;
        const finalizados = proyectos.filter(p => p.estado === 'ACABADO').length;

        area.innerHTML = `
            <div class="cuadricula-estadisticas" style="margin-bottom:20px;">
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Total proyectos</div>
                    <div class="est-valor neutral">${proyectos.length}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">En curso</div>
                    <div class="est-valor positivo">${activos}</div>
                </div>
                <div class="tarjeta-estadistica">
                    <div class="est-etiqueta">Finalizados</div>
                    <div class="est-valor neutral">${finalizados}</div>
                </div>
            </div>

            <div class="barra-acciones">
                <h3 class="tarjeta-titulo">Proyectos</h3>
                <div style="display:flex; gap:10px;">
                    <button class="btn-secundario" onclick="ProyectosPanel.abrirModalCliente()">
                        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                        Nuevo cliente
                    </button>
                    <button class="btn-primario" onclick="ProyectosPanel.abrirModalCrear()">
                        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                        Nuevo proyecto
                    </button>
                </div>
            </div>

            <div id="lista-proyectos" style="margin-top:16px;">
                ${proyectos.length === 0
                    ? '<div class="tarjeta" style="text-align:center; color:var(--color-texto-tenue); padding:48px;">No hay proyectos aún. Crea el primero.</div>'
                    : proyectos.map(p => construirTarjeta(p)).join('')
                }
            </div>
            <div id="modal-proyectos"></div>
        `;

        // Guardar empleados y clientes para uso en modales
        window._proyectosEmpleados = empleados;
        window._proyectosClientes = clientes;
    };

    const construirTarjeta = (p) => {
        const fecha = p.fechaInicio ? new Date(p.fechaInicio).toLocaleDateString('es-ES') : '—';
        const empleados = p.empleados && p.empleados.length > 0 ? p.empleados.join(', ') : 'Sin asignar';
        const clientes = p.clientes && p.clientes.length > 0 ? p.clientes.join(', ') : 'Sin cliente';
        const esAcabado = p.estado === 'ACABADO';

        return `
            <div class="tarjeta-proyecto">
                <div class="proyecto-cabecera">
                    <div>
                        <div class="proyecto-nombre">${p.nombre}</div>
                        ${p.descripcion ? `<div style="font-size:13px; color:var(--color-texto-secundario); margin-top:4px;">${p.descripcion}</div>` : ''}
                    </div>
                    <span class="badge ${COLORES_ESTADO[p.estado] || ''}">${p.etiquetaEstado}</span>
                </div>
                <div class="proyecto-meta">
                    <span>👥 ${empleados}</span>
                    <span>🤝 ${clientes}</span>
                    <span>📅 ${fecha}</span>
                    ${p.creadoPor ? `<span>✍️ ${p.creadoPor}</span>` : ''}
                </div>
                <div class="proyecto-acciones" style="margin-top:14px;">
                    ${!esAcabado ? `
                        <button class="btn-secundario" style="font-size:13px; padding:7px 14px;" onclick="ProyectosPanel.abrirModalEditar(${p.id})">
                            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                            Editar
                        </button>
                        <button class="btn-primario" style="font-size:13px; padding:7px 14px; background:#22c55e;" onclick="ProyectosPanel.finalizar(${p.id}, '${escapar(p.nombre)}')">
                            ✓ Finalizar
                        </button>
                    ` : ''}
                    <button class="btn-peligro" style="font-size:13px; padding:7px 14px;" onclick="ProyectosPanel.eliminar(${p.id}, '${escapar(p.nombre)}')">
                        <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/></svg>
                        Eliminar
                    </button>
                </div>
            </div>
        `;
    };

    const escapar = (s) => String(s).replace(/'/g, "\\'");

    const opcionesEmpleados = () => (window._proyectosEmpleados || [])
        .map(e => `<option value="${e.id}">${e.nombreCompleto}</option>`).join('');

    const opcionesClientes = () => (window._proyectosClientes || [])
        .map(c => `<option value="${c.id}">${c.nombre}${c.empresa ? ` (${c.empresa})` : ''}</option>`).join('');

    const abrirModalCrear = () => {
        document.getElementById('modal-proyectos').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Nuevo proyecto</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Nombre del proyecto *</label>
                            <input class="campo-input" id="proy-nombre" type="text" placeholder="Ej: Rediseño web corporativa">
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Descripción</label>
                            <textarea class="campo-textarea" id="proy-descripcion" placeholder="Descripción del proyecto..."></textarea>
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Empleados asignados</label>
                            <select class="campo-select" id="proy-empleados" multiple style="height:90px;">
                                ${opcionesEmpleados()}
                            </select>
                            <small style="color:var(--color-texto-tenue);">Mantén Ctrl para seleccionar varios</small>
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Clientes</label>
                            <select class="campo-select" id="proy-clientes" multiple style="height:90px;">
                                ${opcionesClientes()}
                            </select>
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Fecha de inicio</label>
                            <input class="campo-input" id="proy-fecha" type="date" value="${new Date().toISOString().split('T')[0]}">
                        </div>
                        <div id="proy-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="ProyectosPanel.crear()">Crear proyecto</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const abrirModalEditar = async (id) => {
        try {
            const p = await API.get(`/proyectos/${id}`);
            document.getElementById('modal-proyectos').innerHTML = `
                <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                    <div class="modal">
                        <div class="modal-cabecera">
                            <h3 class="modal-titulo">Editar proyecto</h3>
                            <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                        </div>
                        <div class="modal-form">
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Nombre *</label>
                                <input class="campo-input" id="edit-nombre" value="${p.nombre}">
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Estado</label>
                                <select class="campo-select" id="edit-estado">
                                    <option value="PLANIFICANDO" ${p.estado === 'PLANIFICANDO' ? 'selected' : ''}>Planificando</option>
                                    <option value="EN_PROCESO" ${p.estado === 'EN_PROCESO' ? 'selected' : ''}>En proceso</option>
                                    <option value="FINALIZANDO" ${p.estado === 'FINALIZANDO' ? 'selected' : ''}>Finalizando</option>
                                    <option value="ACABADO" ${p.estado === 'ACABADO' ? 'selected' : ''}>Acabado</option>
                                </select>
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Descripción</label>
                                <textarea class="campo-textarea" id="edit-descripcion">${p.descripcion || ''}</textarea>
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Empleados</label>
                                <select class="campo-select" id="edit-empleados" multiple style="height:90px;">
                                    ${opcionesEmpleados()}
                                </select>
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Clientes</label>
                                <select class="campo-select" id="edit-clientes" multiple style="height:90px;">
                                    ${opcionesClientes()}
                                </select>
                            </div>
                            <div id="edit-error" class="mensaje-error oculto"></div>
                            <div class="modal-acciones">
                                <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                                <button class="btn-primario" onclick="ProyectosPanel.actualizar(${id})">Guardar</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const abrirModalCliente = () => {
        document.getElementById('modal-proyectos').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Nuevo cliente</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div class="campo-grupo"><label class="campo-etiqueta">Nombre *</label><input class="campo-input" id="cli-nombre" type="text"></div>
                        <div class="campo-grupo"><label class="campo-etiqueta">Empresa</label><input class="campo-input" id="cli-empresa" type="text"></div>
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                            <div class="campo-grupo"><label class="campo-etiqueta">Email</label><input class="campo-input" id="cli-email" type="email"></div>
                            <div class="campo-grupo"><label class="campo-etiqueta">Teléfono</label><input class="campo-input" id="cli-telefono" type="tel"></div>
                        </div>
                        <div id="cli-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="ProyectosPanel.crearCliente()">Crear cliente</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const seleccionMultiple = (id) =>
        Array.from(document.getElementById(id)?.selectedOptions || []).map(o => parseInt(o.value));

    const crear = async () => {
        const errEl = document.getElementById('proy-error');
        errEl.classList.add('oculto');
        try {
            await API.post('/proyectos', {
                nombre: document.getElementById('proy-nombre').value.trim(),
                descripcion: document.getElementById('proy-descripcion').value || null,
                idsEmpleados: seleccionMultiple('proy-empleados'),
                idsClientes: seleccionMultiple('proy-clientes'),
                fechaInicio: document.getElementById('proy-fecha').value || null
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Proyecto creado correctamente', 'exito');
            await renderizar();
        } catch (e) { errEl.textContent = e.message; errEl.classList.remove('oculto'); }
    };

    const actualizar = async (id) => {
        const errEl = document.getElementById('edit-error');
        errEl.classList.add('oculto');
        try {
            await API.put(`/proyectos/${id}`, {
                nombre: document.getElementById('edit-nombre').value.trim(),
                descripcion: document.getElementById('edit-descripcion').value || null,
                estado: document.getElementById('edit-estado').value,
                idsEmpleados: seleccionMultiple('edit-empleados'),
                idsClientes: seleccionMultiple('edit-clientes')
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Proyecto actualizado', 'exito');
            await renderizar();
        } catch (e) { errEl.textContent = e.message; errEl.classList.remove('oculto'); }
    };

    const finalizar = async (id, nombre) => {
        if (!confirm(`¿Marcar "${nombre}" como acabado?`)) return;
        try {
            await API.patch(`/proyectos/${id}/finalizar`);
            Aplicacion.mostrarToast('Proyecto finalizado', 'exito');
            await renderizar();
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const eliminar = async (id, nombre) => {
        if (!confirm(`¿Eliminar el proyecto "${nombre}"?`)) return;
        try {
            await API.del(`/proyectos/${id}`);
            Aplicacion.mostrarToast('Proyecto eliminado', 'exito');
            await renderizar();
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const crearCliente = async () => {
        const errEl = document.getElementById('cli-error');
        errEl.classList.add('oculto');
        try {
            await API.post('/proyectos/clientes', {
                nombre: document.getElementById('cli-nombre').value.trim(),
                empresa: document.getElementById('cli-empresa').value || null,
                email: document.getElementById('cli-email').value || null,
                telefono: document.getElementById('cli-telefono').value || null
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Cliente creado correctamente', 'exito');
            await renderizar();
        } catch (e) { errEl.textContent = e.message; errEl.classList.remove('oculto'); }
    };

    return { renderizar, abrirModalCrear, abrirModalEditar, abrirModalCliente, crear, actualizar, finalizar, eliminar, crearCliente };
})();
