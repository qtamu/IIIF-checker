package iiifchecker.iiifchecker; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IIIFService {
    private HttpClient client;
    private ObjectMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(IIIFService.class);

    public IIIFService() {
        this.client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        this.mapper = new ObjectMapper();
    }

    public CompletableFuture<Void> validateAndDisplayManifest(String manifestUrl, String indent) {
        return fetchManifest(manifestUrl)
            .thenCompose(json -> parseAndProcessManifest(json, indent))
            .exceptionally(e -> {
                logger.error("Failed to process manifest URL: {}", manifestUrl, e);
                return null;
            });
    }

    public CompletableFuture<String> fetchManifest(String manifestUrl) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(manifestUrl))
            .GET()
            .timeout(Duration.ofSeconds(60))
            .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .exceptionally(e -> {
                logger.error("Error fetching manifest from URL: {}", manifestUrl, e);
                return null; // Return null to signal an error in fetching
            });
    }

    private CompletableFuture<Void> parseAndProcessManifest(String json, String indent) {
        if (json == null) {
            logger.warn("Received null JSON for manifest");
            return CompletableFuture.completedFuture(null); // Handle null JSON appropriately
        }
        try {
            JsonNode rootNode = mapper.readTree(json);
            return processManifestNode(rootNode, indent);
        } catch (Exception e) {
            logger.error("Error parsing JSON or validating manifest: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(null);
        }
    }

    public CompletableFuture<Void> processManifestNode(JsonNode node, String indent) {
        String id = node.path("@id").asText();
        String type = node.path("@type").asText();
        String label = node.path("label").asText();

        // Validate the URL before proceeding
        if (!isValidUri(id)) {
            logger.error("Invalid URI: {}", id);
            return CompletableFuture.completedFuture(null);
        }

        logger.info("{}ID: {}, Type: {}, Label: {}", indent, id, type, label);
        CompletableFuture<String> statusFuture = validateUrl(id);

        List<CompletableFuture<Void>> childFutures = new ArrayList<>();
        // processChildNodes(node, "items", indent, childFutures);
        processChildNodes(node, "manifests", indent, childFutures);

        return CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[0]))
            .thenCompose(v -> statusFuture.thenAccept(status -> logger.info("{}Status for {}: {}", indent, id, status)));
    }

    private void processChildNodes(JsonNode node, String fieldName, String indent, List<CompletableFuture<Void>> futures) {
        if (node.has(fieldName)) {
            node.get(fieldName).forEach(item -> {
                if (item.has("@id") && isValidUri(item.get("@id").asText())) {
                    futures.add(validateAndDisplayManifest(item.get("@id").asText(), indent + "  "));
                }
            });
        }
    }

    private boolean isValidUri(String uri) {
        try {
            URI.create(uri);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<String> validateUrl(String url) {
        System.out.println("URL: + " + url);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(60))
            .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .thenApply(response -> "HTTP Status: " + response.statusCode())
            .exceptionally(e -> "Error: " + e.getMessage());
    }
}
