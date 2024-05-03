package iiifchecker.iiifchecker; 

// import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/iiif")

public class ManifestController {
    private final IIIFService iiifService;

    public ManifestController(IIIFService iiifService) {
        this.iiifService = iiifService;
    }

    @GetMapping("/validate/{manifestUrl}")
    public CompletableFuture<Void> validateManifest(@PathVariable String manifestUrl) {
        return iiifService.validateAndDisplayManifest(manifestUrl, "");  // Pass an initial indent as empty for the top-level
    }
}
