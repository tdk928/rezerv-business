package bg.rezerv.business.repository;

import bg.rezerv.business.domain.TimeOff;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {

    List<TimeOff> findByStaffIdOrderByStartsAtAsc(Long staffId);

    @Query("""
            SELECT t FROM TimeOff t
            WHERE t.staffId IN :staffIds
              AND t.startsAt < :to
              AND t.endsAt > :from
            ORDER BY t.staffId ASC, t.startsAt ASC
            """)
    List<TimeOff> findOverlapping(@Param("staffIds") Collection<Long> staffIds,
                                  @Param("from") Instant from,
                                  @Param("to") Instant to);
}
