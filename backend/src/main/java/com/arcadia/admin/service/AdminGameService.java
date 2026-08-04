package com.arcadia.admin.service;

import com.arcadia.admin.dto.response.AdminGameResponse;
import com.arcadia.common.exception.BadRequestException;
import com.arcadia.common.exception.ConflictException;
import com.arcadia.common.exception.GameNotFoundException;
import com.arcadia.common.exception.ResourceNotFoundException;
import com.arcadia.common.util.SlugUtils;
import com.arcadia.entity.Category;
import com.arcadia.entity.Game;
import com.arcadia.entity.GameVersion;
import com.arcadia.game.mapper.GameMapper;
import com.arcadia.repository.CategoryRepository;
import com.arcadia.repository.GameRepository;
import com.arcadia.repository.GameVersionRepository;
import com.arcadia.storage.StorageService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AdminGameService {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final GameVersionRepository gameVersionRepository;
    private final StorageService storageService;
    private final GameMapper gameMapper;

    public AdminGameService(GameRepository gameRepository,
                            CategoryRepository categoryRepository,
                            GameVersionRepository gameVersionRepository,
                            StorageService storageService,
                            GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
        this.gameVersionRepository = gameVersionRepository;
        this.storageService = storageService;
        this.gameMapper = gameMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminGameResponse> listAll() {
        return gameRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(gameMapper::toAdmin)
                .toList();
    }

    @Transactional
    public AdminGameResponse create(MultipartFile zip, String title, String description, Long categoryId,
                                    String version, String releaseNotes, MultipartFile thumbnail,
                                    MultipartFile cover, Boolean isPublic) {
        String gameTitle = requireText(title, "El título es obligatorio");
        String ver = normalizeVersion(version);
        String slug = uniqueSlug(SlugUtils.toSlug(gameTitle));
        Path dir = storageService.gameDir(slug);
        long fileSize = storageService.extractGameZip(zip, dir);
        Category category = resolveCategory(categoryId);

        Game game = Game.builder()
                .title(gameTitle)
                .slug(slug)
                .description(description)
                .filePath(gamePath(slug, "index.html"))
                .fileSize(fileSize)
                .version(ver)
                .category(category)
                .isPublic(isPublic != null && isPublic)
                .isHidden(false)
                .build();

        applyImages(game, dir, thumbnail, cover);
        gameRepository.save(game);
        gameVersionRepository.save(GameVersion.builder()
                .game(game)
                .version(ver)
                .filePath(game.getFilePath())
                .releaseNotes(releaseNotes)
                .build());
        return gameMapper.toAdmin(game);
    }

    @Transactional
    public AdminGameResponse update(Long id, MultipartFile zip, String title, String description,
                                    Long categoryId, String version, String releaseNotes,
                                    MultipartFile thumbnail, MultipartFile cover,
                                    Boolean isPublic, Boolean isHidden) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Juego no encontrado"));

        if (hasText(title)) {
            game.setTitle(title.trim());
        }
        if (description != null) {
            game.setDescription(description.isEmpty() ? null : description);
        }
        if (categoryId != null) {
            game.setCategory(resolveCategory(categoryId));
        }
        if (isPublic != null) {
            game.setPublic(isPublic);
        }
        if (isHidden != null) {
            game.setHidden(isHidden);
        }

        if (zip != null && !zip.isEmpty()) {
            String ver = requireText(version, "Indica la versión nueva de la subida");
            ver = normalizeVersion(ver);
            if (gameVersionRepository.findByGameIdAndVersion(game.getId(), ver).isPresent()) {
                throw new ConflictException("Ya existe la versión " + ver + " para este juego");
            }
            Path dir = storageService.gameDir(game.getSlug());
            long fileSize = storageService.extractGameZip(zip, dir);
            game.setFilePath(gamePath(game.getSlug(), "index.html"));
            game.setFileSize(fileSize);
            game.setVersion(ver);
            gameVersionRepository.save(GameVersion.builder()
                    .game(game)
                    .version(ver)
                    .filePath(game.getFilePath())
                    .releaseNotes(releaseNotes)
                    .build());
        }

        if (thumbnail != null && !thumbnail.isEmpty() || cover != null && !cover.isEmpty()) {
            Path dir = storageService.gameDir(game.getSlug());
            applyImages(game, dir, thumbnail, cover);
        }

        return gameMapper.toAdmin(gameRepository.save(game));
    }

    @Transactional
    public void delete(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Juego no encontrado"));
        storageService.deleteDirectory(storageService.gameDir(game.getSlug()));
        gameRepository.delete(game);
    }

    private void applyImages(Game game, Path dir, MultipartFile thumbnail, MultipartFile cover) {
        if (thumbnail != null && !thumbnail.isEmpty()) {
            game.setThumbnailPath(gamePath(game.getSlug(), storageService.saveImage(thumbnail, dir, "thumbnail")));
        }
        if (cover != null && !cover.isEmpty()) {
            game.setCoverUrl(gamePath(game.getSlug(), storageService.saveImage(cover, dir, "cover")));
        }
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + categoryId));
    }

    private String uniqueSlug(String base) {
        if (!hasText(base)) {
            base = "juego";
        }
        String slug = base;
        int suffix = 2;
        while (gameRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String normalizeVersion(String version) {
        if (!hasText(version)) {
            return "1.0";
        }
        String ver = version.trim();
        if (!VERSION_PATTERN.matcher(ver).matches()) {
            throw new BadRequestException("Versión no válida: " + version);
        }
        return ver;
    }

    private String gamePath(String slug, String filename) {
        return "/uploads/games/" + slug + "/" + filename;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String requireText(String value, String message) {
        if (!hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }
}
