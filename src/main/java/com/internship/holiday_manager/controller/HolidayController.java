package com.internship.holiday_manager.controller;
import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.dto.holiday.UpdateDetailsHolidayDto;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.service.holiday_service.HolidayService;
import com.internship.holiday_manager.utils.annotations.AllowEmployee;
import com.internship.holiday_manager.utils.annotations.AllowTeamLead;
import com.internship.holiday_manager.utils.annotations.AllowTeamLeadAndEmployee;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/holiday")
@CrossOrigin
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @PostMapping("/add-holiday")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<HolidayDto> addHoliday(@RequestBody HolidayDto dto){
        return new ResponseEntity<>(holidayService.createHoliday(dto), HttpStatus.OK);
    }
    @PutMapping("/update-holiday")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<HolidayDto> updateHoliday(@RequestBody HolidayDto dto){
        return new ResponseEntity<>(holidayService.updateHoliday(dto), HttpStatus.OK);
    }
    @GetMapping("/get-all-holidays")
    @AllowEmployee
    public ResponseEntity<List<HolidayDto>> getAll(){
        return new ResponseEntity<>(holidayService.getAll(),HttpStatus.OK);
    }

    @GetMapping("/get-users-holidays/{id}")
    @AllowEmployee
    public ResponseEntity<List<HolidayDto>> getUsersHoliday(@PathVariable Long id){
        return new ResponseEntity<>(holidayService.getUsersHolidays(id),HttpStatus.OK);
    }

    @DeleteMapping("/delete-holiday/{id}")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<HolidayDto> deleteHoliday(@PathVariable Long id) {
        return new ResponseEntity(holidayService.deleteHoliday(id), HttpStatus.OK);
    }

    @GetMapping("/requests-filtered-by")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<List<HolidayDto>> getRequestsFilteredByType(@RequestParam(required = false) HolidayStatus status, @RequestParam(required = false) HolidayType type, @RequestParam("id") Long id) {
        if (type != null && status != null) {
            return new ResponseEntity<>(holidayService.getRequestsByStatusAndType(id, status, type), HttpStatus.OK);
        }
        else if(type != null){
            return new ResponseEntity<>(holidayService.getRequestsByType(id, type), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(holidayService.getRequestsByStatus(id, status), HttpStatus.OK);
        }
    }

    @PutMapping("/approve/{id}")
    @AllowTeamLead
    public ResponseEntity<HolidayDto> approveHolidayRequest(@PathVariable Long id){
        return new ResponseEntity<>(holidayService.approveHolidayRequest(id),HttpStatus.OK);
    }
    @PutMapping("/deny/{id}")
    @AllowTeamLead
    public ResponseEntity<HolidayDto> denyHolidayRequest(@PathVariable Long id){
        return new ResponseEntity<>(holidayService.denyHolidayRequest(id),HttpStatus.OK);
    }
    @PutMapping("/details")
    @AllowTeamLead
    public ResponseEntity<HolidayDto> requestMoreHolidayDetails(@RequestBody UpdateDetailsHolidayDto updateDetailsHolidayDto){
        return new ResponseEntity<>(holidayService.requestMoreDetails(updateDetailsHolidayDto),HttpStatus.OK);
    }


    @GetMapping("/number-of-holidays")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<Integer> getNoHolidays(@RequestParam String startDate, @RequestParam String endDate){
        LocalDateTime sD =  LocalDateTime. parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime eD = LocalDateTime. parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new ResponseEntity<>(holidayService.getNoHolidays(sD, eD), HttpStatus.OK);
    }

    @GetMapping("/holiday-info")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<HolidayDto> getHolidayById(@RequestParam Long id){
        return new ResponseEntity<>(holidayService.getHolidayById(id), HttpStatus.OK);
    }

}
