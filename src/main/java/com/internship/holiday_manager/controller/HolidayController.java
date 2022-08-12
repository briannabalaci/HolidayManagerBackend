package com.internship.holiday_manager.controller;
import com.internship.holiday_manager.dto.holiday.HolidayDto;
import com.internship.holiday_manager.service.holiday_service.HolidayService;
import com.internship.holiday_manager.utils.annotations.AllowEmployee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @AllowEmployee
    public ResponseEntity<HolidayDto> addHoliday(@RequestBody HolidayDto dto){
        return new ResponseEntity<>(holidayService.createHoliday(dto), HttpStatus.OK);
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
}
