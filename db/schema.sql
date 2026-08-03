-- ============================================================
-- ARCADIA · Esquema de Base de Datos (MySQL 8)
-- Plataforma web Full-Stack de juegos en el navegador
-- ============================================================
-- Ejecutar como: mysql -u root -p < db/schema.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- (Opcional) Crear la base de datos y usarla
CREATE DATABASE IF NOT EXISTS arcadia
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE arcadia;

-- ------------------------------------------------------------
-- 1. ROLES
-- Escalable: mañana se pueden añadir MODERATOR, EDITOR, TESTER...
-- ------------------------------------------------------------
CREATE TABLE roles (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_name (name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. USUARIOS
-- ------------------------------------------------------------
CREATE TABLE users (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nickname       VARCHAR(50)  NOT NULL,
    email          VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    avatar_url     VARCHAR(255),
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_nickname (nickname),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

-- Relación N:M entre usuarios y roles
CREATE TABLE user_roles (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id INT UNSIGNED    NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. CATEGORÍAS (géneros de los juegos)
-- Tabla (no ENUM): permite añadir Terror, Zombies... sin recompilar
-- ------------------------------------------------------------
CREATE TABLE categories (
    id   INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_categories_name (name),
    UNIQUE KEY uq_categories_slug (slug)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. JUEGOS
-- ------------------------------------------------------------
CREATE TABLE games (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title          VARCHAR(120) NOT NULL,
    slug           VARCHAR(140) NOT NULL,
    description    TEXT,
    file_path      VARCHAR(255) NOT NULL,   -- ruta del index.html / zip descomprimido
    file_size      BIGINT UNSIGNED,         -- tamaño del juego en bytes
    version        VARCHAR(20)  NOT NULL DEFAULT '1.0', -- versión actual (ver game_versions)
    thumbnail_path VARCHAR(255),            -- miniatura en uploads/thumbnails/
    cover_url      VARCHAR(255),            -- portada (banner) principal
    category_id    INT UNSIGNED,
    is_public      BOOLEAN NOT NULL DEFAULT FALSE, -- publicado en el catálogo
    is_hidden      BOOLEAN NOT NULL DEFAULT FALSE, -- oculto (soft delete)
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_games_slug (slug),
    KEY idx_games_category (category_id),
    KEY idx_games_public (is_public),
    KEY idx_games_title (title),
    CONSTRAINT fk_games_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4.1 VERSIONES DEL JUEGO
-- Historial de versiones: permite volver atrás, notas de
-- actualización y saber con qué versión jugó cada usuario.
-- El "Más jugados" se calcula agregando play_sessions (no hay
-- contador desnormalizado en games).
-- ------------------------------------------------------------
CREATE TABLE game_versions (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    game_id       BIGINT UNSIGNED NOT NULL,
    version       VARCHAR(20) NOT NULL,
    file_path     VARCHAR(255) NOT NULL,
    release_notes TEXT,
    uploaded_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_game_versions_game_version (game_id, version),
    CONSTRAINT fk_game_versions_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. IMÁGENES DEL JUEGO (capturas estilo Steam)
-- ------------------------------------------------------------
CREATE TABLE game_images (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    game_id    BIGINT UNSIGNED NOT NULL,
    image_url  VARCHAR(255) NOT NULL,
    position   INT UNSIGNED NOT NULL DEFAULT 0, -- orden de visualización
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_game_images_position (game_id, position),
    CONSTRAINT fk_game_images_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. LOGROS (por juego)
-- ------------------------------------------------------------
CREATE TABLE achievements (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    game_id     BIGINT UNSIGNED NOT NULL,
    title       VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    icon        VARCHAR(255),
    points      INT UNSIGNED NOT NULL DEFAULT 0, -- 10 / 25 / 50 / 100 (estilo Xbox)
    hidden      BOOLEAN      NOT NULL DEFAULT FALSE, -- logro secreto
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_achievements_game_title (game_id, title),
    CONSTRAINT fk_achievements_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Logros desbloqueados por cada usuario
CREATE TABLE user_achievements (
    user_id        BIGINT UNSIGNED NOT NULL,
    achievement_id BIGINT UNSIGNED NOT NULL,
    unlocked_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, achievement_id),
    CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. BIBLIOTECA PERSONAL (User + Game)
-- ------------------------------------------------------------
CREATE TABLE library_items (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id             BIGINT UNSIGNED NOT NULL,
    game_id             BIGINT UNSIGNED NOT NULL,
    added_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_played_at      TIMESTAMP NULL,
    time_played_seconds BIGINT UNSIGNED NOT NULL DEFAULT 0,
    status              ENUM('PLAYING', 'COMPLETED', 'ABANDONED') NOT NULL DEFAULT 'PLAYING',
    rating              TINYINT UNSIGNED NULL, -- 1-5
    PRIMARY KEY (id),
    UNIQUE KEY uq_library_user_game (user_id, game_id),
    KEY idx_library_user (user_id),
    KEY idx_library_game (game_id),
    KEY idx_library_last_played (last_played_at),
    CONSTRAINT chk_library_rating CHECK (rating IS NULL OR (rating BETWEEN 1 AND 5)),
    CONSTRAINT fk_library_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_library_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 8. FAVORITOS (independiente de la biblioteca)
-- Se puede marcar favorito sin añadir el juego a la biblioteca
-- ------------------------------------------------------------
CREATE TABLE favorites (
    user_id    BIGINT UNSIGNED NOT NULL,
    game_id    BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, game_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 9. RESEÑAS (Reviews)
-- ------------------------------------------------------------
CREATE TABLE reviews (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    game_id    BIGINT UNSIGNED NOT NULL,
    rating     TINYINT UNSIGNED NOT NULL, -- 1-5
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_reviews_user_game (user_id, game_id),
    KEY idx_reviews_game (game_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 10. SESIONES DE JUEGO (para estadísticas)
-- ------------------------------------------------------------
CREATE TABLE play_sessions (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id           BIGINT UNSIGNED NOT NULL,
    game_id           BIGINT UNSIGNED NOT NULL,
    started_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at          TIMESTAMP NULL,
    duration_seconds  BIGINT UNSIGNED NULL,
    PRIMARY KEY (id),
    KEY idx_play_sessions_user (user_id),
    KEY idx_play_sessions_game (game_id),
    KEY idx_play_sessions_started (started_at),
    CONSTRAINT fk_play_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_play_sessions_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 11. PROGRESO / CLOUD SAVE
-- Guardado automático de partidas (JSON) vinculado al usuario
-- ------------------------------------------------------------
CREATE TABLE saved_games (
    user_id    BIGINT UNSIGNED NOT NULL,
    game_id    BIGINT UNSIGNED NOT NULL,
    data       JSON NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, game_id),
    CONSTRAINT fk_saved_games_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_games_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 12. TOKENS DE SEGURIDAD
-- ------------------------------------------------------------

-- Refresh Tokens (rotación de JWT)
CREATE TABLE refresh_tokens (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_tokens_token (token),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Recuperación de contraseña
CREATE TABLE password_reset_tokens (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_password_reset_tokens_token (token),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Verificación de email
CREATE TABLE email_verification_tokens (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_email_verification_tokens_token (token),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- DATOS INICIALES (SEED)
-- ============================================================

-- Roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER',  'Usuario registrado con biblioteca propia'),
    ('ROLE_ADMIN', 'Administrador: sube, edita y publica juegos');

-- Categorías
INSERT INTO categories (name, slug) VALUES
    ('Acción',  'accion'),
    ('Puzzle',  'puzzle'),
    ('Carreras','carreras'),
    ('Deporte', 'deporte'),
    ('Arcade',  'arcade'),
    ('Aventura','aventura');
