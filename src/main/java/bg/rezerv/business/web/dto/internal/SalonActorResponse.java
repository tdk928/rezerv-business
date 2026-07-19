package bg.rezerv.business.web.dto.internal;

/** Дали user е owner на фирмата на салона и/или активен staff там. */
public record SalonActorResponse(
        Long salonId,
        Long userId,
        boolean owner,
        Long staffMemberId) {
}
