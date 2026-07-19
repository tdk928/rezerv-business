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
import org.springframework.web.client.RestClientResponseException;

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

    public UserSummary findByEmail(String email) {
        try {
            UserSummary user = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/users/by-email")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .body(UserSummary.class);
            if (user == null || user.id() == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "Потребител с този email не е намерен");
            }
            return user;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "Потребител с този email не е намерен");
            }
            log.error("CAS findByEmail failed email={}", email, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_LOOKUP_FAILED",
                    "Неуспешно търсене на потребител по email");
        } catch (RestClientException ex) {
            log.error("CAS findByEmail failed email={}", email, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_LOOKUP_FAILED",
                    "Неуспешно търсене на потребител по email");
        }
    }

    /** Създава нов CAS user с STAFF + company membership. */
    public UserSummary createStaffUser(String email,
                                       String password,
                                       String firstName,
                                       String lastName,
                                       String phone,
                                       Long companyId) {
        try {
            UserSummary user = restClient.post()
                    .uri("/internal/users/create-staff")
                    .body(new CreateStaffBody(email, password, firstName, lastName, phone, companyId))
                    .retrieve()
                    .body(UserSummary.class);
            if (user == null || user.id() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_CREATE_FAILED",
                        "Неуспешно създаване на служителски акаунт");
            }
            log.info("CAS create-staff ok userId={} companyId={}", user.id(), companyId);
            return user;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 409) {
                throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                        "Потребител с този email вече съществува");
            }
            log.error("CAS create-staff failed email={}", email, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_CREATE_FAILED",
                    "Неуспешно създаване на служителски акаунт");
        } catch (RestClientException ex) {
            log.error("CAS create-staff failed email={}", email, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_CREATE_FAILED",
                    "Неуспешно създаване на служителски акаунт");
        }
    }

    /** Добавя STAFF роля + company membership в CAS. */
    public void assignStaff(Long userId, Long companyId) {
        try {
            restClient.post()
                    .uri("/internal/users/{userId}/assign-staff", userId)
                    .body(new AssignCompanyBody(companyId))
                    .retrieve()
                    .toBodilessEntity();
            log.info("CAS assign-staff ok userId={} companyId={}", userId, companyId);
        } catch (RestClientException ex) {
            log.error("CAS assign-staff failed userId={} companyId={}", userId, companyId, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "CAS_ASSIGN_FAILED",
                    "Неуспешно свързване на служителя с акаунта");
        }
    }

    public record UserSummary(Long id, String email, String firstName, String lastName) {
    }

    private record AssignCompanyBody(Long companyId) {
    }

    private record CreateStaffBody(
            String email,
            String password,
            String firstName,
            String lastName,
            String phone,
            Long companyId) {
    }

    private record LookupBody(List<Long> ids) {
    }
}
