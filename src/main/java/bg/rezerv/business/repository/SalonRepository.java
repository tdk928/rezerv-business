package bg.rezerv.business.repository;

import bg.rezerv.business.domain.Salon;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalonRepository extends JpaRepository<Salon, Long> {

    @EntityGraph(attributePaths = "city")
    List<Salon> findByCompanyIdInOrderByNameAsc(Collection<Long> companyIds);

    List<Salon> findByCompanyId(Long companyId);

    @EntityGraph(attributePaths = "city")
    @Query("""
            select s from Salon s
            where s.status = bg.rezerv.business.domain.SalonStatus.ACTIVE
              and (:cityId is null or s.city.id = :cityId)
              and (:categoryId is null or exists (
                    select 1 from SalonServiceItem sv
                    where sv.salonId = s.id and sv.category.id = :categoryId and sv.active = true))
              and (:q is null or lower(s.name) like lower(concat('%', cast(:q as string), '%')))
            order by s.ratingAvg desc, s.ratingCount desc, s.name asc
            """)
    Page<Salon> search(@Param("cityId") Long cityId,
                       @Param("categoryId") Long categoryId,
                       @Param("q") String q,
                       Pageable pageable);

    @EntityGraph(attributePaths = "city")
    Optional<Salon> findWithCityById(Long id);
}
