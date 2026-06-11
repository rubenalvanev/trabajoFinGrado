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

-- ====================================================
-- Módulo de Proyectos
-- ====================================================
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