package bg.rezerv.business.repository;

import bg.rezerv.business.domain.StaffMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    List<StaffMember> findBySalonIdAndActiveTrueOrderByDisplayNameAsc(Long salonId);

    Optional<StaffMember> findByIdAndSalonId(Long id, Long salonId);

    Optional<StaffMember> findByUserIdAndSalonIdAndActiveTrue(Long userId, Long salonId);

    List<StaffMember> findByUserIdAndActiveTrue(Long userId);

    boolean existsBySalonIdAndUserId(Long salonId, Long userId);
}
