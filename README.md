# BIA App

Aplicación web para la gestión de **Análisis de Impacto en el Negocio** (*Business Impact Analysis* / BIA), un proceso clave dentro de la continuidad de negocio y la gestión de riesgos tecnológicos.

Permite registrar empresas, sus procesos críticos, el personal clave y los activos tecnológicos de los que dependen, calcular automáticamente el nivel de riesgo de cada proceso (impacto x probabilidad) y generar tanto un informe ejecutivo visual como una exportación en Excel con el mapa de calor de riesgos.

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Funcionalidades](#funcionalidades)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Modelo de datos](#modelo-de-datos)
- [Requisitos previos](#requisitos-previos)
- [Configuración](#configuración)
- [Puesta en marcha](#puesta-en-marcha)
- [Rutas principales](#rutas-principales)
- [Exportación a Excel](#exportación-a-excel)
- [Seguridad y multiusuario](#seguridad-y-multiusuario)
- [Notas de seguridad](#notas-de-seguridad)
- [Roadmap / posibles mejoras](#roadmap--posibles-mejoras)

## Descripción general

Un BIA identifica qué procesos de negocio son críticos, cuánto tiempo puede estar caída una operación antes de generar un impacto grave (RTO) y qué información se puede permitir perder (RPO). Esta aplicación digitaliza ese proceso permitiendo, por cada **empresa**, crear uno o varios **proyectos BIA**, y dentro de cada proyecto, dar de alta **procesos críticos** vinculados a las **personas** y **activos tecnológicos** de los que dependen.

Con esos datos, la aplicación calcula automáticamente:

- La **urgencia** de recuperación en función del RTO (Recovery Time Objective).
- La **criticidad** del proceso (impacto + urgencia, escala 2–10).
- El **riesgo total** (impacto × probabilidad, escala 1–25) y su clasificación en `CRITICO`, `ALTO` o `MEDIO_BAJO`.
- Los **puntos únicos de fallo (SPOF)**: personas o activos tecnológicos de los que dependen varios procesos críticos a la vez.

## Funcionalidades

- **Inicio de sesión obligatorio** con Spring Security, roles y aislamiento de datos por empresa (ver [Seguridad y multiusuario](#seguridad-y-multiusuario)).
- Alta y listado de empresas (nombre, sector, tamaño).
- Ficha de detalle por empresa con sus proyectos BIA, personas y activos tecnológicos asociados.
- Creación de proyectos BIA por empresa.
- Alta de procesos críticos dentro de un proyecto BIA, con cálculo automático de criticidad y nivel de riesgo.
- Vinculación de procesos críticos con activos tecnológicos y con personal clave.
- Dashboard del proyecto BIA con la matriz de procesos.
- Informe ejecutivo con:
  - Nº total de procesos analizados.
  - Nº de procesos en riesgo crítico y alto.
  - Detección de personas SPOF (asignadas a más de un proceso crítico).
  - Detección de activos tecnológicos SPOF.
- Exportación del proyecto BIA a un archivo **Excel (.xlsx)** con mapa de calor de riesgo por colores.

## Tecnologías

- **Java 17**
- **Spring Boot 3.5.13**
  - Spring Web (MVC)
  - Spring Data JPA
  - Thymeleaf (motor de plantillas para las vistas)
- **MySQL** (vía `mysql-connector-j`)
- **Apache POI (poi-ooxml 5.2.5)** para la generación de informes Excel
- **Maven** (con Maven Wrapper `mvnw` / `mvnw.cmd`) como gestor de dependencias y build

## Estructura del proyecto

```
BIA-app/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/bia/app/bia_app/
    │   │   ├── BiaAppApplication.java        # Punto de entrada Spring Boot
    │   │   ├── controller/
    │   │   │   ├── EmpresaController.java          # Listado y alta de empresas
    │   │   │   ├── EmpresaDetalleController.java   # Detalle de empresa, alta de BIA/personas/activos
    │   │   │   └── BiaDashboardController.java     # Dashboard, informe ejecutivo, export a Excel
    │   │   ├── model/
    │   │   │   ├── Empresa.java
    │   │   │   ├── BiaProyecto.java
    │   │   │   ├── ProcesoCritico.java              # Lógica de cálculo de riesgo/criticidad
    │   │   │   ├── ActivoTecnologico.java
    │   │   │   └── Persona.java
    │   │   ├── repository/                          # Interfaces Spring Data JPA
    │   │   └── service/
    │   │       ├── EmpresaService.java
    │   │       ├── ActivosService.java              # Lógica de alta y vinculación de entidades
    │   │       └── ExcelExportService.java          # Generación del Excel con Apache POI
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/styles.css
    │       └── templates/
    │           ├── empresas.html
    │           ├── empresa_detalle.html
    │           ├── bia_dashboard.html
    │           └── informe_ejecutivo.html
    └── test/
        └── java/com/bia/app/bia_app/BiaAppApplicationTests.java
```

## Modelo de datos

Relaciones principales entre entidades JPA:

- **Empresa** `1 — N` **Persona**
- **Empresa** `1 — N` **ActivoTecnologico**
- **Empresa** `1 — N` **BiaProyecto**
- **BiaProyecto** `1 — N` **ProcesoCritico**
- **ProcesoCritico** `N — M` **ActivoTecnologico** (tabla intermedia `proceso_activo`)
- **ProcesoCritico** `N — M` **Persona** (tabla intermedia `proceso_persona`)

Campos clave de `ProcesoCritico`:

| Campo | Descripción |
|---|---|
| `rtoHoras` | Recovery Time Objective, en horas |
| `rpo` | Recovery Point Objective |
| `impacto` | Escala 1 (bajo) a 5 (muy alto) |
| `probabilidad` | Escala 1 (rara) a 5 (casi cierta) |
| `criticidad` | Autocalculada: impacto + urgencia (derivada del RTO), escala 2–10 |
| `getRiesgoTotal()` | Autocalculado: impacto × probabilidad, escala 1–25 |
| `getNivelRiesgo()` | `CRITICO` (≥15), `ALTO` (6–14), `MEDIO_BAJO` (1–5) |

## Requisitos previos

- **JDK 17** o superior
- **MySQL** en ejecución (local o remoto)
- Maven no es necesario instalarlo aparte: el proyecto incluye el *wrapper* (`./mvnw` / `mvnw.cmd`)

## Configuración

La configuración de la aplicación se encuentra en `src/main/resources/application.properties`:

```properties
spring.application.name=bia-app
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/bia
spring.datasource.username=root
spring.datasource.password=1234

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Antes de ejecutar la aplicación:

1. Crea una base de datos en MySQL llamada `bia` (o cambia el nombre en `spring.datasource.url`).
2. Ajusta `spring.datasource.username` y `spring.datasource.password` a tus propias credenciales.
3. Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate crea/actualiza automáticamente las tablas al arrancar; no es necesario ejecutar scripts SQL manualmente.

> Ver la sección [Notas de seguridad](#notas-de-seguridad) sobre las credenciales incluidas por defecto en este archivo.

## Puesta en marcha

Clonar el repositorio y ejecutar con el wrapper de Maven:

```bash
git clone https://github.com/Jonandermorenoo88/BIA-app.git
cd BIA-app

# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

También se puede generar el `.jar` y ejecutarlo directamente:

```bash
./mvnw clean package
java -jar target/bia-app-0.0.1-SNAPSHOT.jar
```

Por defecto la aplicación queda disponible en `http://localhost:8080`.

## Rutas principales

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/empresas` | Listado de empresas |
| `POST` | `/empresas/guardar` | Crear una empresa |
| `GET` | `/empresas/{empresaId}` | Detalle de una empresa (personas, activos, proyectos BIA) |
| `POST` | `/empresas/{empresaId}/bias/guardar` | Crear un proyecto BIA para una empresa |
| `POST` | `/empresas/{empresaId}/personas/guardar` | Alta de una persona |
| `POST` | `/empresas/{empresaId}/activos/guardar` | Alta de un activo tecnológico |
| `GET` | `/empresas/{empresaId}/bias/{biaId}` | Dashboard del proyecto BIA |
| `GET` | `/empresas/{empresaId}/bias/{biaId}/informe-ejecutivo` | Informe ejecutivo (riesgos, SPOF) |
| `POST` | `/empresas/{empresaId}/bias/{biaId}/procesos/guardar` | Alta de un proceso crítico |
| `POST` | `/empresas/{empresaId}/bias/{biaId}/procesos/{idProceso}/vincular-activo` | Vincular un activo a un proceso |
| `POST` | `/empresas/{empresaId}/bias/{biaId}/procesos/{idProceso}/vincular-persona` | Vincular una persona a un proceso |
| `GET` | `/empresas/{empresaId}/bias/{biaId}/exportar-excel` | Descarga del proyecto BIA en Excel |

## Exportación a Excel

`ExcelExportService` genera un libro `.xlsx` (Apache POI) con una hoja por proyecto BIA que incluye: nombre y descripción del proceso, RTO, RPO, impacto, probabilidad, criticidad, riesgo total con **mapa de calor por colores** (verde / amarillo / rojo / rojo oscuro según el nivel de riesgo) y los activos y personas vinculados a cada proceso.

## Seguridad y multiusuario

La aplicación exige iniciar sesión y separa los datos de cada cliente.

| Rol | Qué puede hacer |
|---|---|
| `ADMIN` | Ve todas las empresas, gestiona usuarios (`/admin/usuarios`) y es el único que puede eliminar empresas. |
| `CONSULTOR` | Ve y edita **solo** las empresas que tiene asignadas. Las empresas que crea se le asignan automáticamente. |
| `CLIENTE` | **Solo lectura** de sus empresas asignadas: dashboard, informe ejecutivo y exportación a Excel. |

Controles implementados:

- **Autenticación** por formulario (Spring Security) con contraseñas en **BCrypt** (coste 12).
- **Aislamiento entre empresas (anti-IDOR):** cada petición comprueba que el usuario tiene acceso a la empresa de la URL (`AccesoService`) y que el BIA, proceso, activo o persona pertenecen realmente a esa empresa (`ActivosService`). Cambiar un id en la URL devuelve 403/404.
- **Protección frente a asignación masiva:** los formularios no pueden fijar ids ni relaciones (`@InitBinder`).
- **CSRF** activado en todos los formularios.
- **Bloqueo de cuenta** durante 15 minutos tras 5 contraseñas erróneas seguidas.
- **Política de contraseñas** (mínimo 12 caracteres, letras y números) y **contraseñas temporales** obligatorias de cambiar en el primer acceso.
- **Sesión endurecida:** cookie `HttpOnly` y `SameSite=Strict`, caducidad a los 30 minutos, regeneración del id de sesión al iniciar sesión, cabeceras anti-clickjacking.
- **Tests de integración** (`SeguridadIntegrationTests`) que comprueban todo lo anterior contra una base de datos H2 en memoria.

### Primer arranque

Si la tabla de usuarios está vacía se crea un administrador:

- Usuario: `BIA_ADMIN_USERNAME` (por defecto `admin`).
- Contraseña: `BIA_ADMIN_PASSWORD`. Si no la defines, se genera una aleatoria que aparece **una sola vez** en el log de arranque y se pide cambiarla en el primer inicio de sesión.

```bash
BIA_ADMIN_PASSWORD='UnaContraseñaLarga2026' ./mvnw spring-boot:run
```

Desde **Usuarios** el administrador crea consultores y clientes, les asigna empresas, resetea contraseñas (genera una temporal y desbloquea la cuenta) y desactiva cuentas. Siempre debe quedar al menos un administrador activo.

En producción con HTTPS define `SESSION_COOKIE_SECURE=true`.

## Notas de seguridad

- El archivo `application.properties` versionado en el repositorio contiene credenciales de base de datos en texto plano (`root` / `1234`). Se recomienda **no** mantener credenciales reales en este archivo: usar variables de entorno (`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) o un fichero `application-local.properties` excluido de git, y añadir `application.properties` (o el fichero con secretos) al `.gitignore`.
- Antes de desplegar en un entorno real, cambiar `spring.jpa.hibernate.ddl-auto` de `update` a un valor más controlado (por ejemplo `validate`) y gestionar el esquema con migraciones (Flyway/Liquibase).
- El repositorio incluye un `startup.log` con la salida de arranque de la aplicación; conviene revisarlo por si contiene rutas locales o información sensible y, si no aporta valor, eliminarlo del control de versiones.

## Roadmap / posibles mejoras

- ~~Autenticación y control de acceso por usuario/rol.~~ Hecho.
- Registro de auditoría (quién cambió qué y cuándo).
- Doble factor de autenticación (TOTP).
- Externalizar credenciales de base de datos (variables de entorno / *secrets*).
- Migraciones de esquema con Flyway o Liquibase en lugar de `ddl-auto=update`.
- Tests automatizados de controladores y servicios (actualmente solo existe el test de contexto por defecto de Spring Boot).
- Paginación y búsqueda en el listado de empresas.
- Internacionalización de las vistas Thymeleaf.
