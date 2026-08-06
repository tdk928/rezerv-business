package bg.rezerv.business.repository;

import bg.rezerv.business.domain.StaffServiceLink;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffServiceLinkRepository extends JpaRepository<StaffServiceLink, StaffServiceLink.Pk> {

    List<StaffServiceLink> findByStaffId(Long staffId);

    List<StaffServiceLink> findByStaffIdIn(Collection<Long> staffIds);

    /** Flush + clear — иначе delete + insert на същите PK в една транзакция може да се обърка. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StaffServiceLink s where s.staffId = :staffId")
    void deleteByStaffId(@Param("staffId") Long staffId);
}
