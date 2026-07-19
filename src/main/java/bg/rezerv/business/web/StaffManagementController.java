package bg.rezerv.business.web;

import bg.rezerv.business.service.StaffManagementService;
import bg.rezerv.business.web.dto.AddStaffRequest;
import bg.rezerv.business.web.dto.CreateStaffRequest;
import bg.rezerv.business.web.dto.CreateSalonClosureRequest;
import bg.rezerv.business.web.dto.CreateTimeOffRequest;
import bg.rezerv.business.web.dto.ReplaceSalonWorkingHoursRequest;
import bg.rezerv.business.web.dto.ReplaceStaffServicesRequest;
import bg.rezerv.business.web.dto.SalonClosureResponse;
import bg.rezerv.business.web.dto.StaffMemberResponse;
import bg.rezerv.business.web.dto.TimeOffRequestResponse;
import bg.rezerv.business.web.dto.TimeOffResponse;
import bg.rezerv.business.web.dto.WorkingHoursResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class StaffManagementController {

    private final StaffManagementService staffManagementService;

    public StaffManagementController(StaffManagementService staffManagementService) {
        this.staffManagementService = staffManagementService;
    }

    @PostMapping("/salons/{salonId}/staff")
    public StaffMemberResponse addStaff(HttpServletRequest request,
                                        @PathVariable Long salonId,
                                        @Valid @RequestBody AddStaffRequest body) {
        return staffManagementService.addStaff(RequestContext.requireAuthenticated(request), salonId, body);
    }

    @PostMapping("/salons/{salonId}/staff/create")
    public StaffMemberResponse createStaff(HttpServletRequest request,
                                           @PathVariable Long salonId,
                                           @Valid @RequestBody CreateStaffRequest body) {
        return staffManagementService.createStaff(RequestContext.requireAuthenticated(request), salonId, body);
    }

    @GetMapping("/salons/{salonId}/staff")
    public List<StaffMemberResponse> listStaff(HttpServletRequest request, @PathVariable Long salonId) {
        return staffManagementService.listStaff(RequestContext.requireAuthenticated(request), salonId);
    }

    @PutMapping("/staff/{staffId}/working-hours")
    public List<WorkingHoursResponse> replaceStaffHours(HttpServletRequest request,
                                                        @PathVariable Long staffId,
                                                        @Valid @RequestBody ReplaceSalonWorkingHoursRequest body) {
        return staffManagementService.replaceStaffWorkingHours(
                RequestContext.requireAuthenticated(request), staffId, body);
    }

    @PutMapping("/staff/{staffId}/services")
    public StaffMemberResponse replaceServices(HttpServletRequest request,
                                               @PathVariable Long staffId,
                                               @Valid @RequestBody ReplaceStaffServicesRequest body) {
        return staffManagementService.replaceStaffServices(
                RequestContext.requireAuthenticated(request), staffId, body);
    }

    @PostMapping("/salons/{salonId}/closures")
    public SalonClosureResponse closeDay(HttpServletRequest request,
                                         @PathVariable Long salonId,
                                         @Valid @RequestBody CreateSalonClosureRequest body) {
        return staffManagementService.closeSalonDay(RequestContext.requireAuthenticated(request), salonId, body);
    }

    @GetMapping("/salons/{salonId}/closures")
    public List<SalonClosureResponse> listClosures(
            HttpServletRequest request,
            @PathVariable Long salonId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return staffManagementService.listClosures(RequestContext.requireAuthenticated(request), salonId, from, to);
    }

    @DeleteMapping("/salons/{salonId}/closures/{closureId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClosure(HttpServletRequest request,
                              @PathVariable Long salonId,
                              @PathVariable Long closureId) {
        staffManagementService.deleteClosure(RequestContext.requireAuthenticated(request), salonId, closureId);
    }

    @PostMapping("/staff/{staffId}/time-off")
    public TimeOffResponse createOwnerTimeOff(HttpServletRequest request,
                                              @PathVariable Long staffId,
                                              @Valid @RequestBody CreateTimeOffRequest body) {
        return staffManagementService.createOwnerTimeOff(
                RequestContext.requireAuthenticated(request), staffId, body);
    }

    @GetMapping("/staff/{staffId}/time-off")
    public List<TimeOffResponse> listTimeOff(HttpServletRequest request, @PathVariable Long staffId) {
        return staffManagementService.listTimeOff(RequestContext.requireAuthenticated(request), staffId);
    }

    @GetMapping("/salons/{salonId}/time-off-requests")
    public List<TimeOffRequestResponse> listPending(HttpServletRequest request, @PathVariable Long salonId) {
        return staffManagementService.listPendingRequestsForSalon(
                RequestContext.requireAuthenticated(request), salonId);
    }

    @PostMapping("/time-off-requests/{requestId}/approve")
    public TimeOffRequestResponse approve(HttpServletRequest request, @PathVariable Long requestId) {
        return staffManagementService.approveRequest(RequestContext.requireAuthenticated(request), requestId);
    }

    @PostMapping("/time-off-requests/{requestId}/reject")
    public TimeOffRequestResponse reject(HttpServletRequest request, @PathVariable Long requestId) {
        return staffManagementService.rejectRequest(RequestContext.requireAuthenticated(request), requestId);
    }

    @PostMapping("/staff/me/time-off-requests")
    public TimeOffRequestResponse createMyRequest(HttpServletRequest request,
                                                  @Valid @RequestBody CreateTimeOffRequest body) {
        return staffManagementService.createMyTimeOffRequest(RequestContext.requireAuthenticated(request), body);
    }

    @GetMapping("/staff/me/time-off-requests")
    public List<TimeOffRequestResponse> listMyRequests(HttpServletRequest request) {
        return staffManagementService.listMyTimeOffRequests(RequestContext.requireAuthenticated(request));
    }
}
