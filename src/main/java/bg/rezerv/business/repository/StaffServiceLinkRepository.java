package bg.rezerv.business.repository;

import bg.rezerv.business.domain.StaffServiceLink;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffServiceLinkRepository extends JpaRepository<StaffServiceLink, StaffServiceLink.Pk> {

    List<StaffServiceLink> findByStaffId(Long staffId);

    List<StaffServiceLink> findByStaffIdIn(Collection<Long> staffIds);

    void deleteByStaffId(Long staffId);
}
