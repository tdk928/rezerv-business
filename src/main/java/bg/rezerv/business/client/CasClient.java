package bg.rezerv.business.client;

import bg.rezerv.business.web.error.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Service-to-service клиент към rezerv-cas (/internal/**). */
@Component
public class CasClient {

    private static final Logger log = LoggerFactory.getLogger(CasClient.class);

    private final RestClient restClient;

    public CasClient(RestClient casRestClient) {
        this.restClient = casRestClient;
    }

    /**
     * Задава company_id + BUSINESS_OWNER на user след успешен onboarding.
     * @see bg.rezerv.cas.web.InternalUserController
     */
    public void assignCompany(Long userId, Long companyId) {
        try {
            restClient.post()
                    .uri("/internal/users/{userId}/assign-company", userId)
                    .body(new AssignCompanyBody(companyId))
                    .retrieve()
                    .toBodilessEntity();
            log.info("CAS assign-company ok userId={} companyId={}", userId, companyId);
        } catch (RestClientException ex) {
            log.error("CAS assign-company failed userId={} companyId={}", userId, companyId, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_ASSIGN_FAILED",
                    "Неуспешно свързване с акаунта — опитайте отново");
        }
    }

    private record AssignCompanyBody(Long companyId) {
    }
}
