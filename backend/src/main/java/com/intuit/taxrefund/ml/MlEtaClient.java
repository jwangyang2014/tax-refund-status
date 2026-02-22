package com.intuit.taxrefund.ml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class MlEtaClient {

    private final RestClient rest;
    private final ObjectMapper om;

    public MlEtaClient(MlProps props, ObjectMapper om) {
        this.rest = RestClient.builder()
            .baseUrl(props.baseUrl())
            .build();
        this.om = om;
    }

    public ModelInfo modelInfo() {
        try {
            String raw = rest.get()
                .uri("/model/info")
                .retrieve()
                .body(String.class);

            JsonNode n = om.readTree(raw);
            return new ModelInfo(
                n.path("modelName").asText("unknown"),
                n.path("modelVersion").asText("unknown")
            );
        } catch (Exception e) {
            return new ModelInfo("unknown", "unavailable");
        }
    }

    public PredictResponse predict(Long userId, int taxYear, String status, String filingState, BigDecimal expectedAmount) {
        Map<String, Object> body = Map.of(
            "userId", userId,
            "taxYear", taxYear,
            "status", status,
            "filingState", filingState,
            "expectedAmount", expectedAmount
        );

        String raw = rest.post()
            .uri("/predict")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        try {
            JsonNode n = om.readTree(raw);
            int etaDays = n.path("etaDays").asInt();
            String modelName = n.path("modelName").asText("unknown");
            String modelVersion = n.path("modelVersion").asText("unknown");
            JsonNode features = n.path("features");

            String featuresJson = features.isMissingNode() ? "{}" : om.writeValueAsString(features);

            return new PredictResponse(etaDays, modelName, modelVersion, featuresJson);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse ML response: " + e.getMessage(), e);
        }
    }

    public record PredictResponse(int etaDays, String modelName, String modelVersion, String featuresJson) {}
    public record ModelInfo(String modelName, String modelVersion) {}
}