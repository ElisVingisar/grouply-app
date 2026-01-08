package ee.grouply.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class GrouplyBackendApplication {

    public static void main(String[] args) {

    // Load environment variables from root .env file
    Dotenv dotenv = Dotenv.configure()
            .directory("../")
            .ignoreIfMissing()
            .load();

    dotenv.entries().forEach(e ->
            System.setProperty(e.getKey(), e.getValue())
    );

    SpringApplication.run(GrouplyBackendApplication.class, args);
}
}
