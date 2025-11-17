package ee.grouply.backend.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final Path uploadRoot = Path.of("uploads").toAbsolutePath().normalize();

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file,
                                    @AuthenticationPrincipal UserDetails user) throws IOException {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }

        Files.createDirectories(uploadRoot);

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);
        String filename = System.currentTimeMillis() + "-" + Math.abs(original.hashCode()) + ext;

        Path target = uploadRoot.resolve(filename);
        try (var in = file.getInputStream()) {
            Files.copy(in, target);
        }

        // WebConfig serves /files/** from uploads/
        String publicPath = "/files/" + filename;
        return ResponseEntity.ok(Map.of("url", publicPath));
    }
}
