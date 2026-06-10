const UsuariosPanel = (() => {

    let usuarioActual = null;

    const renderizar = async (usuario) => {
        usuarioActual = usuario;
        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';

        try {
            if (usuario.rol === 'ADMIN') {
                await renderizarAdmin(area);
            } else {
                await renderizarPerfil(area, usuario);
            }
        } catch (error) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar usuarios: ${error.message}</p>`;
        }
    };

    const renderizarAdmin = async (area) => {
        const [usuarios, grupos] = await Promise.all([
            API.get('/usuarios'),
            API.get('/usuarios/grupos-disponibles')
        ]);

        window._gruposDisponibles = grupos;
        area.innerHTML = `
            <div class="barra-acciones">
                <h2 class="tarjeta-titulo">Gestión de Usuarios</h2>
                <button class="btn-primario" onclick="UsuariosPanel.abrirModalCrear()">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Nuevo usuario
                </button>
            </div>
            <div class="tarjeta">
                <div class="contenedor-tabla">
                    <table>
                        <thead>
                            <tr>
                                <th>Usuario</th>
                                <th>Email</th>
                                <th>Rol</th>
                                <th>Grupo</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="tabla-usuarios">
                            ${usuarios.map(u => construirFilaUsuario(u)).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
            <div id="modal-usuario"></div>
        `;
    };

    const renderizarPerfil = async (area, usuario) => {
        const datos = await API.get(`/usuarios/${usuario.idUsuario}`);
        area.innerHTML = `
            <div style="max-width: 560px;">
                <h2 class="tarjeta-titulo" style="margin-bottom:20px;">Mi Perfil</h2>
                <div class="tarjeta">
                    <div style="display:flex; align-items:center; gap:16px; margin-bottom:24px; padding-bottom:20px; border-bottom: 1px solid var(--color-borde);">
                        <div class="avatar-usuario" style="width:56px; height:56px; font-size:20px;">${datos.nombre.charAt(0).toUpperCase()}</div>
                        <div>
                            <div style="font-family: var(--fuente-display); font-size:18px; font-weight:700;">${datos.nombreCompleto}</div>
                            <div style="color: var(--color-texto-secundario); font-size:13px;">${datos.email}</div>
                            <span class="badge badge-${datos.rol.toLowerCase()}" style="margin-top:4px;">${datos.rol}</span>
                        </div>
                    </div>
                    <div class="modal-form" id="form-perfil">
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Nombre</label>
                            <input type="text" class="campo-input" id="perfil-nombre" value="${datos.nombre}">
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Apellidos</label>
                            <input type="text" class="campo-input" id="perfil-apellidos" value="${datos.apellidos || ''}">
                        </div>
                        <hr style="border: none; border-top: 1px solid var(--color-borde);">
                        <p style="font-size:13px; font-weight:600; color: var(--color-texto-secundario);">Cambiar contraseña (opcional)</p>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Contraseña actual</label>
                            <input type="password" class="campo-input" id="perfil-contrasena-actual" placeholder="Dejar vacío para no cambiar">
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Nueva contraseña</label>
                            <input type="password" class="campo-input" id="perfil-nueva-contrasena" placeholder="Mínimo 8 caracteres">
                        </div>
                        <div id="perfil-error" class="mensaje-error oculto"></div>
                        <button class="btn-primario" onclick="UsuariosPanel.guardarPerfil(${datos.id})">Guardar cambios</button>
                    </div>
                </div>
            </div>
        `;
    };

    const construirFilaUsuario = (u) => `
        <tr>
            <td>
                <div class="usuario-fila">
                    <div class="avatar-tabla">${u.nombre.charAt(0).toUpperCase()}</div>
                    <div>
                        <div style="font-weight:500;">${u.nombreCompleto}</div>
                        <div style="font-size:12px; color:var(--color-texto-secundario);">${u.email}</div>
                    </div>
                </div>
            </td>
            <td>${u.email}</td>
            <td><span class="badge badge-${u.rol.toLowerCase()}">${u.rol}</span></td>
            <td>${u.grupo ? u.grupo.replace('GRUPO_', '') : '—'}</td>
            <td><span class="badge" style="background:rgba(34,197,94,0.1); color:#86efac;">Activo</span></td>
            <td>
                <div style="display:flex; gap:6px;">
                    <button class="btn-icono" title="Editar perfil" onclick="UsuariosPanel.abrirModalEditar(${u.id})">
                        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>
                    <button class="btn-peligro" title="Eliminar" onclick="UsuariosPanel.eliminar(${u.id}, '${u.nombreCompleto}')">
                        <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                    </button>
                </div>
            </td>
        </tr>
    `;

    const abrirModalCrear = () => {
        const modal = document.getElementById('modal-usuario');
        modal.innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Nuevo usuario</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                            <div class="campo-grupo"><label class="campo-etiqueta">Nombre *</label><input class="campo-input" id="nu-nombre" type="text"></div>
                            <div class="campo-grupo"><label class="campo-etiqueta">Apellidos</label><input class="campo-input" id="nu-apellidos" type="text"></div>
                        </div>
                        <div class="campo-grupo"><label class="campo-etiqueta">Email *</label><input class="campo-input" id="nu-email" type="email"></div>
                        <div class="campo-grupo"><label class="campo-etiqueta">Contraseña *</label><input class="campo-input" id="nu-contrasena" type="password"></div>
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Rol *</label>
                                <select class="campo-select" id="nu-rol">
                                    <option value="ADMIN">Admin</option>
                                    <option value="USUARIO" selected>Usuario</option>
                                </select>
                            </div>
                            <div class="campo-grupo">
                                <label class="campo-etiqueta">Grupo</label>
                                <select class="campo-select" id="nu-grupo">
                                    <option value="">Sin grupo</option>
                                    ${(window._gruposDisponibles || []).map(g => `<option value="${g.id}">${g.nombre}</option>`).join('')}
                                </select>
                            </div>
                        </div>
                        <div id="nu-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" onclick="UsuariosPanel.crear()">Crear usuario</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const abrirModalEditar = async (id) => {
        try {
            const u = await API.get(`/usuarios/${id}`);
            const modal = document.getElementById('modal-usuario');
            modal.innerHTML = `
                <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                    <div class="modal">
                        <div class="modal-cabecera">
                            <h3 class="modal-titulo">Editar perfil</h3>
                            <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                        </div>
                        <div class="modal-form">
                            <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                                <div class="campo-grupo"><label class="campo-etiqueta">Nombre</label><input class="campo-input" id="eu-nombre" value="${u.nombre}"></div>
                                <div class="campo-grupo"><label class="campo-etiqueta">Apellidos</label><input class="campo-input" id="eu-apellidos" value="${u.apellidos || ''}"></div>
                            </div>
                            <div id="eu-error" class="mensaje-error oculto"></div>
                            <div class="modal-acciones">
                                <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                                <button class="btn-primario" onclick="UsuariosPanel.guardarEdicion(${u.id})">Guardar</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const crear = async () => {
        const errEl = document.getElementById('nu-error');
        errEl.classList.add('oculto');
        try {
            await API.post('/usuarios', {
                nombre: document.getElementById('nu-nombre').value,
                apellidos: document.getElementById('nu-apellidos').value,
                email: document.getElementById('nu-email').value,
                contrasena: document.getElementById('nu-contrasena').value,
                rol: document.getElementById('nu-rol').value,
                grupoId: document.getElementById('nu-grupo').value || null
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Usuario creado correctamente', 'exito');
            await renderizar(usuarioActual);
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const guardarEdicion = async (id) => {
        const errEl = document.getElementById('eu-error');
        errEl.classList.add('oculto');
        try {
            await API.put(`/usuarios/${id}/perfil`, {
                nombre: document.getElementById('eu-nombre').value,
                apellidos: document.getElementById('eu-apellidos').value,
            });
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Perfil actualizado', 'exito');
            await renderizar(usuarioActual);
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const guardarPerfil = async (id) => {
        const errEl = document.getElementById('perfil-error');
        errEl.classList.add('oculto');
        const cuerpo = {
            nombre: document.getElementById('perfil-nombre').value,
            apellidos: document.getElementById('perfil-apellidos').value,
        };
        const nuevaPass = document.getElementById('perfil-nueva-contrasena').value;
        if (nuevaPass) {
            cuerpo.nuevaContrasena = nuevaPass;
            cuerpo.contrasenaActual = document.getElementById('perfil-contrasena-actual').value;
        }
        try {
            await API.put(`/usuarios/${id}/perfil`, cuerpo);
            Aplicacion.mostrarToast('Perfil guardado correctamente', 'exito');
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
        }
    };

    const eliminar = async (id, nombre) => {
        if (!confirm(`¿Eliminar al usuario "${nombre}"? Esta acción no se puede deshacer.`)) return;
        try {
            await API.del(`/usuarios/${id}`);
            Aplicacion.mostrarToast('Usuario eliminado', 'exito');
            await renderizar(usuarioActual);
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    return { renderizar, abrirModalCrear, abrirModalEditar, crear, guardarEdicion, guardarPerfil, eliminar };
})();
