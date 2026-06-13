const Aplicacion = (() => {

    let usuarioActual = null;
    let moduloActivo = null;

    const ICONOS = {
        MODULOS:    `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>`,
        USUARIOS:   `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
        GRUPOS:     `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
        FICHAJES: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
    };

    const ICONO_GENERICO = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>`;

    const RENDERIZADORES = {
        MODULOS:  (usuario) => ModulosPanel.renderizar(usuario),
        USUARIOS: (usuario) => UsuariosPanel.renderizar(usuario),
        GRUPOS:   ()        => GruposPanel.renderizar(),
        FICHAJES: (usuario) => FichajesPanel.renderizar(usuario),
    };

    const MODULOS_FIJOS_ADMIN   = ['MODULOS', 'GRUPOS'];
    const MODULOS_FIJOS_SIEMPRE = ['USUARIOS', 'FICHAJES'];

    const inicializar = () => {
        Login.inicializar();
        crearContenedorToast();

        const tokenGuardado   = localStorage.getItem('localytics_token');
        const usuarioGuardado = localStorage.getItem('localytics_usuario');

        if (tokenGuardado && usuarioGuardado) {
            try {
                iniciarApp(JSON.parse(usuarioGuardado));
            } catch {
                mostrarLogin();
            }
        } else {
            mostrarLogin();
        }
    };

    const mostrarLogin = () => {
        document.getElementById('pantalla-login').classList.remove('oculto');
        document.getElementById('app-principal').classList.add('oculto');
    };

    const iniciarApp = async (usuario) => {
        usuarioActual = usuario;

        document.getElementById('pantalla-login').classList.add('oculto');
        document.getElementById('app-principal').classList.remove('oculto');

        document.getElementById('nombre-usuario').textContent = usuario.nombre;
        document.getElementById('avatar-usuario').textContent = usuario.nombre.charAt(0).toUpperCase();

        await actualizarSidebar();
        navegarA('MODULOS');
    };

    const actualizarSidebar = async () => {
        if (!usuarioActual) return;

        const esAdmin = usuarioActual.rol === 'ADMIN';

        let todosLosModulos = [];
        let clavesActivas   = [];
        try {
            todosLosModulos = await API.get('/modulos');
            clavesActivas   = await API.get('/modulos/activos');
        } catch {
            clavesActivas = [];
        }

        const mapaModulos = {};
        todosLosModulos.forEach(m => { mapaModulos[m.clave] = m.nombre; });

        const navFijos = document.getElementById('nav-modulos-fijos');
        navFijos.innerHTML = '';

        MODULOS_FIJOS_ADMIN.forEach(clave => {
            if (esAdmin) {
                navFijos.insertAdjacentHTML('beforeend',
                    construirNavItem(clave, mapaModulos[clave] || clave));
            }
        });

        MODULOS_FIJOS_SIEMPRE.forEach(clave => {
            navFijos.insertAdjacentHTML('beforeend',
                construirNavItem(clave, mapaModulos[clave] || clave));
        });

        const navActivados  = document.getElementById('nav-modulos-activados');
        const tituloModulos = document.getElementById('titulo-modulos-activados');
        navActivados.innerHTML = '';

        const modulosMostrar = todosLosModulos.filter(m => {
            if (m.obligatorio) return false;
            if (!clavesActivas.includes(m.clave)) return false;
            if (esAdmin) return true;

            return true;
        });

        if (modulosMostrar.length > 0) {
            tituloModulos.style.display = '';
            modulosMostrar.forEach(m => {
                navActivados.insertAdjacentHTML('beforeend',
                    construirNavItem(m.clave, m.nombre));
            });
        } else {
            tituloModulos.style.display = 'none';
        }

        if (moduloActivo) marcarNavActivo(moduloActivo);
    };

    const construirNavItem = (clave, nombre) => `
        <div class="nav-item" data-clave="${clave}" onclick="Aplicacion.navegarA('${clave}')">
            ${ICONOS[clave] || ICONO_GENERICO}
            <span>${nombre}</span>
        </div>
    `;

    const marcarNavActivo = (clave) => {
        document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('activo'));
        const el = document.querySelector(`.nav-item[data-clave="${clave}"]`);
        if (el) el.classList.add('activo');
    };

    const navegarA = async (clave) => {
        moduloActivo = clave;
        marcarNavActivo(clave);

        const itemNav = document.querySelector(`.nav-item[data-clave="${clave}"] span`);
        document.getElementById('topbar-titulo').textContent =
            itemNav ? itemNav.textContent : clave;

        const renderizador = RENDERIZADORES[clave];
        if (renderizador) {
            await renderizador(usuarioActual);
        } else {
            document.getElementById('area-modulo').innerHTML =
                `<p style="color:var(--color-texto-tenue); padding:32px;">
                    Módulo "${clave}" no tiene renderizador registrado.
                 </p>`;
        }
    };

    const cerrarSesion = () => {
        if (!confirm('¿Cerrar sesión?')) return;
        localStorage.removeItem('localytics_token');
        localStorage.removeItem('localytics_usuario');
        usuarioActual = null;
        moduloActivo  = null;
        mostrarLogin();
        document.getElementById('login-email').value = '';
        document.getElementById('login-contrasena').value = '';
    };

    const crearContenedorToast = () => {
        if (!document.getElementById('contenedor-toast')) {
            const cont = document.createElement('div');
            cont.id = 'contenedor-toast';
            document.body.appendChild(cont);
        }
    };

    const mostrarToast = (mensaje, tipo = 'info') => {
        const cont = document.getElementById('contenedor-toast');
        const iconos = { exito: '✓', error: '✕', info: 'ℹ' };
        const toast = document.createElement('div');
        toast.className = `toast ${tipo}`;
        toast.innerHTML = `<span style="font-weight:700;">${iconos[tipo] || 'ℹ'}</span> ${mensaje}`;
        cont.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3200);
    };

    return { inicializar, iniciarApp, navegarA, actualizarSidebar, mostrarToast };
})();

function cerrarSesion() {
    localStorage.removeItem('localytics_token');
    localStorage.removeItem('localytics_usuario');
    window.location.reload();
}

document.addEventListener('DOMContentLoaded', () => Aplicacion.inicializar());