package bg.rezerv.business.web;

import bg.rezerv.business.service.NomenclatureService;
import bg.rezerv.business.web.dto.CityResponse;
import bg.rezerv.business.web.dto.ServiceCategoryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Публични номенклатури — gateway ги route-ва без JWT (/api/business/public/**). */
@RestController
@RequestMapping("/business/public")
public class NomenclatureController {

    private final NomenclatureService nomenclatureService;

    public NomenclatureController(NomenclatureService nomenclatureService) {
        this.nomenclatureService = nomenclatureService;
    }

    @GetMapping("/cities")
    public List<CityResponse> getCities() {
        return nomenclatureService.getCities();
    }

    @GetMapping("/categories")
    public List<ServiceCategoryResponse> getServiceCategories() {
        return nomenclatureService.getServiceCategories();
    }
}
