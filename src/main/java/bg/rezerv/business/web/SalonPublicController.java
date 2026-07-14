package bg.rezerv.business.web;

import bg.rezerv.business.service.SalonQueryService;
import bg.rezerv.business.web.dto.PageResponse;
import bg.rezerv.business.web.dto.SalonCardResponse;
import bg.rezerv.business.web.dto.SalonDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Публично търсене и детайл на салони — през gateway: /api/business/public/salons. */
@RestController
@RequestMapping("/business/public/salons")
public class SalonPublicController {

    private final SalonQueryService salonQueryService;

    public SalonPublicController(SalonQueryService salonQueryService) {
        this.salonQueryService = salonQueryService;
    }

    @GetMapping
    public PageResponse<SalonCardResponse> search(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return salonQueryService.search(cityId, categoryId, q, page, Math.min(size, 50));
    }

    @GetMapping("/{id}")
    public SalonDetailResponse getSalon(@PathVariable Long id) {
        return salonQueryService.getSalon(id);
    }
}
