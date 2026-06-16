const DocumentosPanel = (() => {

    const CATEGORIAS = ['CONTRATO','FACTURA','INFORME','MANUAL','PRESENTACION','LEGAL','RRHH','TECNICO','OTRO'];

    const renderizar = async () => {
        const area = document.getElementById('area-modulo');
        area.innerHTML = '<div class="modulo-carga"><svg class="girando" viewBox="0 0 24 24" width="32" height="32"><circle cx="12" cy="12" r="10" stroke="#6366f1" stroke-width="2" fill="none" stroke-dasharray="30 70"/></svg></div>';
        try {
            const documentos = await API.get('/documentos');
            renderizarContenido(area, documentos);
        } catch (e) {
            area.innerHTML = `<p class="mensaje-error">Error al cargar documentos: ${e.message}</p>`;
        }
    };

    const renderizarContenido = (area, documentos) => {
        const categorias = ['TODAS', ...CATEGORIAS];

        area.innerHTML = `
            <div class="barra-acciones">
                <h2 class="tarjeta-titulo">Gestión Documental</h2>
                <button class="btn-primario" onclick="DocumentosPanel.abrirModalSubir()">
                    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                    Subir documento
                </button>
            </div>

            <div style="display:flex;gap:8px;flex-wrap:wrap;margin:16px 0;">
                ${categorias.map(c => `
                    <button class="btn-categoria ${c === 'TODAS' ? 'activo' : ''}" 
                            data-categoria="${c}"
                            onclick="DocumentosPanel.filtrarCategoria('${c}', this)"
                            style="padding:5px 14px;border-radius:20px;font-size:12px;font-weight:500;cursor:pointer;border:1px solid var(--color-borde);background:${c === 'TODAS' ? 'var(--color-primario)' : 'var(--color-superficie-2)'};color:${c === 'TODAS' ? 'white' : 'var(--color-texto-secundario)'};">
                        ${c === 'TODAS' ? 'Todas' : c.charAt(0) + c.slice(1).toLowerCase()}
                    </button>
                `).join('')}
            </div>

            <div id="lista-documentos">
                ${construirListaDocumentos(documentos)}
            </div>
            <div id="modal-documentos"></div>
        `;

        window._documentosCache = documentos;
    };

    const construirListaDocumentos = (documentos) => {
        if (documentos.length === 0) {
            return '<div class="tarjeta" style="text-align:center;color:var(--color-texto-tenue);padding:48px;">No hay documentos. Sube el primero.</div>';
        }
        return `
            <div class="tarjeta">
                <div class="contenedor-tabla">
                    <table>
                        <thead><tr>
                            <th>Nombre</th><th>Categoría</th><th>Versiones</th><th>Último archivo</th><th>Subido por</th><th>Fecha</th><th>Acciones</th>
                        </tr></thead>
                        <tbody>
                            ${documentos.map(d => construirFilaDocumento(d)).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    };

    const construirFilaDocumento = (d) => {
        const fecha = d.actualizadoEn ? new Date(d.actualizadoEn).toLocaleDateString('es-ES') : '—';
        const tamanio = d.ultimaVersion ? formatearTamanio(d.ultimaVersion.tamanioBytes) : '—';
        return `
            <tr>
                <td>
                    <div style="font-weight:500;">${d.nombre}</div>
                    ${d.descripcion ? `<div style="font-size:12px;color:var(--color-texto-tenue);">${d.descripcion}</div>` : ''}
                </td>
                <td><span class="badge" style="background:var(--color-primario-suave);color:var(--color-primario);font-size:11px;">${d.categoria}</span></td>
                <td style="text-align:center;">
                    <span style="font-weight:600;color:var(--color-primario);">v${d.totalVersiones}</span>
                </td>
                <td>
                    ${d.ultimaVersion
                        ? `<div style="font-size:13px;">${d.ultimaVersion.nombreArchivo}</div>
                           <div style="font-size:11px;color:var(--color-texto-tenue);">${tamanio}</div>`
                        : '—'}
                </td>
                <td style="font-size:13px;">${d.creadoPor}</td>
                <td style="font-size:13px;">${fecha}</td>
                <td>
                    <div style="display:flex;gap:6px;">
                        ${d.ultimaVersion ? `
                            <button class="btn-icono" title="Descargar última versión" onclick="DocumentosPanel.descargar(${d.ultimaVersion.id}, '${escapar(d.ultimaVersion.nombreArchivo)}')">
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                            </button>
                        ` : ''}
                        <button class="btn-icono" title="Ver versiones / subir nueva" onclick="DocumentosPanel.abrirDetalle(${d.id})">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                        </button>
                        <button class="btn-peligro" title="Eliminar" onclick="DocumentosPanel.eliminar(${d.id}, '${escapar(d.nombre)}')">
                            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/></svg>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    };

    const filtrarCategoria = async (categoria, boton) => {
        document.querySelectorAll('.btn-categoria').forEach(b => {
            b.style.background = 'var(--color-superficie-2)';
            b.style.color = 'var(--color-texto-secundario)';
        });
        boton.style.background = 'var(--color-primario)';
        boton.style.color = 'white';

        try {
            const docs = categoria === 'TODAS'
                ? await API.get('/documentos')
                : await API.get(`/documentos/categoria/${categoria}`);
            document.getElementById('lista-documentos').innerHTML = construirListaDocumentos(docs);
        } catch (e) { Aplicacion.mostrarToast('Error al filtrar: ' + e.message, 'error'); }
    };

    const abrirModalSubir = () => {
        document.getElementById('modal-documentos').innerHTML = `
            <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                <div class="modal">
                    <div class="modal-cabecera">
                        <h3 class="modal-titulo">Subir documento</h3>
                        <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                    </div>
                    <div class="modal-form">
                        <div class="campo-grupo"><label class="campo-etiqueta">Nombre *</label><input class="campo-input" id="doc-nombre" type="text" placeholder="Nombre descriptivo del documento"></div>
                        <div class="campo-grupo"><label class="campo-etiqueta">Descripción</label><input class="campo-input" id="doc-descripcion" type="text" placeholder="Descripción opcional"></div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Categoría *</label>
                            <select class="campo-select" id="doc-categoria">
                                ${CATEGORIAS.map(c => `<option value="${c}">${c.charAt(0)+c.slice(1).toLowerCase()}</option>`).join('')}
                            </select>
                        </div>
                        <div class="campo-grupo">
                            <label class="campo-etiqueta">Archivo *</label>
                            <input class="campo-input" id="doc-archivo" type="file">
                        </div>
                        <div class="campo-grupo"><label class="campo-etiqueta">Comentario</label><input class="campo-input" id="doc-comentario" type="text" placeholder="Comentario de la versión inicial"></div>
                        <div id="doc-error" class="mensaje-error oculto"></div>
                        <div class="modal-acciones">
                            <button class="btn-secundario" onclick="this.closest('.modal-fondo').remove()">Cancelar</button>
                            <button class="btn-primario" id="btn-subir-doc" onclick="DocumentosPanel.subir()">Subir</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    };

    const abrirDetalle = async (id) => {
        try {
            const doc = await API.get(`/documentos/${id}`);
            document.getElementById('modal-documentos').innerHTML = `
                <div class="modal-fondo" onclick="if(event.target===this) this.remove()">
                    <div class="modal" style="width:560px;">
                        <div class="modal-cabecera">
                            <h3 class="modal-titulo">${doc.nombre}</h3>
                            <button class="btn-icono" onclick="this.closest('.modal-fondo').remove()">✕</button>
                        </div>
                        <p style="font-size:13px;color:var(--color-texto-secundario);margin-bottom:16px;">${doc.descripcion || ''}</p>

                        <div style="background:var(--color-superficie-2);border:1px solid var(--color-borde);border-radius:8px;padding:14px;margin-bottom:16px;">
                            <p style="font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:.5px;color:var(--color-texto-tenue);margin-bottom:10px;">Subir nueva versión</p>
                            <div style="display:flex;gap:10px;align-items:flex-end;">
                                <div class="campo-grupo" style="flex:1;"><label class="campo-etiqueta">Archivo</label><input class="campo-input" id="nv-archivo" type="file"></div>
                                <div class="campo-grupo" style="flex:1;"><label class="campo-etiqueta">Comentario</label><input class="campo-input" id="nv-comentario" type="text" placeholder="Cambios en esta versión"></div>
                                <button class="btn-primario" style="padding:10px 14px;flex-shrink:0;" onclick="DocumentosPanel.subirVersion(${doc.id})">
                                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                                </button>
                            </div>
                            <div id="nv-error" class="mensaje-error oculto" style="margin-top:8px;"></div>
                        </div>

                        <p style="font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:.5px;color:var(--color-texto-tenue);margin-bottom:8px;">Historial (${doc.versiones?.length || 0} versiones)</p>
                        <div style="max-height:260px;overflow-y:auto;">
                            ${(doc.versiones || []).map(v => `
                                <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;border-bottom:1px solid var(--color-borde);">
                                    <div>
                                        <div style="display:flex;align-items:center;gap:8px;">
                                            <span style="background:var(--color-primario-suave);color:var(--color-primario);padding:2px 8px;border-radius:10px;font-size:11px;font-weight:700;">v${v.numeroVersion}</span>
                                            <span style="font-size:13px;font-weight:500;">${v.nombreArchivo}</span>
                                        </div>
                                        <div style="font-size:11px;color:var(--color-texto-tenue);margin-top:3px;">
                                            ${formatearTamanio(v.tamanioBytes)} · ${v.subidoPor} · ${new Date(v.subidoEn).toLocaleString('es-ES')}
                                            ${v.comentario ? ` · <em>${v.comentario}</em>` : ''}
                                        </div>
                                    </div>
                                    <button class="btn-icono" title="Descargar" onclick="DocumentosPanel.descargar(${v.id}, '${escapar(v.nombreArchivo)}')">
                                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                                    </button>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
            `;
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const subir = async () => {
        const errEl = document.getElementById('doc-error');
        errEl.classList.add('oculto');
        const archivo = document.getElementById('doc-archivo').files[0];
        if (!archivo) { errEl.textContent = 'Selecciona un archivo'; errEl.classList.remove('oculto'); return; }

        const formData = new FormData();
        formData.append('nombre', document.getElementById('doc-nombre').value.trim());
        formData.append('descripcion', document.getElementById('doc-descripcion').value.trim());
        formData.append('categoria', document.getElementById('doc-categoria').value);
        formData.append('archivo', archivo);
        formData.append('comentario', document.getElementById('doc-comentario').value.trim());

        const btn = document.getElementById('btn-subir-doc');
        btn.disabled = true; btn.textContent = 'Subiendo...';

        try {
            const res = await fetch('/api/documentos', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${localStorage.getItem('localytics_token')}` },
                body: formData
            });
            const datos = await res.json();
            if (!datos.exito) throw new Error(datos.error);
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Documento subido correctamente', 'exito');
            await renderizar();
        } catch (e) {
            errEl.textContent = e.message;
            errEl.classList.remove('oculto');
            btn.disabled = false; btn.textContent = 'Subir';
        }
    };

    const subirVersion = async (documentoId) => {
        const errEl = document.getElementById('nv-error');
        errEl.classList.add('oculto');
        const archivo = document.getElementById('nv-archivo').files[0];
        if (!archivo) { errEl.textContent = 'Selecciona un archivo'; errEl.classList.remove('oculto'); return; }

        const formData = new FormData();
        formData.append('archivo', archivo);
        formData.append('comentario', document.getElementById('nv-comentario').value.trim());

        try {
            const res = await fetch(`/api/documentos/${documentoId}/versiones`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${localStorage.getItem('localytics_token')}` },
                body: formData
            });
            const datos = await res.json();
            if (!datos.exito) throw new Error(datos.error);
            document.querySelector('.modal-fondo').remove();
            Aplicacion.mostrarToast('Nueva versión subida', 'exito');
            await renderizar();
        } catch (e) { errEl.textContent = e.message; errEl.classList.remove('oculto'); }
    };

    const descargar = async (versionId, nombreArchivo) => {
        try {
            const res = await fetch(`/api/documentos/versiones/${versionId}/descargar`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('localytics_token')}` }
            });
            if (!res.ok) throw new Error('Error al descargar');
            const blob = await res.blob();
            const url = URL.createObjectURL(blob);
            const enlace = document.createElement('a');
            enlace.href = url; enlace.download = nombreArchivo; enlace.click();
            URL.revokeObjectURL(url);
        } catch (e) { Aplicacion.mostrarToast('Error al descargar: ' + e.message, 'error'); }
    };

    const eliminar = async (id, nombre) => {
        if (!confirm(`¿Eliminar el documento "${nombre}"?`)) return;
        try {
            await API.del(`/documentos/${id}`);
            Aplicacion.mostrarToast('Documento eliminado', 'exito');
            await renderizar();
        } catch (e) { Aplicacion.mostrarToast('Error: ' + e.message, 'error'); }
    };

    const formatearTamanio = (bytes) => {
        if (!bytes) return '—';
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / 1048576).toFixed(1) + ' MB';
    };

    const escapar = (s) => String(s).replace(/'/g, "\\'");

    return { renderizar, filtrarCategoria, abrirModalSubir, abrirDetalle, subir, subirVersion, descargar, eliminar };
})();