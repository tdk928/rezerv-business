package bg.rezerv.business.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rezerv.cas")
public record CasProperties(String baseUrl) {
}
