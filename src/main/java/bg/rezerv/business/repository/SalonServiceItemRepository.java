package bg.rezerv.business.repository;

import bg.rezerv.business.domain.SalonServiceItem;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalonServiceItemRepository extends JpaRepository<SalonServiceItem, Long> {

    @EntityGraph(attributePaths = "category")
    List<SalonServiceItem> findBySalonIdAndActiveTrueOrderByNameAsc(Long salonId);

    /** Минимална цена на активна услуга per салон — за "от X лв." на картите. */
    @Query("""
            select sv.salonId as salonId, min(sv.price) as minPrice
            from SalonServiceItem sv
            where sv.salonId in :salonIds and sv.active = true
            group by sv.salonId
            """)
    List<MinPricePerSalon> findMinPrices(@Param("salonIds") Collection<Long> salonIds);

    interface MinPricePerSalon {
        Long getSalonId();

        BigDecimal getMinPrice();
    }
}
