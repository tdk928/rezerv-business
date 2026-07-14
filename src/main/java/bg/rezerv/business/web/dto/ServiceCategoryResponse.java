package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.ServiceCategory;

public record ServiceCategoryResponse(Long id, String name, String slug, String icon) {

    public static ServiceCategoryResponse from(ServiceCategory category) {
        return new ServiceCategoryResponse(
                category.getId(), category.getName(), category.getSlug(), category.getIcon());
    }
}
