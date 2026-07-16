package bg.rezerv.business.service;

import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonPhoto;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.repository.SalonPhotoRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.web.dto.CityResponse;
import bg.rezerv.business.web.dto.PageResponse;
import bg.rezerv.business.web.dto.SalonCardResponse;
import bg.rezerv.business.web.dto.SalonDetailResponse;
import bg.rezerv.business.web.error.ApiException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SalonQueryService {

    private final SalonRepository salonRepository;
    private final SalonPhotoRepository salonPhotoRepository;
    private final SalonServiceItemRepository salonServiceItemRepository;

    public SalonQueryService(SalonRepository salonRepository,
                             SalonPhotoRepository salonPhotoRepository,
                             SalonServiceItemRepository salonServiceItemRepository) {
        this.salonRepository = salonRepository;
        this.salonPhotoRepository = salonPhotoRepository;
        this.salonServiceItemRepository = salonServiceItemRepository;
    }

    public PageResponse<SalonCardResponse> search(Long cityId, Long categoryId, String q, int page, int size) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<Salon> salons = salonRepository.search(cityId, categoryId, query, PageRequest.of(page, size));

        List<Long> salonIds = salons.getContent().stream().map(Salon::getId).toList();
        Map<Long, String> mainPhotos = mainPhotoPerSalon(salonIds);
        Map<Long, BigDecimal> minPrices = salonServiceItemRepository.findMinPrices(salonIds).stream()
                .collect(Collectors.toMap(
                        SalonServiceItemRepository.MinPricePerSalon::getSalonId,
                        SalonServiceItemRepository.MinPricePerSalon::getMinPrice));

        List<SalonCardResponse> cards = salons.getContent().stream()
                .map(s -> new SalonCardResponse(
                        s.getId(),
                        s.getName(),
                        CityResponse.from(s.getCity()),
                        s.getAddress(),
                        s.getRatingAvg(),
                        s.getRatingCount(),
                        mainPhotos.get(s.getId()),
                        minPrices.get(s.getId())))
                .toList();
        return PageResponse.of(salons, cards);
    }

    public SalonDetailResponse getSalon(Long id) {
        Salon salon = salonRepository.findWithCityById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "SALON_NOT_FOUND", "Салонът не е намерен"));

        List<String> photos = salonPhotoRepository.findBySalonIdOrderByPositionAsc(id).stream()
                .map(SalonPhoto::getUrl)
                .toList();

        List<SalonDetailResponse.ServiceGroup> serviceGroups = groupServicesByCategory(
                salonServiceItemRepository.findBySalonIdAndActiveTrueOrderByNameAsc(id));

        return new SalonDetailResponse(
                salon.getId(),
                salon.getName(),
                salon.getDescription(),
                CityResponse.from(salon.getCity()),
                salon.getAddress(),
                salon.getLat(),
                salon.getLng(),
                salon.getEmail(),
                salon.getPhone(),
                salon.getRatingAvg(),
                salon.getRatingCount(),
                photos,
                serviceGroups);
    }

    private Map<Long, String> mainPhotoPerSalon(List<Long> salonIds) {
        if (salonIds.isEmpty()) {
            return Map.of();
        }
        return salonPhotoRepository.findBySalonIdInOrderByPositionAsc(salonIds).stream()
                .collect(Collectors.toMap(SalonPhoto::getSalonId, SalonPhoto::getUrl, (first, later) -> first));
    }

    private List<SalonDetailResponse.ServiceGroup> groupServicesByCategory(List<SalonServiceItem> services) {
        Map<Long, List<SalonServiceItem>> byCategory = services.stream()
                .collect(Collectors.groupingBy(
                        sv -> sv.getCategory().getId(), LinkedHashMap::new, Collectors.toList()));

        return byCategory.values().stream()
                .map(items -> new SalonDetailResponse.ServiceGroup(
                        items.getFirst().getCategory().getId(),
                        items.getFirst().getCategory().getName(),
                        items.getFirst().getCategory().getSlug(),
                        items.stream()
                                .map(sv -> new SalonDetailResponse.ServiceItem(
                                        sv.getId(), sv.getName(), sv.getDurationMin(), sv.getPrice()))
                                .toList()))
                .sorted(Comparator.comparing(SalonDetailResponse.ServiceGroup::categoryName))
                .toList();
    }
}
