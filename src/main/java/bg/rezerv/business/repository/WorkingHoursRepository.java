package bg.rezerv.business.repository;

import bg.rezerv.business.domain.WorkingHours;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

    List<WorkingHours> findBySalonIdAndStaffIdIsNullOrderByDayOfWeekAsc(Long salonId);

    List<WorkingHours> findBySalonIdInAndStaffIdIsNullOrderBySalonIdAscDayOfWeekAsc(Collection<Long> salonIds);

    void deleteBySalonIdAndStaffIdIsNull(Long salonId);
}
