-- ============================================================
-- ARCADIA · DATOS DE PRUEBA
-- Ejecutar con la BD ya inicializada:
--   docker compose exec -T mysql mysql -u arcadia -pArcadia2024 arcadia < db/seed_test_data.sql
-- (ajusta usuario/contraseña según tu .env)
--
-- Es IDEMPOTENTE: se puede ejecutar varias veces sin duplicar.
--
-- CREDENCIALES:
--   admin  / admin123      (ROLE_ADMIN)
--   gamer1 / password123   (ROLE_USER)
--   gamer2 / password123   (ROLE_USER)
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- USUARIOS
-- ------------------------------------------------------------
INSERT IGNORE INTO users (nickname, email, password_hash, email_verified, enabled, created_at, updated_at) VALUES
    ('admin',  'admin@arcadia.test',  '$2b$12$Q3aqRb6EiOyFZIFYN6o0jucdHBPl.jI4sQIS.ElxEP965VYz1WY3W', TRUE, TRUE, NOW(), NOW()),
    ('gamer1', 'gamer1@arcadia.test', '$2b$12$2.o6KnwrDoLaHskpp7zIgejtGbkYfcfi6/95dCwdSsNetEEdUUYFW', TRUE, TRUE, NOW(), NOW()),
    ('gamer2', 'gamer2@arcadia.test', '$2b$12$2.o6KnwrDoLaHskpp7zIgejtGbkYfcfi6/95dCwdSsNetEEdUUYFW', TRUE, TRUE, NOW(), NOW());

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
    ((SELECT id FROM users WHERE nickname = 'admin'),  (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')),
    ((SELECT id FROM users WHERE nickname = 'admin'),  (SELECT id FROM roles WHERE name = 'ROLE_USER')),
    ((SELECT id FROM users WHERE nickname = 'gamer1'), (SELECT id FROM roles WHERE name = 'ROLE_USER')),
    ((SELECT id FROM users WHERE nickname = 'gamer2'), (SELECT id FROM roles WHERE name = 'ROLE_USER'));

-- ------------------------------------------------------------
-- JUEGOS
-- snake-runner, puzzle-blocks y moto-rush son jugables
-- (tienen ficheros reales en backend/uploads/games/{slug}/)
-- ------------------------------------------------------------
INSERT IGNORE INTO games
    (title, slug, description, file_path, file_size, version, thumbnail_path, cover_url,
     category_id, is_public, is_hidden, created_at, updated_at) VALUES
    ('Snake Runner',
     'snake-runner',
     'Clásico Snake con dificultad creciente. Come manzanas, crece y no choques contra la pared.',
     '/uploads/games/snake-runner/index.html', 4192, '1.0',
     '/uploads/games/snake-runner/thumbnail.png',
     '/uploads/games/snake-runner/cover.png',
     (SELECT id FROM categories WHERE slug = 'arcade'), TRUE, FALSE, NOW(), NOW()),

    ('Puzzle Blocks',
     'puzzle-blocks',
     'Memoria y lógica: voltea fichas y encuentra las parejas con el mínimo de intentos.',
     '/uploads/games/puzzle-blocks/index.html', 3481, '1.1',
     '/uploads/games/puzzle-blocks/thumbnail.png',
     '/uploads/games/puzzle-blocks/cover.png',
     (SELECT id FROM categories WHERE slug = 'puzzle'), TRUE, FALSE, NOW(), NOW()),

    ('Moto Rush',
     'moto-rush',
     'Esquiva el tráfico en la autopista y consigue la mayor puntuación posible.',
     '/uploads/games/moto-rush/index.html', 5242, '1.0',
     '/uploads/games/moto-rush/thumbnail.png',
     '/uploads/games/moto-rush/cover.png',
     (SELECT id FROM categories WHERE slug = 'carreras'), TRUE, FALSE, NOW(), NOW()),

    -- Borrador: no aparece en el catálogo público (prueba del listado admin)
    ('Super Platformer',
     'super-platformer',
     'Juego de plataformas aún en desarrollo.',
     '/uploads/games/super-platformer/index.html', 2048, '0.9',
     NULL, NULL,
     (SELECT id FROM categories WHERE slug = 'accion'), FALSE, FALSE, NOW(), NOW()),

    -- Oculto: publicado pero con soft-delete (prueba de "Ocultar (soft)")
    ('Mystery Rooms',
     'mystery-rooms',
     'Aventura de escape room retirada temporalmente.',
     '/uploads/games/mystery-rooms/index.html', 1536, '1.0',
     NULL, NULL,
     (SELECT id FROM categories WHERE slug = 'aventura'), TRUE, TRUE, NOW(), NOW());

-- ------------------------------------------------------------
-- VERSIONES DE JUEGO (historial)
-- ------------------------------------------------------------
INSERT IGNORE INTO game_versions (game_id, version, file_path, release_notes, uploaded_at) VALUES
    ((SELECT id FROM games WHERE slug = 'snake-runner'),   '1.0', '/uploads/games/snake-runner/index.html',  'Primera versión', NOW()),
    ((SELECT id FROM games WHERE slug = 'puzzle-blocks'),  '1.0', '/uploads/games/puzzle-blocks/index.html', 'Versión inicial', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    ((SELECT id FROM games WHERE slug = 'puzzle-blocks'),  '1.1', '/uploads/games/puzzle-blocks/index.html', 'Más parejas y contador de intentos', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    ((SELECT id FROM games WHERE slug = 'moto-rush'),      '1.0', '/uploads/games/moto-rush/index.html',     'Primera versión', NOW());

-- ------------------------------------------------------------
-- IMÁGENES (galería estilo Steam)
-- ------------------------------------------------------------
INSERT IGNORE INTO game_images (game_id, image_url, position, created_at) VALUES
    ((SELECT id FROM games WHERE slug = 'snake-runner'),  '/uploads/games/snake-runner/cover.png',   0, NOW()),
    ((SELECT id FROM games WHERE slug = 'snake-runner'),  '/uploads/games/snake-runner/thumbnail.png', 1, NOW()),
    ((SELECT id FROM games WHERE slug = 'puzzle-blocks'), '/uploads/games/puzzle-blocks/cover.png',   0, NOW()),
    ((SELECT id FROM games WHERE slug = 'moto-rush'),     '/uploads/games/moto-rush/cover.png',       0, NOW());

-- ------------------------------------------------------------
-- LOGROS
-- ------------------------------------------------------------
INSERT IGNORE INTO achievements (game_id, title, description, icon, points, hidden) VALUES
    ((SELECT id FROM games WHERE slug = 'snake-runner'), 'Primera manzana',   'Come tu primera manzana',   'apple',   10, FALSE),
    ((SELECT id FROM games WHERE slug = 'snake-runner'), 'Longitud 10',       'Alcanza 10 puntos de longitud', 'ruler', 25, FALSE),
    ((SELECT id FROM games WHERE slug = 'snake-runner'), 'Velocista',         'Supera 1 minuto de juego',  'zap',     50, FALSE),
    ((SELECT id FROM games WHERE slug = 'snake-runner'), 'Serpiente dorada',  'Logro secreto por puntuación legendaria', 'trophy', 100, TRUE),
    ((SELECT id FROM games WHERE slug = 'puzzle-blocks'),'Primera pareja',    'Encuentra tu primera pareja', 'heart',  10, FALSE),
    ((SELECT id FROM games WHERE slug = 'puzzle-blocks'),'Memoria de elefante','Completa el tablero en menos de 30 intentos', 'brain', 50, TRUE);

-- ------------------------------------------------------------
-- BIBLIOTECA PERSONAL
-- ------------------------------------------------------------
INSERT IGNORE INTO library_items
    (user_id, game_id, added_at, last_played_at, time_played_seconds, status, rating) VALUES
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 1 HOUR), 7200, 'PLAYING', 4),
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'puzzle-blocks'),
     DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 3600, 'COMPLETED', 5),
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'moto-rush'),
     DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 900, 'ABANDONED', 2),
    ((SELECT id FROM users WHERE nickname = 'gamer2'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 3 HOUR), 5400, 'PLAYING', NULL);

-- ------------------------------------------------------------
-- FAVORITOS
-- ------------------------------------------------------------
INSERT IGNORE INTO favorites (user_id, game_id, created_at) VALUES
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'snake-runner'), NOW()),
    ((SELECT id FROM users WHERE nickname = 'gamer2'),
     (SELECT id FROM games WHERE slug = 'moto-rush'), NOW());

-- ------------------------------------------------------------
-- RESEÑAS
-- ------------------------------------------------------------
INSERT IGNORE INTO reviews (user_id, game_id, rating, comment, created_at, updated_at) VALUES
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     4, 'Adictivo, pero a partir del nivel 5 va muy rápido. Muy divertido.', NOW(), NOW()),
    ((SELECT id FROM users WHERE nickname = 'gamer2'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     5, 'El clásico perfecto. Me encanta.', NOW(), NOW()),
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'puzzle-blocks'),
     5, 'Ideal para entrenar la memoria.', NOW(), NOW());

-- ------------------------------------------------------------
-- SESIONES DE JUEGO (alimentan el ranking "Más jugados")
-- ------------------------------------------------------------
INSERT IGNORE INTO play_sessions (user_id, game_id, started_at, ended_at, duration_seconds) VALUES
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 1800),
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 5400),
    ((SELECT id FROM users WHERE nickname = 'gamer2'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW(), 3600),
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'puzzle-blocks'),
     DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 2400),
    ((SELECT id FROM users WHERE nickname = 'gamer2'),
     (SELECT id FROM games WHERE slug = 'moto-rush'),
     DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 1200);

-- ------------------------------------------------------------
-- LOGROS DESBLOQUEADOS
-- ------------------------------------------------------------
INSERT IGNORE INTO user_achievements (user_id, achievement_id, unlocked_at) VALUES
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM achievements WHERE title = 'Primera manzana'),
     DATE_SUB(NOW(), INTERVAL 5 DAY)),
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM achievements WHERE title = 'Longitud 10'),
     DATE_SUB(NOW(), INTERVAL 4 DAY)),
    ((SELECT id FROM users WHERE nickname = 'gamer2'),
     (SELECT id FROM achievements WHERE title = 'Primera manzana'),
     DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ------------------------------------------------------------
-- CLOUD SAVE (progreso)
-- ------------------------------------------------------------
INSERT IGNORE INTO saved_games (user_id, game_id, data, updated_at) VALUES
    ((SELECT id FROM users WHERE nickname = 'gamer1'),
     (SELECT id FROM games WHERE slug = 'snake-runner'),
     JSON_OBJECT('level', 7, 'score', 540, 'bestScore', 540),
     NOW());

SET FOREIGN_KEY_CHECKS = 1;
