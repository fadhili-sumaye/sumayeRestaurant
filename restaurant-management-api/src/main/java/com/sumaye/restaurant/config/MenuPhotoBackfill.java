package com.sumaye.restaurant.config;

import com.sumaye.restaurant.model.MenuItem;
import com.sumaye.restaurant.repository.MenuItemRepository;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Attaches the bundled food photos to seeded menu items on every profile (dev and prod).
 * Runs idempotently on startup: existing rows that still have no imageUrl get the
 * matching photo path; populated rows are left untouched. The dev-only DataInitializer
 * cannot do this because prod items were created from SQL, not from the initializer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuPhotoBackfill implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;

    private static final Map<String, String> PHOTO_BY_NAME;

    static {
        PHOTO_BY_NAME = new LinkedHashMap<>();
        PHOTO_BY_NAME.put("pilau kuku", "/images/pilau-kuku.png");
        PHOTO_BY_NAME.put("ugali samaki", "/images/ugali-samaki.png");
        PHOTO_BY_NAME.put("chips kuku", "/images/chips-kuku.png");
        PHOTO_BY_NAME.put("biryani ng'ombe", "/images/biryani-ngombe.png");
        PHOTO_BY_NAME.put("chai ya maziwa", "/images/chai-ya-maziwa.png");
        PHOTO_BY_NAME.put("juisi ya embe", "/images/juisi-ya-embe.png");
        PHOTO_BY_NAME.put("soda", "/images/soda.png");
        PHOTO_BY_NAME.put("maji ya kunywa", "/images/maji-ya-kunywa.png");
        PHOTO_BY_NAME.put("sambusa ya nyama", "/images/sambusa-ya-nyama.png");
        PHOTO_BY_NAME.put("mandazi", "/images/mandazi.png");
    }

    @Override
    public void run(String... args) {
        int updated = 0;
        for (MenuItem item : menuItemRepository.findAll()) {
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                continue;
            }
            String photo = PHOTO_BY_NAME.get(normalize(item.getName()));
            if (photo != null) {
                item.setImageUrl(photo);
                menuItemRepository.save(item);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Backfilled menu photos for {} item(s)", updated);
        }
    }

    private String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}