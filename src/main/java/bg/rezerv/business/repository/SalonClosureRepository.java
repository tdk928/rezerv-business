package bg.rezerv.business.repository;

import bg.rezerv.business.domain.SalonClosure;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonClosureRepository extends JpaRepository<SalonClosure, Long> {

    List<SalonClosure> findBySalonIdAndClosedOnBetweenOrderByClosedOnAsc(
            Long salonId, LocalDate from, LocalDate to);

    Optional<SalonClosure> findByIdAndSalonId(Long id, Long salonId);

    boolean existsBySalonIdAndClosedOn(Long salonId, LocalDate closedOn);
}
