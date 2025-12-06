package ee.grouply.backend.api;

import ee.grouply.backend.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteImage(@RequestParam("url") String url) {
        try {
            String publicId = cloudinaryService.extractPublicId(url);
            if (publicId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid Cloudinary URL"));
            }

            cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok(Map.of("message", "Image deleted"));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }
}
