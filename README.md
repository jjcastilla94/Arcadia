<div align="center">

# 🎮 Arcadia

**Plataforma web Full-Stack para jugar al instante sin descargas**

Juega en el navegador, guarda tu progreso, desbloquea logros y construye tu propia biblioteca personal.

</div>

---

## 📖 ¿Qué es Arcadia?

**Arcadia** es una plataforma web que combina lo mejor de dos mundos:

- 🕹️ **La inmediatez de un Arcade (Friv):** juega directamente en el navegador, sin instalar nada.
- 📚 **La personalización de tu biblioteca (Steam):** progreso centralizado, biblioteca propia, logros y estadísticas por usuario.

El **administrador** es la única figura con permisos para subir, editar y publicar juegos HTML5 en el catálogo.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
| --- | --- |
| **Backend** | Spring Boot (Spring Web, Spring Data JPA, Spring Security, Validation, Lombok) |
| **Base de Datos** | MySQL |
| **Autenticación** | JWT |
| **Frontend** | Vue 3 + Vite (Axios) |
| **Estado Global** | Pinia |
| **Despliegue** | Docker, Cloudinary, Railway, Render, Vercel |

---

## 🚀 Puesta en marcha (desarrollo)

Prerrequisitos: [Docker Desktop](https://www.docker.com/products/docker-desktop/), JDK 21 y Node 20+.

```bash
# 1. Base de datos (MySQL 8.4 en Docker) — aplica db/schema.sql la primera vez
cp .env.example .env   # ajusta credenciales y JWT_SECRET aquí (nunca se commitea .env)
docker compose up -d

# 2. Backend (Spring Boot) — http://localhost:8080
# Lee las variables del .env de la raíz automáticamente (JWT_SECRET, DB_*, MYSQL_*)
cd backend
./mvnw spring-boot:run

# 3. Frontend (Vue 3 + Vite) — http://localhost:5173
cd frontend
npm install
npm run dev
```

- **Swagger/OpenAPI:** http://localhost:8080/swagger-ui.html
- **MySQL (DBeaver/Workbench):** host `localhost:3306`, BD `${MYSQL_DATABASE}`, usuario `${MYSQL_USER}` / contraseña `${MYSQL_PASSWORD}` (ver `.env`).
- Detener la base de datos: `docker compose down` (los datos persisten en el volumen `arcadia_mysql_data`).

---

## ✨ Funcionalidades Clave

### 👥 Módulo de Usuarios *(Estilo Steam)*

- **Autenticación:** Registro, inicio de sesión, verificación de email y recuperación de contraseña.
- **Biblioteca personal (My Library):** añade juegos del catálogo a tu biblioteca propia.
- **Favoritos:** independientes de la biblioteca (puedes marcar favorito sin añadir el juego).
- **Estado y valoración:** marca juegos como *En curso / Completado / Abandonado* y puntúalos de 1 a 5.
- **Reseñas (Reviews):** rating + comentario por juego, como en una plataforma real.
- **Sesiones de juego:** registro de cada sesión para estadísticas (tiempo medio, horas jugadas, sesión más larga).
- **Progreso y Cloud Save:** guardado automático de puntuaciones y nivel vinculado a la cuenta.
- **Sistema de Logros:** insignias con puntos que se envían desde los juegos a la plataforma.
- **Estadísticas:** contador de tiempo total jugado por juego.

### 🎮 Módulo de Catálogo y Reproductor *(Estilo Friv)*

- **Dashboard:** cuadrícula visualmente atractiva con miniaturas de los juegos.
- **Filtros y búsqueda:** por nombre y por géneros (Acción, Puzzle, Carreras, etc.).
- **Galería de capturas:** imágenes del juego (portada y capturas estilo Steam).
- **Más jugados:** ranking calculado agregando `play_sessions` (sin contadores duplicados).
- **Player View:** juego HTML5 ejecutado en un `<iframe>` seguro.
  - Botón de **pantalla completa** (Fullscreen API).
  - Panel lateral con información del juego, capturas, reseñas, logros disponibles/completados y controles.

### 🛡️ Módulo de Administración *(Admin Panel)*

- **Subida de juegos:** formulario con carga de archivos `.zip` (con `index.html` y assets), título, descripción, categoría, miniatura, portada y capturas.
- **Control de versiones:** cada juego tiene `version` y `file_size` registrados.
- **Gestión de juegos:** editar, ocultar y publicar juegos del catálogo.

---

## 🗺️ Roadmap de Desarrollo

```text
FASE 1  ✅  Proyecto base, JWT y Usuarios  *(completada)*
FASE 2  ✅  Catálogo, Admin, Biblioteca y Perfil  *(completada)*
FASE 3  ✅  Player y Sesiones de Juego  *(completada)*
FASE 4  ➜  Cloud Save + Arcadia.js
FASE 5  ➜  Sistema de Logros
FASE 6  ➜  Favoritos y Reseñas
FASE 7  ➜  Perfil avanzado y Pulido
FASE 8  ➜  Seguridad, Testing y Optimización
FASE 9  ➜  Despliegue y Producción
```

> El orden está pensado para tener **siempre algo funcionando** de principio a fin.

### 🧱 FASE 1: Proyecto Base, JWT y Usuarios

> **Objetivo:** estructura del proyecto lista, BD conectada y sistema de login/registro funcional.

**Backend (Spring Boot)**
- Crear el proyecto con Spring Initializr (Spring Web, Spring Data JPA, Spring Security, MySQL Driver, Validation, Lombok).
- Configurar la conexión a MySQL en `application.yml`.
- Crear las entidades `@Entity` de `User` y `Role` (`ROLE_USER`, `ROLE_ADMIN`) con relación N:M.
- Seguridad y JWT: `SecurityFilterChain`, `JwtTokenProvider` y `JwtAuthenticationFilter`.
- Tablas de `refresh_tokens`, `password_reset_tokens` y `email_verification_tokens`.
- Endpoints: `POST /api/auth/register` y `POST /api/auth/login`.

**Frontend (Vue 3 + Vite)**
- Crear la app con Vue 3 + Vite, `vue-router` y Axios como cliente HTTP.
- Estado global con Pinia (store de auth con el token del usuario).
- Vistas base: registro, login y layout global con navbar y estado de sesión.
- Guardas de navegación del router (redirect si el usuario no está autenticado).

**✅ Estado: FASE 1 COMPLETADA**

| Área | Implementado |
| --- | --- |
| **Backend** | Spring Boot, Spring Security, JWT + Refresh Tokens, Swagger/OpenAPI, DTOs + MapStruct, manejo global de excepciones, Spring Data JPA |
| **Base de Datos** | MySQL 8.4 en Docker, 17 tablas, seed inicial (roles y categorías) |
| **Frontend** | Vue 3, Vite, Pinia, Vue Router, Axios, Login, Registro, Navbar, persistencia de sesión |
| **Integración** | Login real, Registro real, `GET /api/users/me`, proxy de Vite `/api → :8080`, comunicación completa Frontend ↔ Backend |

### 🎮 FASE 2: Catálogo, Admin, Biblioteca y Perfil

> **Objetivo:** que el catálogo público muestre los juegos en una cuadrícula estilo Friv y que el administrador pueda subir y gestionar juegos mientras el usuario construye su biblioteca y perfil.

**Backend (Spring Boot)**
- Endpoint público `GET /api/games` (título, miniatura, portada, categoría y contador de partidas derivado de `play_sessions`).
- Búsqueda por nombre y filtro por categoría (tabla `categories`, no ENUM).

**Frontend (Vue 3 + Vite)**
- **Home estilo Friv:** cuadrícula con tarjetas, búsqueda en tiempo real y filtros por género.

**✅ Estado: FASE 2 COMPLETADA**

| Área | Implementado |
| --- | --- |
| **Catálogo** | `GET /api/games` público con búsqueda por nombre y filtro por categoría; solo aparecen juegos publicados (borradores y ocultos dan 404) |
| **Game Details** | `GET /api/games/{slug}` con portada, galería de capturas, historial de versiones, logros (incluidos los secretos) y estadísticas derivadas de `play_sessions` |
| **Admin** | `GET/POST/PUT/DELETE /api/admin/games` con `ROLE_ADMIN`: subida de ZIP, edición, publicar/ocultar, nuevas versiones y borrado (BD + ficheros) |
| **Biblioteca** | `GET/POST/DELETE /api/library` con soft-delete: añadir/quitar, estado, valoración y persistencia de stats al re-añadir |
| **Perfil** | `GET/PUT /api/users/me` y `PUT /api/users/me/password`: editar nickname/avatar, cambiar contraseña (BCrypt) con comprobación de unicidad y validación; vista `/profile` en el frontend |
| **Permisos** | Admin solo para `ROLE_ADMIN` (403 a usuarios), biblioteca autenticada (401 anónimo); guardas en el router de Vue |

### 🕹️ FASE 3: Player y Sesiones de Juego

> **Objetivo:** que cualquier usuario pueda entrar a un juego y jugarlo dentro de la web.

**Backend (Spring Boot)**
- Registro de sesiones de juego en `play_sessions` (inicio, fin, duración) como fuente de verdad para el ranking de "Más jugados" y las estadísticas (consultas agregadas, sin contador duplicado): `POST /api/play-sessions/start` y `POST /api/play-sessions/end`.
- Al jugar, actualizar `last_played_at` y `time_played_seconds` en la biblioteca del usuario.
- Seguridad del reproductor: solo accesible para juegos publicados (borradores y ocultos dan 404), validación del juego y control de sesiones.

**Frontend (Vue 3 + Vite)**
- **PlayerView** en `/games/:slug/play`: `<iframe>` con el `index.html` del juego, botón **Jugar** real, pantalla completa (Fullscreen API), volver al detalle y estados de loading/error.
- Panel lateral: info del juego, capturas, reseñas, logros disponibles/completados y controles.
- **Integración con la biblioteca:** jugados recientemente y estadísticas del juego derivadas de `play_sessions`.

**✅ Estado: FASE 3 COMPLETADA**

| Área | Implementado |
| --- | --- |
| **Player** | `PlayerView.vue` en `/play/:slug`: iframe HTML5 con sandbox, loading/error, pantalla completa (Fullscreen API), volver al detalle y botón **Jugar** desde Game Details |
| **Sesiones** | `POST /api/play-sessions/start` y `/end`: inicio, `ended_at`, `duration_seconds`, cierre idempotente y ownership por usuario (sesión ajena → 404) |
| **Biblioteca** | Actualización de `time_played_seconds` y `last_played_at` al terminar la sesión (si el juego está en la biblioteca) |
| **Cierre robusto** | `fetch` con `keepalive` + `Authorization` en `pagehide` para cerrar la sesión al hacer F5 o cerrar la pestaña |
| **Seguridad** | `start` solo para juegos publicados y no ocultos (404 si no); anónimo → 401; token inválido → 401 |
| **Validación** | 12 pruebas de Player/sesiones pasadas contra el stack real + test de integración `SessionFlowIntegrationTest` |

### ☁️ FASE 4: Cloud Save + Arcadia.js

> **Objetivo:** conectar el iframe con la app para guardar partidas automáticamente y formalizar el SDK para juegos.

**Intercomunicación Juego ➔ Web (JavaScript `window.postMessage`)**
- Evento `SAVE_DATA`: guardar progreso.
- Evento `LOAD_DATA`: recuperar la partida guardada.

**Backend (Spring Boot)**
- Tabla `saved_games` con JSON (`{ level, coins, weapons }`).
- Endpoints:
  - `POST /api/progress/save`
  - `GET /api/progress/{gameId}`

**SDK Arcadia.js**
- Librería JavaScript para que los juegos se integren sin conocer el protocolo interno:
  - `Arcadia.save(data)` → `SAVE_DATA`
  - `Arcadia.getSave()` → `LOAD_DATA`
  - `Arcadia.unlock(id)` → logros (FASE 5)
  - `Arcadia.getUser()` → usuario autenticado

**Frontend (Vue 3 + Vite)**
- Escuchador de eventos `window.addEventListener('message', ...)` en el reproductor, con validación de origen y estructura.

### 🏆 FASE 5: Sistema de Logros

> **Objetivo:** sistema de logros con puntos y logros secretos.

**Backend (Spring Boot)**
- Entidades: `Achievement` (título, descripción, icono, `points`, `hidden`) y `UserAchievement` (fecha de desbloqueo).
- Endpoints:
  - `POST /api/achievements/unlock`
  - `GET /api/achievements/game/{gameId}`

**Frontend (Vue 3 + Vite)**
- Notificación popup de logro desbloqueado (estilo Steam Toast) al recibir `Arcadia.unlock(...)` en el reproductor.
- Sección de logros en el perfil del usuario (ocultando los secretos sin desbloquear).

### ❤️ FASE 6: Favoritos y Reseñas

> **Objetivo:** favoritos independientes de la biblioteca y reseñas (rating + comentario) por juego.

**Backend (Spring Boot)**
- Favoritos (tabla `favorites`):
  - `POST /api/favorites/{gameId}`
  - `DELETE /api/favorites/{gameId}`
  - `GET /api/favorites`
- Reseñas (tabla `reviews`):
  - `POST /api/reviews`
  - `PUT /api/reviews/{id}`
  - `DELETE /api/reviews/{id}`
  - `GET /api/games/{slug}/reviews`

**Frontend (Vue 3 + Vite)**
- Botones de **Favorito** en Game Details y Player.
- Listado y creación de reseñas en Game Details.

### 🎨 FASE 7: Perfil avanzado y Pulido

> **Objetivo:** dar el estilo visual definitivo y la UX profesional con estadísticas reales del usuario.

- **Perfil de usuario:** avatar y estadísticas reales (juegos jugados, horas jugadas, tiempo medio, sesión más larga, total de logros) calculadas a partir de `play_sessions`, `achievements` y `user_achievements`.
- **UI/UX:** skeletons/loaders, animaciones, responsive, modales de confirmación, errores estilizados y accesibilidad básica.

### 🛡️ FASE 8: Seguridad, Testing y Optimización

> **Objetivo:** dejar la aplicación lista para producción.

- **Backend:** tests unitarios, de integración y de seguridad; validación de permisos; rate limiting si procede; CORS definitivo; gestión de errores; validación de archivos ZIP; protección de `postMessage` y revisión de endpoints.
- **Frontend:** rutas protegidas, manejo global de errores, expiración/refresh de JWT, estados de loading, errores de red y build de producción.
- **Rendimiento:** consultas SQL e índices, paginación, optimización de imágenes, tamaños de ZIP y almacenamiento.

### 🚀 FASE 9: Despliegue y Producción

> **Objetivo:** llevar Arcadia a producción con CI/CD sobre AWS.

- **Desarrollo:** el entorno Docker + MySQL + Spring Boot + Vue actual es la base que se llevará a producción.
- **CI/CD:** GitHub Actions construye la imagen Docker del backend y el build del frontend.
- **Backend + BD:** imagen Docker en AWS ECR y despliegue en AWS EC2 con Docker Compose (Spring Boot + MySQL).
- **Frontend:** Vercel.
- **Producción:** variables de entorno, HTTPS y rotación de refresh tokens.

---

## 🎁 Extra: SDK para Juegos *(Arcadia.js)*

Una pequeña librería JavaScript para que los propios juegos se integren con Arcadia sin conocer el protocolo interno:

```js
Arcadia.save({ level: 4, coins: 120 });
Arcadia.unlock("first-boss");
Arcadia.getSave();
Arcadia.getUser();
```

Internamente usa `window.postMessage`, pero para el desarrollador del juego la integración es trivial. Esto convierte a **Arcadia en una plataforma**, no solo en una web que incrusta juegos.

---

## 🏗️ Arquitectura del Backend

![Arquitectura](docs/architecture.png)

Organización por módulos de negocio, con capas `controller → service → repository` dentro de cada uno (detalle en [`docs/architecture.md`](docs/architecture.md)):

```text
backend
│
├── admin           # Gestión admin de juegos (AdminGameController, subida de ZIP)
├── auth            # Registro, login, refresh tokens (AuthController, AuthService)
├── user            # Perfil autenticado (GET/PUT /api/users/me)
├── game            # Catálogo público y detalle (GET /api/games, GET /api/games/{slug})
├── library         # Biblioteca personal (GET/POST/DELETE /api/library)
├── session         # Sesiones de juego (POST /api/play-sessions/start|end)
├── storage         # StorageService: descompresión de ZIP y servir uploads/
├── security        # SecurityConfig, JwtService, JwtAuthenticationFilter, CustomUserDetails
├── config          # CorsConfig, OpenApiConfig (Swagger)
├── common          # ApiResponse, GlobalExceptionHandler, excepciones y utilidades
├── entity          # Entidades JPA (@Entity User, Role, Game, ...)
└── repository      # Repositorios Spring Data JPA
```

> Los módulos de `admin`, `game`, `category`, `library`, `session`, `storage` y `user` ya están incorporados (fases 1-2). Los módulos de `achievement`, `review`, `favorite` y `sdk` se incorporarán en las fases 4-6 del roadmap.

Diagrama entidad-relación de la base de datos: [`docs/arcadia_entidad_relacion.png`](docs/arcadia_entidad_relacion.png).

Y dentro de cada módulo:

```text
controller  →  service  →  repository  →  entity
            ↘  dto  →  mapper (MapStruct)  ↗
```

**Buenas prácticas previstas:** DTOs en lugar de exponer entidades, validaciones con Bean Validation, manejo global de excepciones, documentación con Swagger/OpenAPI y Docker Compose para levantar el entorno completo.

---

<div align="center">

*Alvaro Castilla  — 2026*

</div>
