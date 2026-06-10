const API = (() => {
    const BASE = '/api';

    const obtenerToken = () => localStorage.getItem('localytics_token');

    const cabeceras = () => ({
        'Content-Type': 'application/json',
        ...(obtenerToken() ? { 'Authorization': `Bearer ${obtenerToken()}` } : {})
    });

    const manejarRespuesta = async (res) => {
        if (res.status === 401) {
            localStorage.removeItem('localytics_token');
            localStorage.removeItem('localytics_usuario');
            window.location.reload();
            return;
        }
        const datos = await res.json();
        if (!datos.exito) throw new Error(datos.error || 'Error desconocido');
        return datos.datos;
    };

    const get = async (ruta) => {
        const res = await fetch(BASE + ruta, { headers: cabeceras() });
        return manejarRespuesta(res);
    };

    const post = async (ruta, cuerpo) => {
        const res = await fetch(BASE + ruta, {
            method: 'POST',
            headers: cabeceras(),
            body: JSON.stringify(cuerpo)
        });
        return manejarRespuesta(res);
    };

    const put = async (ruta, cuerpo) => {
        const res = await fetch(BASE + ruta, {
            method: 'PUT',
            headers: cabeceras(),
            body: JSON.stringify(cuerpo)
        });
        return manejarRespuesta(res);
    };

    const patch = async (ruta, cuerpo = {}) => {
        const res = await fetch(BASE + ruta, {
            method: 'PATCH',
            headers: cabeceras(),
            body: JSON.stringify(cuerpo)
        });
        return manejarRespuesta(res);
    };

    const del = async (ruta) => {
        const res = await fetch(BASE + ruta, { method: 'DELETE', headers: cabeceras() });
        return manejarRespuesta(res);
    };

    const descargar = async (ruta, nombreArchivo) => {
        const res = await fetch(BASE + ruta, { headers: cabeceras() });
        if (!res.ok) throw new Error('Error al descargar el archivo');
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = nombreArchivo;
        enlace.click();
        URL.revokeObjectURL(url);
    };

    return { get, post, put, patch, del, descargar };
})();