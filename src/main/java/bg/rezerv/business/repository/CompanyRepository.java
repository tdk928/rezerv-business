package bg.rezerv.business.repository;

import bg.rezerv.business.domain.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByEik(String eik);

    List<Company> findByOwnerUserIdOrderByCreatedAtAsc(Long ownerUserId);

    Optional<Company> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
