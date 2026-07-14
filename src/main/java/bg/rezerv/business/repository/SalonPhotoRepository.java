package bg.rezerv.business.repository;

import bg.rezerv.business.domain.SalonPhoto;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonPhotoRepository extends JpaRepository<SalonPhoto, Long> {

    List<SalonPhoto> findBySalonIdOrderByPositionAsc(Long salonId);

    List<SalonPhoto> findBySalonIdInOrderByPositionAsc(Collection<Long> salonIds);
}
