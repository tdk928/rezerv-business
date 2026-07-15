package bg.rezerv.business.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CasProperties.class)
public class CasClientConfig {

    @Bean
    RestClient casRestClient(CasProperties casProperties) {
        return RestClient.builder()
                .baseUrl(casProperties.baseUrl())
                .build();
    }
}
