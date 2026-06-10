const Login = (() => {
    const inicializar = () => {
        document.getElementById('form-login').addEventListener('submit', manejarLogin);
        document.getElementById('login-contrasena').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') manejarLogin(e);
        });
    };

    const manejarLogin = async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value.trim();
        const contrasena = document.getElementById('login-contrasena').value;
        const errorEl = document.getElementById('login-error');
        const btnTexto = document.querySelector('#btn-login .btn-texto');
        const btnCarga = document.querySelector('#btn-login .btn-carga');

        errorEl.classList.add('oculto');

        if (!email || !contrasena) {
            errorEl.textContent = 'Introduce tu email y contraseña';
            errorEl.classList.remove('oculto');
            return;
        }

        btnTexto.classList.add('oculto');
        btnCarga.classList.remove('oculto');
        document.getElementById('btn-login').disabled = true;

        try {
            const datos = await API.post('/auth/login', { email, contrasena });
            localStorage.setItem('localytics_token', datos.token);
            localStorage.setItem('localytics_usuario', JSON.stringify(datos));
            Aplicacion.iniciarApp(datos);
        } catch (error) {
            errorEl.textContent = 'Credenciales incorrectas. Inténtalo de nuevo.';
            errorEl.classList.remove('oculto');
        } finally {
            btnTexto.classList.remove('oculto');
            btnCarga.classList.add('oculto');
            document.getElementById('btn-login').disabled = false;
        }
    };

    return { inicializar };
})();