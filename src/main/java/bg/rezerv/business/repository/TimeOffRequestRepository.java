package bg.rezerv.business.repository;

import bg.rezerv.business.domain.TimeOffRequest;
import bg.rezerv.business.domain.TimeOffRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Long> {

    List<TimeOffRequest> findByStaffIdOrderByCreatedAtDesc(Long staffId);

    List<TimeOffRequest> findByStaffIdInAndStatusOrderByCreatedAtAsc(
            List<Long> staffIds, TimeOffRequestStatus status);

    Optional<TimeOffRequest> findByIdAndStatus(Long id, TimeOffRequestStatus status);
}
