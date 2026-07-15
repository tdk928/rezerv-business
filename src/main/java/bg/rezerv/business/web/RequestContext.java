package bg.rezerv.business.web;

import bg.rezerv.business.web.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/** Context от gateway headers (REZERV.md §2.3) — без повторна JWT валидация. */
public record RequestContext(Long userId, List<String> roles, Long companyId) {

    public static final String ATTRIBUTE = "rezerv.requestContext";

    public static RequestContext from(HttpServletRequest request) {
        Long userId = parseLongHeader(request.getHeader(ContextHeaders.USER_ID));
        List<String> roles = parseRoles(request.getHeader(ContextHeaders.USER_ROLES));
        Long companyId = parseLongHeader(request.getHeader(ContextHeaders.COMPANY_ID));
        return new RequestContext(userId, roles, companyId);
    }

    public boolean isAuthenticated() {
        return userId != null;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    private static Long parseLongHeader(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<String> parseRoles(String header) {
        if (header == null || header.isBlank()) {
            return List.of();
        }
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static RequestContext requireAuthenticated(HttpServletRequest request) {
        RequestContext ctx = Optional.ofNullable((RequestContext) request.getAttribute(ATTRIBUTE))
                .orElseGet(() -> from(request));
        if (!ctx.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Изисква се автентикация");
        }
        return ctx;
    }
}
