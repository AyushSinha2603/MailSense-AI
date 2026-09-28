package com.MailSense_AI;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/style")
@CrossOrigin(origins = "*")
public class StyleController {

    public record IngestRequest(List<String> emails) {}

    private final EmbeddingService embeddingService;
    private final StyleStore styleStore;

    public StyleController(EmbeddingService embeddingService, StyleStore styleStore) {
        this.embeddingService = embeddingService;
        this.styleStore = styleStore;
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest(@RequestBody IngestRequest req) {
        List<StyleStore.Entry> added = new ArrayList<>();
        int skipped = 0;
        try {
            for (String raw : req.emails()) {
                String text = raw == null ? "" : raw.trim();
                if (text.length() < 20 || styleStore.contains(text)) {
                    skipped++;
                    continue;
                }
                added.add(new StyleStore.Entry(text, embeddingService.embed(text, "RETRIEVAL_DOCUMENT")));
            }
        } finally {
            styleStore.add(added); // keep whatever succeeded, even if a later one failed
        }
        return Map.of("added", added.size(), "skipped", skipped, "total", styleStore.size());
    }

    @GetMapping("/count")
    public Map<String, Object> count() {
        return Map.of("total", styleStore.size());
    }

    @DeleteMapping
    public Map<String, Object> clear() {
        styleStore.clear();
        return Map.of("total", 0);
    }
}