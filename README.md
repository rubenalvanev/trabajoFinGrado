# Localytics ERP

Sistema ERP modular desarrollado con **Spring Boot 4.0.5**, **Java 21**, **Maven** y **Supabase (PostgreSQL)**.

---

## Inicio rápido

### 1. Configurar Supabase

1. Accede a [supabase.com](https://supabase.com) y crea un nuevo proyecto.
2. Ve a **SQL Editor** y ejecuta el contenido de `src/main/resources/schema.sql`.
3. Copia la **URL de conexión** desde *Connect → Connection string → Session pooler*.

### 2. Configurar la aplicación

Crea un archivo `.env`, copiando y pegando la informacion del archivo `.env.example` y cambia el contenido por tus credenciales de Supabase:

```properties
spring.datasource.url=jdbc:postgresql://[TU_HOST].supabase.com:5432/postgres
spring.datasource.username=[TU_USERNAME]
spring.datasource.password=[TU_PASSWORD]
localytics.jwt.secreto=[CAMBIA_ESTE_SECRETO_POR_UNO_SEGURO_256_BITS]
```

### 3. Ejecutar

```bash
mvn spring-boot:run
```

Accede a: **http://localhost:8080**

---

## Credenciales por defecto

| Email | Contraseña | Rol |
|-------|-----------|-----|
| admin@localytics.com | Admin1234! | ADMIN |

> **Nota:** El hash en `schema.sql` corresponde a `Admin1234!` con BCrypt strength 12.

---

## Arquitectura

```
src/main/java/com/tfg/trabajoFinGrado/
├── TrabajoFinGradoApplication.java     # Clase principal
├── configuracion/                      # Spring Security, UserDetails
│   ├── ConfiguracionSeguridad.java
│   ├── ControladorPaginasWeb.java
│   └── ServicioDetallesUsuario.java
├── seguridad/                          # JWT
│   ├── FiltroAutenticacionJwt.java
│   └── ServicioJwt.java
├── comun/                              # Utilidades compartidas
│   ├── excepcion/
│   └── respuesta/
└── modulos/
    ├── autenticacion/                  # Login JWT
    ├── documentos/                     # Subir, descargar archivos
    ├── fichajes/                       # Fichajes de empresa
    ├── finanzas/                       # Beneficios, gastos, PDF
    ├── grupos/                         # Gestión de grupos
    ├── inventario/                     # Stock
    ├── modulos/                        # Activar/desactivar módulos
    ├── proyectos/                      # Proyectos y clientes
    └── usuarios/                       # Gestión de usuarios
```

---

## Stack técnico

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 4.0.5, Java 21 |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) |
| Base de datos | Supabase (PostgreSQL) vía JPA/Hibernate |
| Frontend | HTML/CSS/JS vanilla (SPA) |
| Gráficos | Chart.js 4.4 |
| PDF | iText 8 |
| Build | Maven |

---

## Variables de entorno (recomendado para producción)

En `application.properties`:
```properties
spring.datasource.url=${DB_URL}
spring.datasource.password=${DB_PASSWORD}
localytics.jwt.secreto=${JWT_SECRET}
```
