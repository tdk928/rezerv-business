package bg.rezerv.business.repository;

import bg.rezerv.business.domain.TimeOff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {

    List<TimeOff> findByStaffIdOrderByStartsAtAsc(Long staffId);
}
