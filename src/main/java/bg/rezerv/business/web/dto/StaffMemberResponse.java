package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.StaffMember;
import bg.rezerv.business.domain.WorkingHours;
import java.util.List;

public record StaffMemberResponse(
        Long id,
        Long salonId,
        Long userId,
        String displayName,
        String title,
        boolean active,
        List<Long> serviceIds,
        List<WorkingHoursResponse> workingHours) {

    public static StaffMemberResponse from(StaffMember staff,
                                           List<Long> serviceIds,
                                           List<WorkingHours> hours) {
        return new StaffMemberResponse(
                staff.getId(),
                staff.getSalonId(),
                staff.getUserId(),
                staff.getDisplayName(),
                staff.getTitle(),
                Boolean.TRUE.equals(staff.getActive()),
                serviceIds,
                hours.stream().map(WorkingHoursResponse::from).toList());
    }
}
