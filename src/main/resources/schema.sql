CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS grupos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(150),
    email VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    rol_id VARCHAR(20) NOT NULL DEFAULT 'USUARIO',
    grupo_id BIGINT REFERENCES grupos(id),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS modulos (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    icono VARCHAR(50),
    obligatorio BOOLEAN DEFAULT FALSE,
    orden INTEGER DEFAULT 0,
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS modulos_activos (
    id BIGSERIAL PRIMARY KEY,
    modulo_id BIGINT NOT NULL REFERENCES modulos(id),
    activado_por BIGINT NOT NULL REFERENCES usuarios(id),
    activado_en TIMESTAMP DEFAULT NOW(),
    UNIQUE(modulo_id)
);

CREATE TABLE IF NOT EXISTS grupos_usuarios (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id) ON DELETE CASCADE,
    asignado_en TIMESTAMP DEFAULT NOW(),
    UNIQUE(usuario_id, grupo_id)
);

CREATE TABLE IF NOT EXISTS grupos_modulos (
    id BIGSERIAL PRIMARY KEY,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id) ON DELETE CASCADE,
    modulo_id BIGINT NOT NULL REFERENCES modulos(id) ON DELETE CASCADE,
    UNIQUE(grupo_id, modulo_id)
);

CREATE TABLE IF NOT EXISTS fichajes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    hora_entrada TIMESTAMP,
    hora_salida TIMESTAMP,
    jornada_horas INTEGER NOT NULL DEFAULT 8,
    observaciones VARCHAR(500),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW(),
    UNIQUE(usuario_id, fecha)
);

CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(50),
    empresa VARCHAR(255),
    creado_por BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS proyectos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    estado VARCHAR(20) DEFAULT 'PLANIFICANDO' CHECK (estado IN ('PLANIFICANDO', 'EN_PROCESO', 'FINALIZANDO', 'ACABADO')),
    fecha_inicio DATE DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    descripcion TEXT,
    creado_por BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS proyectos_empleados (
    proyecto_id BIGINT NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    PRIMARY KEY (proyecto_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS proyectos_clientes (
    proyecto_id BIGINT NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    PRIMARY KEY (proyecto_id, cliente_id)
);

CREATE TABLE IF NOT EXISTS stock (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    proveedor VARCHAR(255) NOT NULL,
    cantidad_total INTEGER NOT NULL DEFAULT 0,
    precio NUMERIC(15, 2) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    creado_por BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS documentos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(20) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    creado_por BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS versiones_documento (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL REFERENCES documentos(id) ON DELETE CASCADE,
    numero_version INTEGER NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_almacenamiento VARCHAR(500) NOT NULL,
    tipo_mime VARCHAR(100),
    tamanio_bytes BIGINT,
    comentario VARCHAR(500),
    subido_por BIGINT NOT NULL REFERENCES usuarios(id),
    subido_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS registros_financieros (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('BENEFICIO', 'GASTO')),
    descripcion VARCHAR(255) NOT NULL,
    importe NUMERIC(15, 2) NOT NULL,
    categoria VARCHAR(100),
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    creado_por BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMP DEFAULT NOW(),
    actualizado_en TIMESTAMP DEFAULT NOW()
);