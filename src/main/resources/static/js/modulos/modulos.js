const ModulosPanel = (() => {

    const iconosPorClave = {
        FINANZAS: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>`,
        INVENTARIO: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>`,
        PROYECTOS: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/></svg>`,
    };

    const renderizar = async (usuario) => {
        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';

        try {
            const modulos = await API.get('/modulos');
            const soloOpcionales = modulos.filter(m => !m.obligatorio);

            area.innerHTML = `
                <div class="tarjeta-cabecera" style="margin-bottom: 20px;">
                    <h2 class="tarjeta-titulo">Gestión de Módulos</h2>
                    <p style="color: var(--color-texto-secundario); font-size: 13px;">Activa o desactiva los módulos disponibles</p>
                </div>
                <div class="cuadricula-modulos" id="cuadricula-modulos"></div>
            `;

            const cuadricula = document.getElementById('cuadricula-modulos');
            soloOpcionales.forEach(modulo => {
                const esAdmin = usuario.rol === 'ADMIN';
                cuadricula.insertAdjacentHTML('beforeend', construirTarjetaModulo(modulo, esAdmin));
            });

            if (esAdmin(usuario)) {
                cuadricula.querySelectorAll('.toggle:not(.obligatorio)').forEach(toggle => {
                    toggle.addEventListener('click', () => alternarModulo(toggle));
                });
            }
        } catch (error) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar los módulos: ${error.message}</p>`;
        }
    };

    const construirTarjetaModulo = (modulo, esAdmin) => {
        const icono = iconosPorClave[modulo.clave] || `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>`;
        return `
            <div class="tarjeta-modulo">
                <div class="modulo-cabecera">
                    <div class="modulo-icono-wrap">${icono}</div>
                    <div class="modulo-info">
                        <div class="modulo-nombre">${modulo.nombre}</div>
                        <div class="modulo-descripcion">${modulo.descripcion || ''}</div>
                    </div>
                </div>
                <div class="modulo-pie">
                    <span style="font-size:12px; color: var(--color-texto-tenue);">${modulo.activo ? 'Activo' : 'Inactivo'}</span>
                    ${esAdmin ? `
                        <div class="toggle-wrap">
                            <div class="toggle ${modulo.activo ? 'activo' : ''}" 
                                 data-id="${modulo.id}" 
                                 data-clave="${modulo.clave}"
                                 data-activo="${modulo.activo}">
                            </div>
                        </div>
                    ` : `<span class="badge ${modulo.activo ? 'badge-exito' : ''}" style="font-size:11px;">${modulo.activo ? '✓ Activado' : '— Sin acceso'}</span>`}
                </div>
            </div>
        `;
    };

    const alternarModulo = async (toggle) => {
        const id = toggle.dataset.id;
        const activo = toggle.dataset.activo === 'true';

        toggle.style.pointerEvents = 'none';
        toggle.style.opacity = '0.6';

        try {
            if (activo) {
                await API.del(`/modulos/${id}/desactivar`);
                toggle.classList.remove('activo');
                toggle.dataset.activo = 'false';
                toggle.previousElementSibling?.textContent && (toggle.previousElementSibling.textContent = 'Inactivo');
                Aplicacion.mostrarToast('Módulo desactivado', 'info');
            } else {
                await API.post(`/modulos/${id}/activar`);
                toggle.classList.add('activo');
                toggle.dataset.activo = 'true';
                Aplicacion.mostrarToast('Módulo activado', 'exito');
            }

            await Aplicacion.actualizarSidebar();
        } catch (error) {
            Aplicacion.mostrarToast('Error: ' + error.message, 'error');
        } finally {
            toggle.style.pointerEvents = '';
            toggle.style.opacity = '';
        }
    };

    const esAdmin = (usuario) => usuario && usuario.rol === 'ADMIN';

    return { renderizar };
})();