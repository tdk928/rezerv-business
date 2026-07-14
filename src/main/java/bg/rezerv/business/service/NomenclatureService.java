package bg.rezerv.business.service;

import bg.rezerv.business.repository.CityRepository;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.web.dto.CityResponse;
import bg.rezerv.business.web.dto.ServiceCategoryResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NomenclatureService {

    private final CityRepository cityRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    public NomenclatureService(CityRepository cityRepository,
                               ServiceCategoryRepository serviceCategoryRepository) {
        this.cityRepository = cityRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    public List<CityResponse> getCities() {
        return cityRepository.findAllByOrderByNameAsc().stream()
                .map(CityResponse::from)
                .toList();
    }

    public List<ServiceCategoryResponse> getServiceCategories() {
        return serviceCategoryRepository.findAllByOrderByNameAsc().stream()
                .map(ServiceCategoryResponse::from)
                .toList();
    }
}
