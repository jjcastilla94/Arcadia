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
| **Base de Datos** | PostgreSQL |
| **Autenticación** | JWT |
| **Frontend** | Angular **o** Vue 3 + Vite |
| **Estado Global** | Pinia (Vue) / Servicio RxJS (Angular) |
| **Despliegue** | Docker, VPS, Railway, Render, Vercel/Netlify |

---

## ✨ Funcionalidades Clave

### 👥 Módulo de Usuarios *(Estilo Steam)*

- **Autenticación:** Registro, inicio de sesión y gestión de perfil (avatar y nickname).
- **Biblioteca personal (My Library):** añade juegos del catálogo a tu biblioteca propia.
- **Favoritos y jugados recientemente.**
- **Progreso y Cloud Save:** guardado automático de puntuaciones y nivel vinculado a la cuenta.
- **Sistema de Logros:** insignias que se envían desde los juegos a la plataforma.
- **Estadísticas:** contador de tiempo total jugado por juego.

### 🎮 Módulo de Catálogo y Reproductor *(Estilo Friv)*

- **Dashboard:** cuadrícula visualmente atractiva con miniaturas de los juegos.
- **Filtros y búsqueda:** por nombre y por géneros (Acción, Puzzle, Carreras, etc.).
- **Player View:** juego HTML5 ejecutado en un `<iframe>` seguro.
  - Botón de **pantalla completa** (Fullscreen API).
  - Panel lateral con información del juego, logros disponibles/completados y controles.

### 🛡️ Módulo de Administración *(Admin Panel)*

- **Subida de juegos:** formulario con carga de archivos `.zip` (con `index.html` y assets), título, descripción, categoría y miniatura.
- **Gestión de juegos:** editar, ocultar y publicar juegos del catálogo.

---

## 🗺️ Roadmap de Desarrollo

```text
FASE 1  ➜  Fundamentos y Autenticación
FASE 2  ➜  Gestión y Subida de Juegos
FASE 3  ➜  El Reproductor e Iframe
FASE 4  ➜  Nube, Guardado y Logros
FASE 5  ➜  Pulido y Despliegue
```

### 🧱 FASE 1: Configuración Base y Autenticación

> **Objetivo:** tener la estructura del proyecto lista, la base de datos conectada y un sistema de login/registro funcional.

**Backend (Spring Boot)**
- Crear el proyecto con Spring Initializr (Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Validation, Lombok).
- Configurar la conexión a PostgreSQL en `application.yml`.
- Crear las entidades `@Entity` de `User` y `Role` (`ROLE_USER`, `ROLE_ADMIN`).
- Seguridad y JWT: `SecurityFilterChain`, `JwtTokenProvider` y `JwtAuthenticationFilter`.
- Endpoints: `POST /api/auth/register` y `POST /api/auth/login`.

**Frontend (Angular / Vue)**
- Crear la app (Angular o Vue 3 + Vite) con rutas y cliente HTTP (Axios / HttpClient).
- Estado global con Pinia (Vue) o Servicio/RxJS (Angular).
- Vistas base: registro, login y layout global con navbar y estado de sesión.
- Guardas de navegación (AuthGuard).

### 🎮 FASE 2: Gestión y Subida de Juegos *(Lado Admin)*

> **Objetivo:** permitir al administrador subir archivos `.zip` con juegos HTML5, descomprimirlos en el servidor y listarlos.

**Backend (Spring Boot)**
- Entidad `Game`: título, descripción, ruta del archivo, miniatura, categoría y fecha.
- `StorageService`: recibe `.zip` vía `MultipartFile`, lo descomprime en `uploads/games/{game-slug}/`, valida que exista `index.html` y guarda la miniatura en `uploads/thumbnails/`.
- `WebMvcConfigurer` para servir la carpeta `uploads/` como recurso estático.
- Endpoints: `POST /api/admin/games` (requiere `ROLE_ADMIN`) y `GET /api/games`.

**Frontend (Angular / Vue)**
- **AdminDashboard:** formulario de subida (texto, categoría, `.zip` y miniatura).
- **Catálogo (Home estilo Friv):** `GET /api/games` para renderizar la cuadrícula con tarjetas/miniaturas.

### 🕹️ FASE 3: El Reproductor e Integración Iframe *(Lado Usuario)*

> **Objetivo:** que cualquier usuario pueda entrar a un juego, jugarlo dentro de la web y añadirlo a su biblioteca.

**Backend (Spring Boot)**
- Entidad `LibraryItem` (relaciona `User` + `Game` con fecha de añadido y tiempo jugado).
- Endpoints:
  - `POST /api/library/add/{gameId}`
  - `DELETE /api/library/remove/{gameId}`
  - `GET /api/library/my-games`

**Frontend (Angular / Vue)**
- **GameView:** `<iframe src="http://localhost:8080/games/nombre-juego/index.html">`.
- Botón de pantalla completa (Fullscreen API).
- Botón **"Añadir a mi Biblioteca"**.
- **LibraryView:** vista de lista/mosaico con los juegos del usuario activo.

### 🏆 FASE 4: Nube, Guardado y Sistema de Logros

> **Objetivo:** conectar el iframe con la app principal para guardar partidas y otorgar logros.

**Intercomunicación Juego ➔ Web (JavaScript `window.postMessage`)**
- Evento `SAVE_DATA`: guardar progreso.
- Evento `UNLOCK_ACHIEVEMENT`: desbloquear un logro.

**Backend (Spring Boot)**
- Entidades: `Achievement` (título, descripción, icono, `game_id`) y `UserAchievement` (usuario, logro, fecha de desbloqueo).
- Endpoints:
  - `POST /api/progress/save`
  - `POST /api/achievements/unlock`
  - `GET /api/achievements/game/{gameId}`

**Frontend (Angular / Vue)**
- Escuchador de eventos `window.addEventListener('message', ...)` en el reproductor.
- Notificación popup de logro desbloqueado (estilo Steam Toast).
- Sección de logros en el perfil del usuario.

### 🎨 FASE 5: Pulido, Dashboard de Perfil y Despliegue

> **Objetivo:** dar el estilo visual definitivo, optimizar rendimiento y preparar producción.

- **Perfil de usuario:** avatar, estadísticas generales (juegos jugados, total de logros, tiempo total).
- **Filtros y buscador en catálogo:** filtrar por categorías o búsqueda en tiempo real por nombre.
- **UI/UX:** loaders/skeletons, modales de confirmación y alertas de error estilizadas.
- **Despliegue:** backend + BD en VPS, Railway, Render o Docker; frontend en Vercel, Netlify o servido junto con Spring Boot.

---

<div align="center">

*Alvaro Castilla  — 2026*

</div>
