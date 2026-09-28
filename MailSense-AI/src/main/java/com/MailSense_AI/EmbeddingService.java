package com.MailSense_AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.embed.url}")
    private String embedUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public EmbeddingService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    /** taskType: RETRIEVAL_DOCUMENT for stored emails, RETRIEVAL_QUERY for the incoming email. */
    public double[] embed(String text, String taskType) {
        String clipped = text.length() > 6000 ? text.substring(0, 6000) : text;

        Map<String, Object> body = Map.of(
                "content", Map.of("parts", List.of(Map.of("text", clipped))),
                "taskType", taskType,
                "outputDimensionality", 768
        );

        try {
            String json = webClient.post()
                    .uri(embedUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode values = mapper.readTree(json).path("embedding").path("values");
            double[] vec = new double[values.size()];
            for (int i = 0; i < vec.length; i++) vec[i] = values.get(i).asDouble();
            return vec;
        } catch (Exception e) {
            throw new RuntimeException("Embedding failed: " + e.getMessage(), e);
        }
    }
}