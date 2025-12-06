package ee.grouply.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
        log.info("CloudinaryService initialized");
    }

    /**
     * Upload image to Cloudinary
     * @param file Multipart file from frontend
     * @return Cloudinary secure URL
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Starting upload: filename={}, size={}, contentType={}", 
                 file.getOriginalFilename(), file.getSize(), file.getContentType());

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }

        try {
            // Upload to Cloudinary (LIHTNE VERSIOON - ilma transformation'ita)
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "grouply",
                "resource_type", "image",
                "quality", "auto:good"     // Auto optimize quality
            ));

            String url = (String) uploadResult.get("secure_url");
            log.info("Upload successful: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            throw new IOException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete image from Cloudinary
     * @param publicId Cloudinary public ID (extracted from URL)
     */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Extract public ID from Cloudinary URL
     * Example: https://res.cloudinary.com/demo/image/upload/v1234/grouply/abc123.jpg
     * Returns: grouply/abc123
     */
    public String extractPublicId(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }
        
        String[] parts = url.split("/upload/");
        if (parts.length < 2) return null;
        
        String afterUpload = parts[1];
        String[] segments = afterUpload.split("/");
        
        StringBuilder publicId = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].startsWith("v") && i == 0) continue;
            if (i > 0) publicId.append("/");
            publicId.append(segments[i]);
        }
        
        String result = publicId.toString();
        int lastDot = result.lastIndexOf('.');
        if (lastDot > 0) {
            result = result.substring(0, lastDot);
        }
        
        return result;
    }
}