package bg.rezerv.business;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RezervBusinessApplicationTests {

    @Test
    void contextLoads() {
        // Контекстът стартира срещу реален PostgreSQL (Testcontainers) + Flyway миграциите минават.
    }
}
