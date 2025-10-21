package ee.grouply.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${grouply.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path uploadPath = projectRoot.resolve(uploadDir).normalize();

        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
}

