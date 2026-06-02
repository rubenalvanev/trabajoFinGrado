-- =====================================================
-- Business Manager - Script de Inicialización MySQL
-- =====================================================
-- Ejecutar SOLO si se prefiere creación manual de la BD.
-- Spring Boot con ddl-auto=update crea las tablas automáticamente.

CREATE DATABASE IF NOT EXISTS tfg
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tfg;

-- La aplicación crea las tablas automáticamente al arrancar.
-- El usuario administrador por defecto se crea también automáticamente:
--   Usuario: admin
--   Contraseña: admin123
