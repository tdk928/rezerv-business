package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.SalonPhoto;

public record SalonPhotoResponse(Long id, Long salonId, String url, Integer position) {

    public static SalonPhotoResponse from(SalonPhoto photo) {
        return new SalonPhotoResponse(photo.getId(), photo.getSalonId(), photo.getUrl(), photo.getPosition());
    }
}
