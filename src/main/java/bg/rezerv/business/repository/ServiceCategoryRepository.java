package bg.rezerv.business.repository;

import bg.rezerv.business.domain.ServiceCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findAllByOrderByNameAsc();
}
