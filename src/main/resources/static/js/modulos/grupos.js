const GruposPanel = (() => {

    let todosLosUsuarios = [];
    let todosLosModulos = [];

    const renderizar = async () => {
        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';

        try {
            const [grupos, usuarios, modulos] = await Promise.all([
                API.get('/grupos'),
                API.get('/usuarios'),
                API.get('/modulos')
            ]);
            todosLosUsuarios = usuarios;
            todosLosModulos = modulos.filter(m => !m.obligatorio);
            renderizarContenido(area, grupos);
        } catch (e) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar grupos: ${e.message}</p>`;
        }
    };

    const renderizarContenido = (area, grupos) => {
        area.innerHTML = `
            <div class="barra-acciones">
                <h2 class="tarjeta-titulo">Gestión de Grupos</h2>
                <button class="btn-primario" onclick="GruposPanel.abrirModalCrear()">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5">
                        <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                    </svg>
                    Nuevo grupo
                </button>
            </div>
            <div style="display:grid; grid-template-columns:repeat(auto-fill,minmax(360px,1fr)); gap:16px; margin-top:16px;">
                ${grupos.length === 0
                    ? '<div class="tarjeta" style="text-align:center;color:var(--color-texto-tenue);padding:48px;grid-column:1/-1;">No hay grupos creados.</div>'
                    : grupos.map(g => construirTarjetaGrupo(g)).join('')}
            </div>
            <div id="modal-grupos"></div>
        `;
    };

    const construirTarjetaGrupo = (g) => `
        <div class="tarjeta">
            <div class="tarjeta-cabecera">
                <div>
                    <div class="tarjeta-titulo">${g.nombre}</div>
                    <div style="font-size:13px;color:var(--color-texto-secundario);margin-top:2px;">
                        ${g.descripcion || 'Sin descripción'}
                    </div>
                </div>
                <div style="display:flex;gap:6px;">
                    <button class="btn-icono" title="Gestionar módulos" onclick="GruposPanel.abrirModalModulos(${g.id}, '${escapar(g.nombre)}', ${JSON.stringify((g.modulos||[]).map(m=>m.id))})">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                            <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
                        </svg>
                    </button>
                    <button class="btn-icono" title="Añadir miembro" onclick="GruposPanel.abrirModalAsignar(${g.id}, '${escapar(g.nombre)}')">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                            <circle cx="9" cy="7" r="4"/>
                            <line x1="19" y1="8" x2="19" y2="14"/><line x1="16" y1="11" x2="22" y2="11"/>
                        </svg>
                    </button>
                    <button class="btn-peligro" title="Eliminar grupo" onclick="GruposPanel.eliminarGrupo(${g.id}, '${escapar(g.nombre)}')">
                        <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/>
                        </svg>
                    </button>
                </div>
            </div>

            <div style="margin-top:12px;padding-bottom:12px;border-bottom:1px solid var(--color-borde);">
                <p style="font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:.5px;color:var(--color-texto-tenue);margin-bottom:6px;">
                    Módulos con acceso
                </p>
                <div style="display:flex;flex-wrap:wrap;gap:6px;">
                    ${(g.modulos && g.modulos.length > 0)
                        ? g.modulos.map(m => `
                            <span style="background:var(--color-primario-suave);color:var(--color-primario);
                                         padding:3px 10px;border-radius:20px;font-size:11px;font-weight:600;">
                                ${m.nombre}
                            </span>`).join('')
                        : '<span style="font-size:12px;color:var(--color-texto-tenue);">Sin módulos asignados</span>'}
                </div>
            </div>

            <div style="margin-top:12px;">
                <p style="font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:.5px;color:var(--color-texto-tenue);margin-bottom:8px;">
                    Miembros (${(g.miembros || []).length})
                </p>
                ${(g.miembros && g.miembros.length > 0)
                    ? g.miembros.map(m => `
                        <div style="display:flex;align-items:center;justify-content:space-between;
                                    padding:7px 0;border-bottom:1px solid var(--color-borde);">
                            <div style="display:flex;align-items:center;gap:8px;">
                                <div class="avatar-tabla" style="width:28px;height:28px;font-size:11px;">
                                    ${m.nombreCompleto.charAt(0).toUpperCase()}
                                </div>
                                <div>
                                    <div style="font-size:13px;font-weight:500;">${m.nombreCompleto}</div>
                                    <div style="font-size:11px;color:var(--color-texto-tenue);">${m.email}</div>
                                </div>
                            </div>
                            <button class="btn-icono" title="Quitar del grupo"
                                onclick="GruposPanel.quitarMiembro(${g.id}, ${m.idUsuario}, '${escapar(m.nombreCompleto)}')">
                                <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
                                    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                                </svg>
                            </button>
                        </div>`).join('')
                    : '<p style="font-size:12px;color:var(--color-texto-tenue);">Sin miembros asignados</p>'}
            </div>
        </div>
    `;

    const abrirModalCrear = () => {
        document.getElementById('modal-grupos').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal" style="width:540px;">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Nuevo grupo</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">

                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Nombre del grupo *</label>
                            <input class="campo-input" id="ng-nombre" type="text" placeholder="Ej: GRUPO_VENTAS">
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Descripción</label>
                            <input class="campo-input" id="ng-descripcion" type="text" placeholder="Descripción del grupo">
                        </div>

                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Módulos que puede gestionar</label>
                            <div style="background:var(--color-superficie-2);border:1px solid var(--color-borde);
                                        border-radius:8px;padding:12px;display:flex;flex-wrap:wrap;gap:10px;">
                                ${todosLosModulos.length === 0
                                    ? '<p style="font-size:12px;color:var(--color-texto-tenue);">No hay módulos opcionales disponibles</p>'
                                    : todosLosModulos.map(m => `
                                        <label style="display:flex;align-items:center;gap:6px;cursor:pointer;font-size:13px;">
                                            <input type="checkbox" class="ng-modulo-check" value="${m.id}"
                                                   style="accent-color:var(--color-primario);width:15px;height:15px;">
                                            ${m.nombre}
                                        </label>`).join('')}
                            </div>
                            <small style="color:var(--color-texto-tenue);">Los usuarios de este grupo podrán acceder a los módulos marcados</small>
                        </div>

                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Miembros iniciales</label>
                            <select class="campo-select" id="ng-usuarios" multiple style="height:110px;">
                                ${todosLosUsuarios.map(u => `
                                    <option value="${u.id}">${u.nombreCompleto} — ${u.email}</option>`).join('')}
                            </select>
                            <small style="color:var(--color-texto-tenue);">Mantén Ctrl para seleccionar varios</small>
                        </div>

                        <div id="ng-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="GruposPanel.crear()">Crear grupo</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const abrirModalModulos = (grupoId, nombreGrupo, idsActuales) => {
        document.getElementById('modal-grupos').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Módulos de "${nombreGrupo}"</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <p style="font-size:13px;color:var(--color-texto-secundario);margin-bottom:4px;">
                            Selecciona los módulos a los que tendrá acceso este grupo.
                        </p>
                        <div style="background:var(--color-superficie-2);border:1px solid var(--color-borde);
                                    border-radius:8px;padding:14px;display:flex;flex-direction:column;gap:10px;">
                            ${todosLosModulos.length === 0
                                ? '<p style="font-size:12px;color:var(--color-texto-tenue);">No hay módulos opcionales</p>'
                                : todosLosModulos.map(m => `
                                    <label style="display:flex;align-items:center;gap:10px;cursor:pointer;">
                                        <input type="checkbox" class="em-modulo-check" value="${m.id}"
                                               ${idsActuales.includes(m.id) ? 'checked' : ''}
                                               style="accent-color:var(--color-primario);width:16px;height:16px;">
                                        <div>
                                            <div style="font-size:14px;font-weight:500;">${m.nombre}</div>
                                            <div style="font-size:12px;color:var(--color-texto-tenue);">${m.descripcion || ''}</div>
                                        </div>
                                    </label>`).join('')}
                        </div>
                        <div id="em-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="GruposPanel.guardarModulos(${grupoId})">Guardar</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const abrirModalAsignar = (grupoId, nombreGrupo) => {
        document.getElementById('modal-grupos').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Añadir miembro a "${nombreGrupo}"</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Seleccionar usuario</label>
                            <select class="campo-select" id="asig-usuario">
                                <option value="">— Selecciona un usuario —</option>
                                ${todosLosUsuarios.map(u => `
                                    <option value="${u.id}">${u.nombreCompleto} (${u.email})</option>`).join('')}
                            </select>
                        </div>
                        <div id="asig-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="GruposPanel.asignar(${grupoId})">Añadir</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const crear = async () => {
        const errEl = document.getElementById('ng-error');
        errEl.classList.add('oculto');

        const idsModulos = Array.from(
            document.querySelectorAll('.ng-modulo-check:checked')
        ).map(el => parseInt(el.value));

        const idsUsuarios = Array.from(
            document.getElementById('ng-usuarios')?.selectedOptions || []
        ).map(o => parseInt(o.value));

        try {
            await API.post('/grupos', {
                nombre: document.getElementById('ng-nombre').value.trim(),
                descripcion: document.getElementById('ng-descripcion').value.trim(),
                idsModulos,
                idsUsuarios
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Grupo creado correctamente', 'exito');
            await renderizar();
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const guardarModulos = async (grupoId) => {
        const errEl = document.getElementById('em-error');
        errEl.classList.add('oculto');
        const idsModulos = Array.from(
            document.querySelectorAll('.em-modulo-check:checked')
        ).map(el => parseInt(el.value));

        try {
            await API.put(`/grupos/${grupoId}/modulos`, { idsModulos });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Módulos actualizados', 'exito');
            await renderizar();
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const asignar = async (grupoId) => {
        const errEl = document.getElementById('asig-error');
        errEl.classList.add('oculto');
        const usuarioId = document.getElementById('asig-usuario').value;
        if (!usuarioId) {
            errEl.textContent = 'Selecciona un usuario';
            errEl.classList.remove('oculto');
            return;
        }
        try {
            await API.post(`/grupos/${grupoId}/usuarios/${usuarioId}`);
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Usuario añadido al grupo', 'exito');
            await renderizar();
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const quitarMiembro = async (grupoId, usuarioId, nombre) => {
        if (!confirm(`¿Quitar a "${nombre}" del grupo?`)) return;
        try {
            await API.del(`/grupos/${grupoId}/usuarios/${usuarioId}`);
            Aplicacion.mostrarToast('Usuario eliminado del grupo', 'exito');
            await renderizar();
        } catch (e) {
            Aplicacion.mostrarToast('Error: ' + e.message, 'error');
        }
    };

    const eliminarGrupo = async (id, nombre) => {
        if (!confirm(`¿Eliminar el grupo "${nombre}"? Se eliminarán todas las asignaciones.`)) return;
        try {
            await API.del(`/grupos/${id}`);
            Aplicacion.mostrarToast('Grupo eliminado', 'exito');
            await renderizar();
        } catch (e) {
            Aplicacion.mostrarToast('Error: ' + e.message, 'error');
        }
    };

    const escapar = (s) => String(s).replace(/'/g, "\\'");

    return {
        renderizar,
        abrirModalCrear,
        abrirModalModulos,
        abrirModalAsignar,
        crear,
        guardarModulos,
        asignar,
        quitarMiembro,
        eliminarGrupo
    };
})();