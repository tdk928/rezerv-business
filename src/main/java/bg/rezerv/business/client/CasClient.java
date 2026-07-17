package bg.rezerv.business.client;

import bg.rezerv.business.web.error.ApiException;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Service-to-service клиент към rezerv-cas (/internal/**). */
@Component
public class CasClient {

    private static final Logger log = LoggerFactory.getLogger(CasClient.class);
    private static final ParameterizedTypeReference<List<UserSummary>> USER_LIST =
            new ParameterizedTypeReference<>() {};

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

    /** Batch lookup на owner профили по user id. */
    public List<UserSummary> lookupUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        try {
            List<UserSummary> users = restClient.post()
                    .uri("/internal/users/lookup")
                    .body(new LookupBody(List.copyOf(userIds)))
                    .retrieve()
                    .body(USER_LIST);
            return users == null ? List.of() : users;
        } catch (RestClientException ex) {
            log.error("CAS user lookup failed for {} ids", userIds.size(), ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_LOOKUP_FAILED",
                    "Неуспешно зареждане на собствениците");
        }
    }

    public record UserSummary(Long id, String email, String firstName, String lastName) {
    }

    private record AssignCompanyBody(Long companyId) {
    }

    private record LookupBody(List<Long> ids) {
    }
}
