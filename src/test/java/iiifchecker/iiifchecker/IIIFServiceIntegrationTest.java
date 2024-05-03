package iiifchecker.iiifchecker; 

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
// import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ActiveProfiles("real")
@SpringBootTest
public class IIIFServiceIntegrationTest {

    @Autowired
    private IIIFService iiifService;

    @Autowired
    private IIIFConfig iiifConfig;

    @Test
    public void testValidateManifestApis() {
        List<String> apiUrls = iiifConfig.getApis();
        apiUrls.forEach(url -> {
            CompletableFuture<Void> resultsFuture = iiifService.validateAndDisplayManifest(url, "");
            resultsFuture.exceptionally(e -> {
                System.out.println("Error processing URL " + url + ": " + e.getMessage());
                return null; // Handling exception to continue processing
            }).join();  // Wait for the future to complete before moving to the next URL
        });
    }
}
