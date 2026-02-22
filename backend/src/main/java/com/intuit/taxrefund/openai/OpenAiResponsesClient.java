package com.intuit.taxrefund.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OpenAiResponsesClient {

    private final OpenAiProps props;
    private final ObjectMapper om;
    private final RestClient rest;

    public OpenAiResponsesClient(OpenAiProps props, ObjectMapper om) {
        this.props = props;
        this.om = om;
        this.rest = RestClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();
    }

    public boolean isEnabled() {
        return props.apiKey() != null && !props.apiKey().isBlank();
    }

    /**
     * Returns JSON text that matches the schema (Structured Outputs strict).
     */
    public String generateStructuredJson(String developerPrompt, String userPrompt, Map<String, Object> jsonSchema) {
        if (!isEnabled()) throw new IllegalStateException("OpenAI API key not configured");

        Map<String, Object> body = Map.of(
            "model", props.model(),
            "input", new Object[] {
                Map.of("role", "developer", "content", developerPrompt),
                Map.of("role", "user", "content", userPrompt)
            },
            // Structured outputs in Responses API: text.format = { type: json_schema, json_schema: { ... strict ... } }
            "text", Map.of(
                "format", Map.of(
                    "type", "json_schema",
                    "json_schema", jsonSchema
                )
            )
        );

        String raw = rest.post()
            .uri("/responses")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + props.apiKey())
            .body(body)
            .retrieve()
            .body(String.class);

        try {
            JsonNode root = om.readTree(raw);
            // Responses API returns output text in output[].content[].text
            // We’ll extract the first text item.
            JsonNode textNode = root.at("/output/0/content/0/text");
            if (textNode.isMissingNode()) {
                throw new IllegalStateException("Unexpected OpenAI response shape: " + raw);
            }
            return textNode.asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
}