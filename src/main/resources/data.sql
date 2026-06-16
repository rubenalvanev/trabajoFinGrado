INSERT INTO grupos (nombre, descripcion) VALUES
('GRUPO_FINANZAS', 'Acceso al módulo de finanzas'),
('GRUPO_PROYECTOS', 'Acceso al módulo de proyectos'),
('GRUPO_INVENTARIO', 'Acceso al módulo de inventario'),
('GRUPO_DOCUMENTOS', 'Acceso al módulo de gestión documental')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO modulos (clave, nombre, descripcion, icono, obligatorio, orden) VALUES
('MODULOS',    'Módulos',    'Gestión y activación de módulos del sistema',        'grid',       TRUE,  1),
('USUARIOS',   'Usuarios',   'Gestión de usuarios y perfiles',                     'users',      TRUE,  2),
('GRUPOS',     'Grupos',     'Gestión de grupos y asignación de miembros',         'users',      TRUE,  3),
('FICHAJES',   'Fichajes',   'Control de presencia y registro de jornada laboral', 'clock',      TRUE,  4),
('FINANZAS',   'Finanzas',   'Control de beneficios y gastos de la empresa',       'trending-up',FALSE, 5),
('INVENTARIO', 'Inventario', 'Gestión del stock y proveedores',                    'package',    FALSE, 6),
('PROYECTOS',  'Proyectos',  'Gestión de proyectos y clientes',                    'briefcase',  FALSE, 7),
('DOCUMENTOS', 'Documentos', 'Gestión documental con versionado y categorías',     'file-text',  FALSE, 8)
ON CONFLICT (clave) DO NOTHING;

INSERT INTO usuarios (nombre, apellidos, email, contrasena, activo, rol_id)
VALUES ('Administrador', 'Sistema', 'admin@localytics.com',
        '$2b$12$IzWn/RoQoq6CmKg/Yvns9.0mh/ogMKSzEVIt9FybNT2ScjZ9tsNUC',
        TRUE, 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO modulos_activos (modulo_id, activado_por)
SELECT m.id, u.id
FROM modulos m, usuarios u
WHERE m.obligatorio = TRUE AND u.email = 'admin@localytics.com'
ON CONFLICT (modulo_id) DO NOTHING;

INSERT INTO modulos_activos (modulo_id, activado_por)
SELECT m.id, u.id FROM modulos m, usuarios u
WHERE m.clave = 'GRUPOS' AND u.email = 'admin@localytics.com'
ON CONFLICT (modulo_id) DO NOTHING;

INSERT INTO modulos (clave, nombre, descripcion, icono, obligatorio, orden)
VALUES ('DOCUMENTOS', 'Documentos', 'Gestión documental con versionado y categorías', 'file-text', FALSE, 6)
ON CONFLICT (clave) DO NOTHING;