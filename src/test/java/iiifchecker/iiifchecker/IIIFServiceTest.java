package iiifchecker.iiifchecker; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
// import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("mock")
public class IIIFServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private IIIFService iiifService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFetchManifest() throws Exception {
        String expectedJson = "{\"@id\":\"http://example.com/image\",\"otherField\":\"value\"}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(expectedJson);

        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(mockResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(future);

        CompletableFuture<String> result = iiifService.fetchManifest("http://fakeurl.com");
        assertEquals(expectedJson, result.get());  // Ensure to use .get() to retrieve the actual result
    }

    @Test
    public void testProcessManifestNode() throws Exception {
        String json = "{\"@id\":\"http://example.com/image\", \"@type\":\"sc:Manifest\", \"label\":\"Example Manifest\", \"items\":[]}";
        JsonNode rootNode = new ObjectMapper().readTree(json);

        // Here, we assume processManifestNode is public; if it's not, you might need to adjust the visibility or test it indirectly through another public method.
        CompletableFuture<Void> processFuture = iiifService.processManifestNode(rootNode, "");
        assertNull(processFuture.get());  // Since processing should complete normally without returning a value.

        verify(mockHttpClient).sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.discarding()));
    }

    @Test
    public void testValidateUrl() throws Exception {
        String url = "http://example.com/image";
        HttpResponse<Void> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);

        CompletableFuture<HttpResponse<Void>> future = CompletableFuture.completedFuture(mockResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.discarding())))
                .thenReturn(future);

        CompletableFuture<String> result = iiifService.validateUrl(url);
        assertEquals("HTTP Status: 200", result.get());  // Checking the status code returned from the validation.
    }
}
