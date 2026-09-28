package com.MailSense_AI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class StyleStore {

    public record Entry(String text, double[] vector) {}

    @Value("${style.store.path:./style-store.json}")
    private String path;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Entry> entries = new ArrayList<>();

    @PostConstruct
    public synchronized void load() {
        File f = new File(path);
        if (!f.exists()) return;
        try {
            entries.addAll(mapper.readValue(f, new TypeReference<List<Entry>>() {}));
        } catch (IOException e) {
            System.err.println("Could not load style store: " + e.getMessage());
        }
    }

    public synchronized boolean contains(String text) {
        return entries.stream().anyMatch(e -> e.text().equals(text));
    }

    public synchronized void add(List<Entry> newEntries) {
        if (newEntries.isEmpty()) return;
        entries.addAll(newEntries);
        try {
            mapper.writeValue(new File(path), entries);
        } catch (IOException e) {
            System.err.println("Could not save style store: " + e.getMessage());
        }
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
        new File(path).delete();
    }

    public synchronized List<String> topK(double[] query, int k) {
        return entries.stream()
                .sorted(Comparator.comparingDouble((Entry e) -> -cosine(query, e.vector())))
                .limit(k)
                .map(Entry::text)
                .toList();
    }

    private static double cosine(double[] a, double[] b) {
        double dot = 0, na = 0, nb = 0;
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-12);
    }
}