package ee.grouply.backend.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    @Value("${grouply.upload-dir}")
    private String uploadDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        Path projectRoot = Paths.get("").toAbsolutePath();
        Path dir = projectRoot.resolve(uploadDir).normalize();
        Files.createDirectories(dir);

        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot != -1) ext = original.substring(dot);

        String filename = UUID.randomUUID() + (ext.isBlank() ? "" : ext.toLowerCase());
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/files/" + filename;
        return Map.of("url", publicUrl);
    }
}
